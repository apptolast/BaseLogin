# Spec 002: Puerto de Firebase Auth y capa social completa (FLE-90)

> Rama: `feature/002-firebase-auth-gateway` · Proyecto: `BaseLogin` (`:baselogin`) · Estado: draft
> **Precedido por FLE-91** (spec 001, CocoaPods → SPM), ya implementado en el PR #11. El smoke
> manual de iOS de este spec corre sobre SPM con `firebase-ios-sdk` 11.8.1, el mismo que Fledge.
> El spec es el mecanismo anti-deriva: debe ser autosuficiente (releíble al inicio de cada fase).

## Contexto y objetivo

`FirebaseAuthProvider` es la pieza central de esta librería: 389 líneas que cubren email/password,
OAuth, teléfono, magic link y gestión de cuenta sobre el SDK de GitLive. **No tiene cobertura
automática**, y no por descuido: es imposible por construcción. Lo dice su propio test:

> *Direct unit-testing of `FirebaseAuthProvider` is deferred because it requires a live or emulated
> Firebase instance (`FirebaseAuth` is a platform expect class).* — `FirebaseAuthProviderTest.kt`

Ese fichero acaba ejercitando `FakeAuthRepository`: un fake comprobando un fake. Cero garantías sobre
el código que de verdad autentica a los usuarios de todas las apps de la flota.

El detonante es **FLE-88** (Fledge). Fledge autentica hoy por REST porque su integración SPM se
revirtió en FLE-8; ya está arreglada, así que va a pasar a consumir el `AuthProvider` de esta
librería. Al evaluarlo aparecieron, además del hueco de tests, tres defectos reales que **afectan a
cualquier consumidor**, no solo a Fledge. Arreglarlos aquí es lo que evita que cada app se escriba su
propio provider — que es exactamente lo que Fledge iba a hacer.

## Alcance

**Dentro:**

- **Puerto `FirebaseAuthGateway`** en `commonMain` que no expone tipos `dev.gitlive.*`, con adaptador
  `GitLiveFirebaseAuthGateway` sobre `FirebaseAuth`. `FirebaseAuthProvider` pasa a depender del
  puerto, con lo que se vuelve testeable desde `commonTest` con fakes a mano.
- **Resolución perezosa**: construir el grafo de Koin deja de tocar el SDK.
- **`displayName` de Apple**: propagarlo, de forma retrocompatible con el Swift ya integrado.
- **`signOut` social**: limpiar el estado de credenciales de la plataforma al cerrar sesión.
- **Higiene de `Platform.android.kt`**: el `lateinit var appContext` global y el `object` que resuelve
  Koin con `try/catch` silencioso.
- **ktlint + `.editorconfig`**, prerequisito del gate `/validate`.
- Retraducir `FirebaseAuthProviderTest`, que hoy no prueba lo que su nombre dice.

**Fuera:**

- Cambiar la UI, los slots, la navegación, los ViewModels o las cadenas de la librería.
- Tocar `AuthRepository`, `AuthRepositoryImpl` o los modelos de dominio (`AuthResult`, `AuthError`,
  `Credentials`, `UserSession`). El contrato público hacia las apps no se mueve.
- Añadir proveedores de identidad nuevos.
- Subir la versión de GitLive (2.4.0) ni la del BOM de Firebase (34.14.1). Verificado que Fledge, que
  declara 2.5.0, eleva el transitivo sin conflicto.
- Migrar el consumo de Fledge: eso es FLE-88, en el otro repo.
- **Migrar la integración iOS de CocoaPods a SPM: eso es FLE-91**, que va antes que este ticket. Aquí
  no se toca `composeApp/build.gradle.kts`, el `Podfile` ni el `.xcworkspace`.

## Conocimiento reutilizable

