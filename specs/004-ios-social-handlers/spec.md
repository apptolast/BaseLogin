# Spec 004: Cablear los handlers sociales que faltaban en el demo de iOS

> Rama: `feature/004-ios-social-handlers` · Proyecto: `BaseLogin` (`iosApp/`) · Estado: implementado
> **Escrito a posteriori.** El trabajo se hizo como fix directo y el spec se redacta después para que
> quede en el carril del repo. No hay ticket FLE: el hallazgo salió de una auditoría de la
> integración iOS, no de Fledge.
> Sale de `feature/003-apple-signin-production` (spec 003), que resolvió Apple.

## Contexto y objetivo

`MainViewController.kt` habilita GitHub, Microsoft, Twitter, Facebook y teléfono, así que la pantalla
de login pinta sus botones. En Swift no había un solo handler para ninguno: cada pulsación entraba en
la rama «handler not configured», registraba un warning, devolvía `null` y el usuario veía
*«Social sign-in cancelled or failed»*.

El demo existe para validar la librería **como la integra un consumidor**. Con cinco de ocho botones
muertos no valida nada: ni siquiera demuestra que el centinela `PLATFORM_AUTH_HANDLED` funcione, que
es la mitad menos ejercitada del contrato Kotlin↔Swift.

## Alcance

**Dentro:**

- Handlers de GitHub, Microsoft, Twitter/X y Facebook sobre el flujo OAuth web de Firebase.
- Los dos handlers del OTP de teléfono.
- El **retorno de la URL**, que es lo que ninguno de los flujos anteriores tenía y sin lo cual todos
  se cuelgan.
- La fontanería de APNs que el teléfono necesita para verificar la app sin reCAPTCHA.
- Documentar ambos requisitos en el README, donde no estaban.

**Fuera:**

- Tocar `:custom-login`. La librería ya expone los seams; lo que faltaba era el lado Swift.
- Magic link: no necesita handler, necesita Universal Links y tiene un `iosBundleId` discrepante.
  Es el **spec 008**.
- Sustituir el flujo OAuth web de Facebook por el SDK nativo de Facebook. Android usa el mismo flujo
  web; cambiarlo aquí rompería la paridad y es una decisión de producto, no de este ticket.

## Diseño

### Quién firma en cada caso

| Provider | Quién completa el login en Firebase | Qué devuelve el handler |
|---|---|---|
| Google | Kotlin, con los tokens que Swift le pasa | `idToken\|\|\|accessToken\|\|\|…` |
| Apple | Kotlin, con el token y el nonce | `idToken\|\|\|rawNonce\|\|\|…` |
| GitHub, Microsoft, Twitter, Facebook | **Swift**, entero | `PLATFORM_AUTH_HANDLED` |
| Teléfono | Swift firma; Kotlin recibe el uid | `verificationID`, luego `uid` |

Los cuatro de OAuth web comparten `FirebaseOAuthCoordinator`, que es el espejo de
`WebOAuthProviderAndroid`. El centinela se lee de cada objeto (`GitHubSignInProviderIOS.shared
.PLATFORM_AUTH_HANDLED`) en vez de repetir la cadena mágica en Swift.

### Lo que no está en el código de login y decide si funciona

1. **Retener el `OAuthProvider`.** El SDK no lo retiene mientras dura el flujo web. En una variable
   local se libera antes del callback: el navegador abre y no vuelve nunca. Se guarda en un
   diccionario por provider id, que además impide que dos flujos se pisen.
2. **Enrutar la URL de vuelta.** Con el ciclo de vida de SwiftUI, `application(_:open:options:)`
   **no se llama** — las URLs llegan a la escena. Es la causa más común de que estos cuatro
   providers se queden colgados. Entra `.onOpenURL` sobre el `WindowGroup`, y un
   `AppDelegate.handle(_:)` que prueba `Auth.auth().canHandle(url)` antes que GoogleSignIn: Firebase
   solo reclama sus propias callbacks, así que ese orden no le quita nada a Google.
3. **Verificación de la app antes del SMS.** Firebase comprueba que la petición viene de esta app con
   un push silencioso de APNs y, si no puede, con una página de reCAPTCHA. `setAPNSToken` y
   `canHandleNotification` son lo que habilita el primer camino.

### Scopes

