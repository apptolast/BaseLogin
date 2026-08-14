# Spec 003: Apple Sign-In listo para producción en iOS

> Rama: `feature/003-apple-signin-production` · Proyecto: `BaseLogin` · Estado: implementado
> **Escrito a posteriori.** El trabajo se hizo como fix directo y el spec se redacta después para que
> quede en el carril del repo. No hay ticket FLE: salió de una auditoría de la integración iOS.
> Continúa el spec 002 (FLE-90), que dejó el lado Kotlin de Apple resuelto y probado.

## Contexto y objetivo

FLE-90 implementó y probó el lado común de Apple: el nonce viaja, el `displayName` se persiste, el
formato de dos segmentos sigue funcionando. Cinco tests en verde.

Lo que nadie miró es que **en iOS no había ni una línea de Swift que lo alimentara**. El demo tenía
la entitlement *Sign in with Apple* puesta, la config `AppleSignInConfig()` activada y el botón
pintado, pero `AppleSignInProviderIOS.signInHandler` no se asignaba nunca. Pulsarlo escribía un
warning, resolvía a `null` y mostraba *«Social sign-in cancelled or failed»*.

Y peor para un consumidor: **el KDoc y el README enseñaban Swift que no compila**. Los ejemplos
usaban `AppleSignInProviderIOS.companion.signInHandler`, y `.companion` solo existe para el companion
object de una clase. `AppleSignInProviderIOS` es un `object`, que Kotlin/Native exporta con `.shared`.
El mismo error estaba copiado en los otros cinco providers. Como esos ejemplos **son** el contrato de
integración de la librería, esto pesa más que el botón muerto del demo.

## Alcance

**Dentro:**

- Implementación de referencia de Apple en el demo, con las propiedades que la hacen apta para
  producción (nonce, retención, completion única, nombre).
- Corregir `.companion` → `.shared` en el KDoc de los seis providers y en las siete apariciones del
  README.
- Documentar el segmento `|||displayName|||` de FLE-90, que ninguna guía de host mencionaba: la
  funcionalidad existía y ningún consumidor podía descubrirla.
- `Logger.ios.kt`: `NSLog` recibía el mensaje ya interpolado como *format string*.

**Fuera:**

- Cambiar la API pública o la lógica de `AppleSignInProviderIOS`. Solo cambia su KDoc.
- Tocar `commonMain`: el parseo del token y sus cinco tests de FLE-90 quedan intactos.
- Revocación del token al borrar cuenta (App Review 5.1.1(v)): es el **spec 005**.
- `AppleSignInConfig.scopes`, que en iOS no hace nada: es el **spec 007**.
- Los otros cinco providers del demo: **spec 004**.

## Diseño

### Las cuatro formas de romper Apple Sign-In que solo se ven en runtime

Cada una es un fallo silencioso, y las cuatro están cerradas en `AppleSignInCoordinator`:

| Fallo | Síntoma | Cómo se cierra |
|---|---|---|
| Nonce predecible o perdido | Firebase acepta un token reproducido de otra app | `SecRandomCopyBytes` comprobando el `OSStatus`, abortando en vez de caer a un valor adivinable; si el nonce falta en el delegate, se rechaza el token en lugar de firmar sin protección |
| `ASAuthorizationController` liberado | La hoja no aparece nunca | Referencia fuerte en el coordinador: el sistema no lo retiene |
| Completion sin llamar, o llamada dos veces | El botón gira para siempre, o la app casca | Un único `finish(_:)` que limpia estado y llama exactamente una vez, cancelación incluida |
| `fullName` no enviado | El perfil se queda sin nombre **para siempre** | `PersonNameComponentsFormatter` → segmento `\|\|\|displayName\|\|\|` |

El último merece detalle: Apple manda `fullName` **solo en la primera autorización** de cada usuario.
Si no se envía entonces, no vuelve: ni reinstalar la app lo recupera, hay que revocarla en
*Ajustes → Cuenta de Apple → Iniciar sesión con Apple*. Es exactamente lo que `applyPendingDisplayName`
persiste, y no se estaba alimentando.

### Por qué `.companion` no compila

Kotlin/Native exporta un `object` como una clase con una propiedad de clase `shared`. Un
`companion object` **de una clase** se exporta además como `companion` sobre la clase que lo contiene.
`GoogleSignInProviderIOS` es una clase con companion, así que su `.companion` era correcto y por eso
el único ejemplo que alguien había ejecutado —el del demo— funcionaba. Los otros seis nunca se
ejecutaron.