De **Fledge / FLE-78** (`specs/002-firestore-sdk-integration`, engram #106):

*Se reutiliza tal cual:* el **patrón de puerto**. `GitLiveFirestoreProvider` deja el handle
`FirebaseFirestore` como miembro concreto **fuera** de la interfaz, y esa única decisión es lo que
permite escribir fakes en `commonTest` sin importar `dev.gitlive.*`. Funcionó, está en producción, y
es literalmente el arreglo que aquí falta.

*Se reutiliza tal cual:* la **resolución perezosa**. `GitLiveFirestoreProvider.firestore()` no toca
Firebase al construirse, porque Koin instancia antes de que el bootstrap haya corrido. `Firebase.auth`
tiene el mismo problema.

*Se adapta:* Fledge descubrió que **`koinApplication { }` sí crea instancias eager por defecto en Koin
4.2.x**. Aquí ningún binding nuevo puede ser `createdAtStart`, y los tests que construyan el grafo
deben poder hacerlo sin Firebase inicializado.

De **Fledge / FLE-87**: la configuración de ktlint va en `.editorconfig`, no en bloques `filter {}` de
Gradle. Excepción necesaria: `function-naming` para `@Composable`.

## Diseño

### El puerto

`baselogin/src/commonMain/kotlin/com/apptolast/baselogin/data/firebase/`

| Fichero | Rol |
|---|---|
| `FirebaseAuthGateway.kt` | El puerto. Tipos propios: `FirebaseAuthUser`, `FirebaseAuthCredential` (sellado: `EmailPassword` / `Google(idToken, accessToken?)` / `OAuth(providerId, idToken, rawNonce?)` / `Phone`) y `FirebaseAuthFailure`. **Sin `FirebaseAuthErrorKind`** — ver corrección abajo |
| `GitLiveFirebaseAuthGateway.kt` | El adaptador. **Único** fichero que importa `dev.gitlive.firebase.auth.*`. Resuelve `Firebase.auth` de forma perezosa. Un funnel convierte cualquier throwable del SDK en `FirebaseAuthFailure` |

`FirebaseAuthProvider(firebaseAuth: FirebaseAuth)` pasa a `FirebaseAuthProvider(gateway:
FirebaseAuthGateway)`.

> **Corrección introducida en `/plan` (29-07-2026).** El spec preveía un enum `FirebaseAuthErrorKind`
> propio del puerto. **Se descarta**: `data/DataMapper.kt` ya tiene `mapFirebaseErrorMessage(String)`,
> una función pura que mapea las tres familias de string —códigos REST (`INVALID_CREDENTIAL`), del SDK
> nativo de Android (`ERROR_WRONG_PASSWORD`) y del SDK web (`wrong-password`)— a los `AuthError`
> tipados, y ya tiene **43 tests** en `DataMapperTest`.
>
> El defecto real, verificado con `grep`, es otro: **los 12 bloques `catch (e: Exception)` de
> `FirebaseAuthProvider` nunca llaman a ese mapper**. Como `FirebaseNetworkException` no hereda de
> `FirebaseAuthException`, cae al catch genérico y acaba en `AuthError.Unknown`, cuando el mapper ya
> sabía devolver `NetworkError` para ese mismo mensaje. El arreglo es **enrutar el catch**, no añadir
> un enum ni un clasificador nuevo. `FirebaseAuthFailure` solo necesita transportar el `message`
> original del SDK.

> ⚠️ **Es un cambio de firma en API pública.** Quien construya `FirebaseAuthProvider` a mano rompe.
> Quien use `loginDataModule()` —el camino documentado y el que usan las apps— no se entera. Va aquí
> escrito para que no se descubra en el `/validate` de otro.

> ⚠️ **Trampa, verificada sobre el bytecode real y no sobre documentación.** En Android los tipos de
> excepción de GitLive son `actual typealias` a los de Google — su artefacto `firebase-app-android` no
> contiene ninguna clase `*Exception*`. Y en el SDK de Google:
>
> ```
> com.google.firebase.FirebaseNetworkException          extends com.google.firebase.FirebaseException
> com.google.firebase.FirebaseTooManyRequestsException  extends com.google.firebase.FirebaseException
> com.google.firebase.auth.FirebaseAuthException        (paquete distinto, hermana de las anteriores)
> ```
>
> Es decir: **`catch (e: FirebaseAuthException)` no captura un error de red**. El
> `FirebaseAuthProvider` actual tiene ese `catch` en **8 sitios**, siempre seguido de un
> `catch (e: Exception)` genérico, así que hoy un fallo de red se mapea a `AuthError.Unknown` en vez
> de a `AuthError.NetworkError` — y la UI no puede ofrecer «reintentar». Es un bug real y silencioso,
> no una hipótesis. El adaptador los trata aparte (AC-09).

### `displayName` de Apple

Hoy el canal Swift→Kotlin es una cadena empaquetada, `idToken|||rawNonce|||<nonce>`, y el nombre se
tira. Apple solo envía `fullName` en la **primerísima** autorización de cada usuario contra cada app:
si se pierde ahí, se pierde para siempre. Un usuario nuevo que entre con Apple se queda sin nombre y
no hay forma de recuperarlo.

Se **añade un segmento opcional**, `|||displayName|||<name>`, en vez de cambiar el formato:

```
idToken                                          ← sigue valiendo
idToken|||rawNonce|||<nonce>                     ← sigue valiendo (integraciones actuales)
idToken|||rawNonce|||<nonce>|||displayName|||<n> ← nuevo
```

Retrocompatible por diseño: el Swift ya escrito de cualquier consumidor sigue funcionando sin tocar
nada, y quien quiera el nombre añade un segmento. La alternativa —cruzar un tipo estructurado por la
frontera— es más limpia pero rompe a todos los que ya tienen el handler cableado, así que se descarta
para esta iteración.

### `signOut` social

Nuevo `expect suspend fun clearSocialSignInState()` en `Platform.kt`, invocado desde
`FirebaseAuthProvider.signOut()`. En Android limpia el estado de Credential Manager; en iOS es un
no-op documentado (Apple no cachea la selección de cuenta igual).

Sin esto, tras cerrar sesión en Android el selector de cuenta de Google **no reaparece** y el usuario
no puede cambiar de cuenta. Es un bug de producto, no una molestia estética.

## Criterios de aceptación (Gherkin)

```gherkin
# ---------- Testabilidad: el objetivo del ticket ----------

Scenario [AC-01]: Ningún tipo del SDK cruza la frontera de test
  Given el puerto FirebaseAuthGateway y su adaptador
  When  se inspeccionan las fuentes
  Then  ningún fichero de commonTest importa dev.gitlive.*
   And  la interfaz del puerto no declara ningún tipo dev.gitlive.* en su firma

Scenario [AC-02]: Construir el grafo de Koin no toca el SDK
  Given loginDataModule sin authProvider propio
  When  se construye el grafo y se resuelve AuthProvider
  Then  la resolución tiene éxito sin que FirebaseApp esté inicializada
   And  el gateway registra cero interacciones con el SDK

# ---------- Comportamiento del provider, ahora testeable ----------

Scenario [AC-03]: El login con email y password delega en el gateway
  Given un gateway falso con un usuario existente
  When  el provider ejecuta signIn con Credentials.EmailPassword
  Then  el resultado es AuthResult.Success con el uid de ese usuario
   And  el gateway recibió una credencial EmailPassword con ese email

Scenario [AC-04]: El registro propaga el displayName al perfil
  Given un gateway falso que acepta la creación de usuario
  When  el provider ejecuta signUp con email, password y displayName
  Then  el resultado es AuthResult.Success
   And  el gateway registró una actualización de perfil con ese displayName

Scenario [AC-05]: Google entrega idToken y accessToken como credencial
  Given una capa social falsa que devuelve "idToken|||accessToken|||at-1"
  When  el provider ejecuta signIn con Credentials.OAuthToken(Google)
  Then  el gateway recibió FirebaseAuthCredential.Google con idToken "idToken"
        y accessToken "at-1"

Scenario [AC-06]: Apple entrega idToken y rawNonce como credencial OAuth
  Given una capa social falsa que devuelve "a-token|||rawNonce|||n-1"
  When  el provider ejecuta signIn con Credentials.OAuthToken(Apple)
  Then  el gateway recibió FirebaseAuthCredential.OAuth con providerId "apple.com",
        idToken "a-token" y rawNonce "n-1"

Scenario [AC-07]: El displayName de Apple llega a la sesión
  Given una capa social falsa que devuelve
        "a-token|||rawNonce|||n-1|||displayName|||Ana Pérez"
  When  el provider ejecuta signIn con Credentials.OAuthToken(Apple)
   And  el usuario del SDK no trae displayName, como ocurre en el primer login real
  Then  la UserSession resultante tiene displayName "Ana Pérez"
   And  el gateway registró una actualización de perfil con ese nombre

Scenario [AC-08]: El formato antiguo de Apple sigue funcionando
  Given una capa social falsa que devuelve "a-token|||rawNonce|||n-1", sin segmento de nombre
  When  el provider ejecuta signIn con Credentials.OAuthToken(Apple)
  Then  el resultado es AuthResult.Success
   And  no se intenta ninguna actualización de perfil
   And  ningún consumidor con el Swift ya integrado necesita cambiar nada

Scenario [AC-09]: Los errores del SDK se mapean a los AuthError tipados
  Given un gateway falso que lanza FirebaseAuthFailure con cada familia de mensaje del SDK
  When  el provider ejecuta la operación correspondiente
  Then  cada uno produce su AuthError tipado
   And  un fallo de red produce AuthError.NetworkError y no AuthError.Unknown

Scenario [AC-10]: Cerrar sesión limpia el estado social de la plataforma
  Given una sesión iniciada con Google
  When  el provider ejecuta signOut
  Then  el gateway cerró la sesión de Firebase
   And  se invocó la limpieza del estado de credenciales de la plataforma

Scenario [AC-11]: observeAuthState refleja los cambios del SDK
  Given un gateway falso que emite un usuario y luego null
  When  se recoge observeAuthState
  Then  la primera emisión es AuthState.Loading
   And  después llega AuthState.Authenticated y por último AuthState.Unauthenticated

# ---------- Verificable por comando de build ----------

Scenario [AC-12]: El proyecto sigue construyendo y pasa el estilo
  Given ktlint configurado con .editorconfig como única fuente de estilo
  When  se ejecutan ktlintCheck, :baselogin:testDebugUnitTest,
        :baselogin:linkDebugFrameworkIosSimulatorArm64 y :composeApp:assembleDebug
  Then  todos terminan en BUILD SUCCESSFUL
```

## Desglose de tareas (ligero)

- [ ] **T0** — ktlint + `.editorconfig` (estilo, excepción `function-naming` para `@Composable`,
      exclusión de rutas generadas). Commit de formato **separado** del funcional. → **AC-12**
- [ ] **T1a** — Esqueletos con `TODO()`: puerto, tipos propios, adaptador. → *(fase `/test`)*
- [ ] **T1b** — Tests + `FakeFirebaseAuthGateway` y capa social falsa; **reescribir**
      `FirebaseAuthProviderTest`, que hoy no prueba el provider. → *(fase `/test`)*
- [ ] **T2** — Implementar el adaptador `GitLiveFirebaseAuthGateway` (perezoso, funnel de
      excepciones, `FirebaseNetworkException` tratada aparte). → **AC-01**, **AC-09**
- [ ] **T3** — Migrar `FirebaseAuthProvider` al puerto, sin cambiar su comportamiento observable.
      → **AC-03…AC-06**, **AC-09**, **AC-11**
- [ ] **T4** — `displayName` de Apple: nuevo segmento retrocompatible + propagación al perfil.
      → **AC-07**, **AC-08**
- [ ] **T5** — `clearSocialSignInState()` `expect/actual` + llamada desde `signOut()`. → **AC-10**
- [ ] **T6** — `loginDataModule`: registrar el gateway y quitar el `single { Firebase.auth }` eager.
      → **AC-02**
- [ ] **T7** — Higiene de `Platform.android.kt`: `appContext` y el `try/catch` silencioso del
      `object` que resuelve Koin.
- [ ] **T8** — Actualizar `CLAUDE.md` (§Architecture, §expect/actual) y el `README` si documenta el
      formato de token de Apple.
- [ ] **T9** — Validación completa; smoke manual de Google en Android y Apple en iOS con
      `composeApp`. Publicar el commit para que FLE-88 pueda repinear.

## Notas no funcionales

**Plataformas**: Android e iOS. La librería no tiene target web ni desktop.

**Compatibilidad**: es el riesgo dominante. El único cambio de firma pública es el constructor de
`FirebaseAuthProvider`; el formato de token de Apple se amplía sin romper. Los modelos de dominio no
se tocan.

**i18n / accesibilidad / diseño visual**: no aplican — no se toca UI ni se añaden cadenas.
**`/design-check` se prevé N/A**, a confirmar en su gate.

**Testabilidad**: es el objetivo del ticket, no una nota al pie. Regla verificable con grep en
`/validate`: **`commonTest` no importa `dev.gitlive.*`**.

**Qué queda solo bajo smoke manual**: el adaptador GitLive real, Credential Manager en Android,
`ASAuthorizationController` en iOS, y la limpieza de credenciales de AC-10 en dispositivo. Es
irreducible: son APIs de plataforma.

**Cómo se consigue el ROJO legítimo en `/test`**: esqueletos con `TODO()` **junto a** los tests, para
que compile y falle con `NotImplementedError`. La reescritura de `FirebaseAuthProviderTest` **tiene
que ocurrir en `/test`** — en `/implement` un hook bloquea los ficheros de test.

## Decisiones abiertas (🚦 Gate 1)

| # | Decisión | Propuesta |
|---|---|---|
| **D-1** ⚠️ | **Cambio de firma pública** en el constructor de `FirebaseAuthProvider`. | Aceptarlo: es el precio de la testabilidad y solo afecta a quien lo construya a mano, no a quien use `loginDataModule()`. Alternativa descartada: mantener un constructor secundario que tome `FirebaseAuth`, que reintroduce la dependencia intestable. |
| **D-2** | **Formato del token de Apple**: segmento adicional retrocompatible frente a tipo estructurado. | Segmento adicional. El tipo estructurado es más limpio pero rompe todos los handlers Swift ya cableados. Anotar como mejora futura. |
| **D-3** | **`:baselogin:iosSimulatorArm64Test` existe** como tarea aquí, a diferencia de Fledge. No se ha comprobado si enlaza (en Fledge falla con `ld: framework 'FirebaseCore' not found`). | Comprobarlo en `/plan`, **después de FLE-91**: el resultado depende de si las dependencias nativas vienen por pods o por SPM, así que medirlo ahora daría una respuesta que caduca. Si enlaza, es cobertura nativa gratis que Fledge no puede tener; si no, se documenta y se queda fuera del gate. |
| **D-4** | **Versiones**: BaseLogin fija GitLive 2.4.0 y BOM 34.14.1; Fledge usa 2.5.0 y BOM 33.15.0. | No tocarlas en este ticket. Verificado en FLE-88 que Gradle eleva el transitivo a 2.5.0 sin conflicto y que compila y enlaza. Alinearlas es un ticket propio. |
| **D-5** | El working tree tiene cambios sin commitear en `iosApp/`: `project.pbxproj` con `DEVELOPMENT_TEAM = 3NXH5U7C5A` sustituyendo al placeholder `${TEAM_ID}` y `CODE_SIGN_STYLE = Manual`, más un `iosApp.entitlements` nuevo con la capacidad Apple Sign-In. | **Decisión del usuario.** El `entitlements` parece legítimo y útil; el team id hardcodeado parece artefacto local que no debería commitearse. No se tocan en este ticket: están en la app de demo, no en `:baselogin`. |

## Evidencia del smoke manual (29-07-2026)

Ejecutado por el usuario sobre la app de demostración: **el login funciona correctamente**. Con eso
quedan cubiertos los dos comportamientos que ningún test automático puede alcanzar, porque dependen
de APIs de plataforma y de credenciales reales:

- Android: Credential Manager y la reaparición del selector de cuenta tras `signOut()`.
- iOS: `ASAuthorizationController` y la propagación del nombre de Apple al perfil.

Es el complemento necesario a los 245 tests: el puerto prueba la lógica del provider, el smoke prueba
que el adaptador habla de verdad con el SDK.

## Trazabilidad

Suite completa del modulo: **245 tests, 0 fallos**. Los 18 nuevos estuvieron en rojo con
`kotlin.NotImplementedError` en el commit `aa4915b` y pasaron a verde en `9273b0d`.

| AC | Test(s) que lo cubren | ¿Rojo antes de implementar? |
|----|-----------------------|------------------------------|
| AC-01 | *(no unitario)* — `grep -rn "dev.gitlive" baselogin/src/commonTest/` → **0**, y un solo fichero de `commonMain` lo importa: `GitLiveFirebaseAuthGateway.kt` | n/a — verificable por inspección |
| AC-02 | *(no unitario)* — `loginDataModule` registra el gateway sin `createdAtStart`; el adaptador resuelve `Firebase.auth` en un getter | n/a — verificable por inspección |
| AC-03 | `` `FLE-90 email password sign in delegates to the gateway` `` | **sí** — `NotImplementedError` |
| AC-04 | `` `FLE-90 sign up propagates the display name to the profile` ``, `` `…without display name does not touch the profile` `` | **sí** |
| AC-05 | `` `FLE-90 google sign in forwards id token and access token` `` | **sí** |
| AC-06 | `` `FLE-90 apple sign in forwards id token and raw nonce` `` | **sí** |
| AC-07 | `` `FLE-90 apple sign in propagates the display name` ``, `` `…keeps an existing display name` `` | **sí** |
| AC-08 | `` `FLE-90 legacy apple token format still signs in` ``, `` `FLE-90 bare apple token without nonce still signs in` `` | **sí** |
| AC-09 | `` `FLE-90 network failures map to NetworkError and not to Unknown` ``, `` `…wrong password maps to InvalidCredentials` ``, `` `…too many requests maps to TooManyRequests` ``, más los **43 tests existentes** de `DataMapperTest` | **sí** |
| AC-10 | `` `FLE-90 sign out clears the platform social state` `` | **sí** |
| AC-11 | `` `FLE-90 auth state stream starts with Loading` `` | **sí** |
| AC-12 | *(no unitario)* — `ktlintCheck`, `:baselogin:testDebugUnitTest`, `:composeApp:linkDebugFrameworkIosSimulatorArm64`, `:composeApp:assembleDebug`, `xcodebuild` | n/a — verificable por build |
| **extra** | `` `FLE-90 get current session reads the cache without requesting a token` `` — cubre el cuarto defecto encontrado en `/plan`: el contrato dice «MUST NOT perform network I/O» | **sí** |
| **extra** | `` `FLE-90 cancelled social sign in never touches the gateway` ``, `` `FLE-90 password reset delegates the email to the gateway` `` | **sí** |

> Nota para `/validate`: los ACs marcados «n/a» **no** deben bloquear el gate de trazabilidad por
> falta de test rojo previo; se validan con el comando indicado en la misma fila.