Replican los de `LoginLibraryConfig` (`user:email`, `email`+`profile`, `email`,
`email`+`public_profile`) para que ambas plataformas pidan lo mismo. Van fijos en Swift porque el
seam solo pasa un `String?` y hoy no lo usa nadie; unificarlo es el **spec 007**, que lo resuelve
para Apple.

## Criterios de aceptación (Gherkin)

```gherkin
Scenario [AC-01]: Los cinco providers tienen handler
  Given el demo arrancado
  When  se inspecciona lo que registra el AppDelegate
  Then  GitHub, Microsoft, Twitter y Facebook tienen signInHandler
   And  PhoneAuthProviderIOS tiene sendCodeHandler y verifyCodeHandler
   And  ninguno de los cinco escribe ya "handler not configured" en el log

Scenario [AC-02]: El flujo OAuth web vuelve a la app
  Given un login de GitHub iniciado desde el demo
  When  el navegador termina y devuelve la URL de callback
  Then  la app la recibe por .onOpenURL
   And  Auth.auth().canHandle(url) la consume antes que GoogleSignIn
   And  el login llega a la pantalla de perfil

Scenario [AC-03]: El provider sobrevive al flujo
  Given un login de Microsoft en curso
  When  el usuario tarda en el navegador
  Then  el OAuthProvider sigue retenido y el callback de credencial se ejecuta

Scenario [AC-04]: Una segunda pulsación no pisa la primera
  Given un login de Twitter ya en vuelo
  When  se pulsa otra vez el mismo botón
  Then  la segunda petición se rechaza con null y la primera sigue viva

Scenario [AC-05]: El centinela no se escribe a mano
  Given el código Swift del demo
  When  se busca la cadena "___PLATFORM_AUTH_COMPLETE___"
  Then  no aparece: se lee de PLATFORM_AUTH_HANDLED de cada provider

Scenario [AC-06]: El teléfono completa los dos saltos
  Given un número en E.164
  When  se pide el código y se introduce el OTP recibido
  Then  la app queda autenticada

Scenario [AC-07]: La librería no se toca
  Given la rama de esta feature
  When  se listan los ficheros cambiados
  Then  ninguno está bajo custom-login/src
```

## Trazabilidad

| AC | Test(s) | ¿Rojo antes? |
|----|---------|--------------|
| AC-01 | *(no unitario)* — inspección del `AppDelegate` | n/a — verificable por build |
| AC-02 | *(no unitario)* — smoke manual en simulador/dispositivo | n/a — requiere navegador real |
| AC-03 | *(no unitario)* — smoke manual | n/a |
| AC-04 | *(no unitario)* — inspección de `FirebaseOAuthCoordinator` | n/a |
| AC-05 | *(no unitario)* — `grep "___PLATFORM_AUTH_COMPLETE___" iosApp/` | n/a |
| AC-06 | *(no unitario)* — smoke manual con SMS real | n/a |
| AC-07 | *(no unitario)* — `git diff --name-only` | n/a |

> Como el spec 001, **esta feature no añade tests automáticos y hay que decirlo en vez de
> inventarlos**: todo el código nuevo es Swift, y este repo no tiene target de test de iOS. Lo que la
> verifica es el build más un smoke manual.

## Notas no funcionales

**Plataformas**: solo iOS. Android no se toca.

**Sin impacto de UI**: no se crea ni modifica ninguna pantalla; los botones ya existían.

**Configuración externa de la que depende, y que no está en el repo:**

- El `REVERSED_CLIENT_ID` del `GoogleService-Info.plist` tiene que estar registrado como esquema de
  URL en `Info.plist`. El que hay es el de GoogleSignIn, que normalmente es el mismo valor, pero el
  plist está en `.gitignore` y no se pudo contrastar.
- Facebook por OAuth genérico exige que la app de Facebook tenga registrada la redirect URI de
  Firebase (`https://<project>.firebaseapp.com/__/auth/handler`). Es el mismo camino que ya usa
  Android.
- El push silencioso del teléfono necesita capacidad **Push Notifications** y clave APNs subida a la
  consola. Sin eso el flujo funciona igual, con el rodeo por reCAPTCHA. **No se añade la entitlement
  `aps-environment`**: sin la capacidad habilitada en el App ID rompería la firma para todos.

**Pendiente de validación en Mac**: el Swift no es compilable desde Windows, donde se escribió. AC-02,
AC-03 y AC-06 quedan abiertos hasta el smoke.
