# Spec 006: Cerrar también la sesión de Google en iOS

> Rama: `feature/006-ios-social-signout` · Proyecto: `BaseLogin` (`:baselogin` + demo)
> Estado: implementado · Sale de `feature/005-apple-token-revocation`.
> Sin ticket FLE: sale de la auditoría de iOS.

## Contexto y objetivo

FLE-90 introdujo `clearSocialSignInState()` porque cerrar sesión en Firebase no basta: en Android,
Credential Manager conserva la cuenta elegida y el selector no vuelve a aparecer, así que el usuario
no puede cambiar de cuenta. En iOS se dejó como no-op, con este comentario:

> *No-op on iOS: neither ASAuthorizationController nor GIDSignIn cache an account selection that
> survives sign-out the way Credential Manager does on Android.*

**De Apple es cierto; de Google no.** `GIDSignIn.sharedInstance` mantiene `currentUser` en el
llavero y sobrevive perfectamente al `signOut()` de Firebase. Consecuencia práctica: en iOS, tras
cerrar sesión, el siguiente login de Google reutiliza la cuenta anterior y **el usuario no tiene
forma de cambiar de cuenta desde la app**. Es la misma avería que el ticket original arregló en
Android, en la plataforma que se dio por buena sin comprobarlo.

## Alcance

**Dentro:**

- Seam `signOutHandler` en `GoogleSignInProviderIOS`, y `clearSocialSignInState()` de iOS pasando a
  invocarlo.
- Implementación en el demo: `GIDSignIn.sharedInstance.signOut()`.
- Corregir el comentario de `Platform.kt`, que hoy afirma algo falso sobre iOS.

**Fuera:**

- Apple: ahí el comentario sí era correcto. `ASAuthorizationController` no guarda ninguna selección
  de cuenta que sobreviva al cierre de sesión, y no hay nada que limpiar.
- Los cuatro de OAuth web: la sesión vive en el navegador, no en la app, y el propio flujo vuelve a
  pedir consentimiento.
- Meter una dependencia de GoogleSignIn en `:baselogin`. La librería no la tiene ni la va a tener:
  por eso esto es un seam y no una llamada directa.

## Diseño

### Por qué un seam y no una llamada

`:baselogin` no depende del SDK de GoogleSignIn — el host lo trae por SPM y le pasa los tokens.
Igual que `signInHandler` y `revokeHandler`, la librería declara el hueco y el host lo rellena:

```kotlin
// GoogleSignInProviderIOS.Companion
var signOutHandler: (() -> Unit)? = null
```

```swift
GoogleSignInProviderIOS.Companion.shared.signOutHandler = {
    GIDSignIn.sharedInstance.signOut()
}
```

`clearSocialSignInState()` lo llama y traga cualquier excepción, como el `actual` de Android: la
sesión de Firebase ya está cerrada a esas alturas, y fallar aquí solo convertiría un cierre de sesión
correcto en un error visible.

### Sin handler, el comportamiento de hoy

Si el host no lo asigna, se registra un warning y no pasa nada — exactamente lo que ocurre ahora. Un
consumidor que actualice el pin no ve ningún cambio hasta que decide cablearlo.

## Criterios de aceptación (Gherkin)

```gherkin
Scenario [AC-01]: Cerrar sesión limpia también Google en iOS
  Given un usuario que entró con Google en iOS
  When  llama a signOut()
  Then  clearSocialSignInState invoca el signOutHandler
   And  el siguiente login de Google vuelve a ofrecer el selector de cuentas

Scenario [AC-02]: Sin handler no se rompe nada
  Given un host que no asigna signOutHandler
  When  llama a signOut()
  Then  se registra un warning
   And  signOut() sigue devolviendo Result.success

Scenario [AC-03]: Un handler que falla no rompe el cierre de sesión
  Given un signOutHandler que lanza
  When  llama a signOut()
  Then  la excepción se registra y no se propaga
   And  signOut() sigue devolviendo Result.success

Scenario [AC-04]: El comentario deja de mentir
  Given la documentación de clearSocialSignInState en Platform.kt
  When  se lee lo que dice de iOS
  Then  distingue Apple, donde no hay nada que limpiar, de Google, donde sí
```

## Trazabilidad

| AC | Test(s) | ¿Rojo antes? |
|----|---------|--------------|
| AC-01 | *(no unitario en el borde de iOS)* — el lado común ya lo cubre `FLE-90 sign out clears the platform social state`, que prueba que `signOut()` invoca el puerto; lo que este ticket añade está **debajo** del puerto, en el `actual` de iOS | n/a |
| AC-02 | *(no unitario)* — inspección del `actual` | n/a |
| AC-03 | *(no unitario)* — inspección del `actual` | n/a |
| AC-04 | *(no unitario)* — lectura del KDoc | n/a |

> **Este ticket no añade tests y hay que decirlo.** Todo lo que cambia vive por debajo de
> `SocialSignInStateCleaner`, que es justo la frontera donde `commonTest` deja de alcanzar: el
> `actual` de iOS y un handler de Swift. Añadir un test que compruebe que el puerto se llama sería
> duplicar el de FLE-90 y fingir cobertura nueva.

## Notas no funcionales

**API pública que se añade**: `GoogleSignInProviderIOS.Companion.signOutHandler`. Nada se cambia ni
se borra.

**Pendiente de Mac**: AC-01 es un smoke manual — entrar con Google, cerrar sesión y comprobar que el
selector de cuentas reaparece en vez de reutilizar la anterior.
