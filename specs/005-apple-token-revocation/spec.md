# Spec 005: Revocar el token de Apple al borrar la cuenta

> Rama: `feature/005-apple-token-revocation` · Proyecto: `BaseLogin` (`:custom-login` + demo)
> Estado: implementado · Sale de `feature/004-ios-social-handlers`.
> Sin ticket FLE: sale de la auditoría de iOS. Es el primero de la tanda que **añade API pública**.

## Contexto y objetivo

La guía **5.1.1(v)** de App Review obliga a toda app que permita crear cuenta a permitir borrarla, y
para *Sign in with Apple* exige además **revocar el token**, no solo eliminar el usuario de Firebase.
Una app que borra la cuenta sin revocar sigue apareciendo en *Ajustes → Cuenta de Apple → Iniciar
sesión con Apple*, y es motivo de rechazo.

Hoy `deleteAccount()` hace una sola cosa:

```kotlin
override suspend fun deleteAccount(): Result<Unit> = runCatching { gateway.deleteCurrentUser() }
```

Ningún consumidor de esta librería que ofrezca borrado de cuenta y Apple Sign-In puede pasar revisión
sin escribirse la revocación por su cuenta — que es exactamente lo que la librería existe para
evitar. El spec 003 dejó Apple listo para producción **salvo esto**, y quedó anotado en el checklist
del README.

## Alcance

**Dentro:**

- Puerto `SocialTokenRevoker` en `data/firebase/`, con `expect/actual` por plataforma.
- `deleteAccount()` revoca antes de borrar, en **best effort**.
- `FirebaseAuthUser` expone los provider ids del usuario, que es lo que decide a quién hay que
  revocar.
- `IdentityProvider.fromId(...)`, para traducir el id de Firebase al tipo de dominio.
- Seam `revokeHandler` en `AppleSignInProviderIOS` e implementación de referencia en el demo.

**Fuera:**

- Revocar en Android. Apple en Android es OAuth web y la guía aplica a la app de iOS; el `actual` de
  Android es un no-op declarado, no un olvido.
- Revocar tokens de Google, GitHub, Microsoft, Twitter o Facebook: ninguno de esos proveedores lo
  exige, y ninguno tiene una API equivalente en el SDK de Firebase.
- Cambiar la firma de `deleteAccount()` o su `Result<Unit>`.
- La pantalla de borrado de cuenta: la librería no la trae y este ticket no la añade.

## Diseño

### Por qué un puerto y no una llamada directa

Regla del repo: *los tipos de un SDK de terceros no cruzan a un seam testeable*, y una función
`expect` de nivel superior no se puede falsear desde `commonTest`. Es el mismo motivo por el que
existen `SocialTokenProvider` y `SocialSignInStateCleaner`, y este puerto es su tercer hermano:

```kotlin
interface SocialTokenRevoker {
    suspend fun revoke(provider: IdentityProvider)
}
```

`FirebaseAuthProvider` lo recibe **con valor por defecto**, como los otros dos. Eso mantiene compilando
a cualquiera que construya la clase a mano con tres argumentos — los tests existentes de FLE-90 lo
demuestran, porque no se tocan.

### Quién hay que revocar

Se decide por los provider ids del propio usuario, no por configuración: un usuario de email en una
app con Apple habilitado no debe ver ninguna hoja de Apple al borrar su cuenta. `FirebaseAuthUser`
gana `providerIds: List<String> = emptyList()`, que el adaptador de GitLive rellena desde
`providerData`. Campo nuevo con default: aditivo.

### Best effort, y por qué

```
revocar (puede fallar)  →  borrar (tiene que ocurrir)
```

Si la revocación falla —red caída, código caducado, usuario que cancela la hoja— **la cuenta se borra
igual**. Un usuario que pide borrar su cuenta no puede quedarse atrapado porque un servidor de Apple
no responda; y el resultado contrario (cuenta viva, token revocado) es peor para él. Se registra un
warning y se sigue.

### El código de autorización tiene que ser fresco

`revokeToken(withAuthorizationCode:)` necesita un `authorizationCode` de Apple **de menos de cinco
minutos y de un solo uso**. El que se obtuvo al iniciar sesión hace tres días no sirve. Por eso el
handler de revocación del demo **vuelve a lanzar la autorización de Apple** y usa el código que
devuelve esa pasada: además de dar un código válido, satisface el requisito de *recent login* que
Firebase impone para borrar un usuario.

### Opt-in: sin handler no hay hoja

Si el host no asigna `revokeHandler`, el `actual` de iOS registra un warning y no hace nada. Así, un
consumidor ya integrado que actualice el pin **no ve cambiar el comportamiento de `deleteAccount()`**
sin haberlo pedido. Es lo contrario de lo deseable en seguridad, pero aquí lo que está en juego es
una hoja de UI apareciendo sin avisar en un flujo destructivo ya en producción.