### `NSLog`

El primer argumento de `NSLog` **es** el format string. Pasar uno ya interpolado convierte cualquier
`%` del mensaje en un especificador, y los mensajes de error de OAuth llevan URLs percent-encoded
(`%20`). Lee memoria arbitraria de los varargs. Va justo en la ruta de error de Apple.

## Criterios de aceptación (Gherkin)

```gherkin
Scenario [AC-01]: Apple firma de verdad en el demo
  Given el demo con la entitlement de Sign in with Apple
  When  el usuario pulsa el botón de Apple
  Then  aparece la hoja nativa de AuthenticationServices
   And  al completarla la app llega a la pantalla de perfil

Scenario [AC-02]: El nonce protege contra replay
  Given una petición de Apple Sign-In
  When  se construye el ASAuthorizationRequest
  Then  request.nonce es el SHA-256 del nonce crudo
   And  el nonce crudo viaja a Kotlin en el segmento |||rawNonce|||
   And  si SecRandomCopyBytes falla, la petición se aborta en vez de usar un valor predecible

Scenario [AC-03]: El nombre se persiste en la primera autorización
  Given un usuario que nunca ha autorizado esta app
  When  Apple devuelve fullName y el usuario de Firebase viene sin displayName
  Then  el token incluye el segmento |||displayName|||
   And  el perfil de Firebase queda con ese nombre

Scenario [AC-04]: Cancelar no deja el botón girando
  Given la hoja de Apple abierta
  When  el usuario la cancela
  Then  la completion se llama una sola vez con null
   And  la corrutina reanuda y el botón vuelve a estar disponible

Scenario [AC-05]: Los ejemplos de Swift compilan
  Given el KDoc de los seis providers y el README
  When  se buscan accesos desde Swift a los objetos Kotlin
  Then  ninguno usa .companion sobre un object
   And  solo GoogleSignInProviderIOS, que es clase con companion, lo usa

Scenario [AC-06]: El log no interpreta el mensaje como formato
  Given un mensaje de error con un carácter %
  When  Logger lo escribe
  Then  NSLog recibe "%@" como formato y el mensaje como argumento

Scenario [AC-07]: La API pública no se mueve
  Given la rama de esta feature
  When  se comparan las firmas públicas de custom-login
  Then  no hay ningún cambio: solo KDoc y Logger
```

## Trazabilidad

| AC | Test(s) | ¿Rojo antes? |
|----|---------|--------------|
| AC-01 | *(no unitario)* — smoke manual en dispositivo | n/a — requiere Apple ID real |
| AC-02 | *(no unitario)* — inspección de `AppleSignInCoordinator`; el lado Kotlin ya lo cubre `FLE-90 apple sign in forwards id token and raw nonce` | n/a en Swift |
| AC-03 | `FLE-90 apple sign in propagates the display name` (ya existente, verde) + smoke con Apple ID nueva | ya estaba verde: lo que faltaba era el emisor |
| AC-04 | *(no unitario)* — inspección + smoke manual | n/a |
| AC-05 | *(no unitario)* — `grep -rn "\.companion\." custom-login/src/iosMain README.md` | n/a |
| AC-06 | *(no unitario)* — inspección de `Logger.ios.kt` | n/a |
| AC-07 | `:custom-login:testDebugUnitTest` en verde sin tocar tests | n/a |

> Ningún AC de este spec es verificable con un test unitario nuevo: el código añadido es Swift, y el
> Kotlin que cambia es KDoc y una llamada a `NSLog`. Se dice explícitamente en vez de inventar tests
> de relleno, igual que en el spec 001.

## Notas no funcionales

**Plataformas**: solo iOS.

**Compatibilidad**: el segmento `|||displayName|||` es **añadido**, nunca sustituto. Un host ya
integrado contra el formato de dos segmentos sigue funcionando sin tocar nada.

**Requisitos externos**: capacidad *Sign in with Apple* en el App ID, entitlement en la app y provider
de Apple habilitado en la consola de Firebase para este bundle id.

**Pendiente**: el Swift se escribió en Windows y no es compilable ahí. AC-01, AC-03 y AC-04 quedan
abiertos hasta el smoke en Mac. `ktlintCheck` y `testDebugUnitTest`, en verde.