## Criterios de aceptación (Gherkin)

```gherkin
Scenario [AC-01]: Se revoca antes de borrar
  Given un usuario cuyo providerIds contiene "apple.com"
  When  se llama a deleteAccount()
  Then  el revoker recibe IdentityProvider.Apple
   And  la revocación ocurre antes de deleteCurrentUser()
   And  el resultado es Result.success

Scenario [AC-02]: A quien no hay que revocar, no se le revoca
  Given un usuario cuyo providerIds es ["password"]
  When  se llama a deleteAccount()
  Then  el revoker no se invoca
   And  la cuenta se borra igual

Scenario [AC-03]: Una revocación fallida no impide borrar
  Given un usuario de Apple
   And  un revoker que lanza excepción
  When  se llama a deleteAccount()
  Then  deleteCurrentUser() se ejecuta igualmente
   And  el resultado sigue siendo Result.success

Scenario [AC-04]: Un fallo al borrar sí se propaga
  Given un gateway que falla al borrar
  When  se llama a deleteAccount()
  Then  el resultado es Result.failure

Scenario [AC-05]: El id de Firebase se traduce al dominio
  Given el id "apple.com"
  When  se llama a IdentityProvider.fromId
  Then  devuelve IdentityProvider.Apple
   And  para un id desconocido devuelve null

Scenario [AC-06]: La API existente no se rompe
  Given un consumidor que construye FirebaseAuthProvider con tres argumentos
  When  compila contra esta versión
  Then  sigue compilando, porque el cuarto puerto tiene valor por defecto

Scenario [AC-07]: Sin handler no hay efecto en iOS
  Given un host que no asigna AppleSignInProviderIOS.shared.revokeHandler
  When  se borra la cuenta de un usuario de Apple
  Then  se registra un warning y la cuenta se borra sin mostrar ninguna hoja

Scenario [AC-08]: El demo revoca de verdad
  Given el demo con el revokeHandler cableado
  When  se borra la cuenta de un usuario de Apple
  Then  se pide una autorización nueva a Apple
   And  se llama a Auth.auth().revokeToken(withAuthorizationCode:) con ese código
   And  la app deja de aparecer en Ajustes → Cuenta de Apple
```

## Desglose de tareas

- [x] **T1** — `SocialTokenRevoker` + `PlatformSocialTokenRevoker` + `expect revokeSocialToken`.
- [x] **T2** — `actual` de Android (no-op declarado) y de iOS (delega en `AppleSignInProviderIOS`).
- [x] **T3** — `providerIds` en `FirebaseAuthUser` y su mapeo en `GitLiveFirebaseAuthGateway`.
- [x] **T4** — `IdentityProvider.fromId`.
- [x] **T5** — Tests rojos de AC-01…AC-05 con `FakeSocialTokenRevoker`.
- [x] **T6** — `deleteAccount()` en `FirebaseAuthProvider`.
- [x] **T7** — `revokeHandler` + `AppleSignInCoordinator` del demo.
- [x] **T8** — README: quitar el checklist de "no lo hace por ti" y documentar el seam.

## Trazabilidad

| AC | Test(s) | ¿Rojo antes de implementar? |
|----|---------|------------------------------|
| AC-01 | `005 deleteAccount revokes the apple token before deleting the user` | sí |
| AC-02 | `005 deleteAccount does not revoke for an email only user` | **no** — verde de partida, porque antes no se revocaba nunca. Queda como guardia de regresión |
| AC-03 | `005 a failing revocation still deletes the account` | sí |
| AC-04 | `005 a failing deletion is reported as a failure` | **no** — verde de partida: `runCatching` ya lo cubría. Guardia de regresión |
| AC-05 | `005 fromId maps the firebase provider id to the domain type` | sí |
| AC-06 | los tests de FLE-90, que construyen con tres argumentos y no se tocan | n/a — verde antes y después |
| AC-07 | *(no unitario)* — inspección del `actual` de iOS | n/a |
| AC-08 | *(no unitario)* — smoke manual en dispositivo | n/a — requiere Apple ID real |

## Notas no funcionales

**API pública que se añade** (nada se cambia ni se borra): `SocialTokenRevoker`,
`PlatformSocialTokenRevoker`, `revokeSocialToken`, `FirebaseAuthUser.providerIds`,
`IdentityProvider.fromId`, `AppleSignInProviderIOS.revokeHandler` y un cuarto parámetro **con
default** en `FirebaseAuthProvider`.

**Compatibilidad binaria**: el parámetro con default preserva la compatibilidad de *fuentes*; un
consumidor que ya haya compilado contra el constructor de tres argumentos y solo actualice el
artefacto sin recompilar podría no encontrar la firma. En la práctica todos consumen por
`loginDataModule`, y JitPack recompila desde fuentes en cada pin.

**Pendiente de Mac**: AC-08 y la compilación del Swift.
