# Spec 013: Los errores de autenticación dicen qué ha pasado

> Rama: `fix/013-descriptive-auth-errors` · Proyecto: `BaseLogin` (`:baselogin`) · Estado: draft
> Sale de `develop` (versión publicada en el árbol: `2.0.1`). Sin ticket FLE todavía: nace de un
> reporte de usuario en una app consumidora ("los mensajes de error no son descriptivos").
> El spec es el mecanismo anti-deriva: debe ser autosuficiente (releíble al inicio de cada fase).

## Contexto y objetivo

El usuario final de las apps que consumen la librería ve a menudo mensajes genéricos ("Se produjo un
error inesperado", "Esta operación no está permitida") en situaciones que la librería **sí podría
distinguir**. La UI solo usa el **tipo** de `AuthError` (`getString(error.toStringRes())`), así que
cualquier error mal clasificado llega al usuario como texto genérico.

Flujo actual, verificado en el árbol:

```
GitLive SDK
  -> GitLiveFirebaseAuthGateway.runGateway()        // throw FirebaseAuthFailure(e.message, e)
  -> FirebaseAuthProvider.runAuth / toAuthError()   // mapFirebaseErrorMessage(message)
  -> mapFirebaseErrorMessage (data/DataMapper.kt)   // busca códigos como SUBCADENA del mensaje
  -> AuthResult.Failure(AuthError.X)
  -> XxxViewModel -> XxxEffect.ShowError(error)
  -> XxxScreen: getString(error.toStringRes())      // presentation/util/AuthErrorExt.kt
```

### Defectos encontrados

**D1. Android pierde casi todos los códigos.** En GitLive `2.6.0` (versión del catálogo) el tipo
`dev.gitlive.firebase.auth.FirebaseAuthException` es en Android `actual typealias` a
`com.google.firebase.auth.FirebaseAuthException`, y expone el código con
`actual val FirebaseAuthException.code: String? get() = errorCode`. Pero el **`message` nativo de
Android es texto humano sin código**:

| Situación | `errorCode` (Android) | `message` (Android) |
|---|---|---|
| Email ya registrado | `ERROR_EMAIL_ALREADY_IN_USE` | "The email address is already in use by another account." |
| Email mal formado | `ERROR_INVALID_EMAIL` | "The email address is badly formatted." |
| Contraseña débil | `ERROR_WEAK_PASSWORD` | "The given password is invalid. [ Password should be at least 6 characters ]" |
| Usuario inexistente | `ERROR_USER_NOT_FOUND` | "There is no user record corresponding to this identifier..." |
| Cuenta deshabilitada | `ERROR_USER_DISABLED` | "The user account has been disabled by an administrator." |
| Bloqueo por abuso | *(no es `FirebaseAuthException`)* | "We have blocked all requests from this device due to unusual activity. Try again later." |

`runGateway` descarta el código y `mapFirebaseErrorMessage` no encuentra nada en esos mensajes:
**en Android casi todo termina en `AuthError.Unknown`**. En iOS funciona por casualidad: GitLive
construye el mensaje con `NSError.toString()`, que incluye `FIRAuthErrorUserInfoNameKey=ERROR_…`, y el
código va como número (`code.toString()`, p.ej. `"17007"`). `DataMapperTest` solo usa cadenas
sintéticas, por eso nunca lo detectó.

Nota: el bloqueo por abuso en Android es `FirebaseTooManyRequestsException` (hermana de
`FirebaseAuthException`, ver engram #111), así que no trae `code` y su mensaje no contiene
`too-many-requests`: hoy también cae en `Unknown`.

**D2. Cancelar un login social se muestra como "operación no permitida".** En
`FirebaseAuthProvider.signInWithOAuth` y `reauthenticate`, un token social `null` (cancelación del
usuario **o** fallo en plataforma) produce `AuthError.OperationNotAllowed(SOCIAL_CANCELLED)` y la
pantalla enseña "Esta operación no está permitida". Lo mismo para un proveedor sin credencial
construible (`unsupported(provider)`) y para Magic Link sin `MagicLinkConfig` en
`AuthRepositoryImpl.sendMagicLink`.

**D3. Faltan variantes para errores frecuentes.** No hay tipo para
`ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL`, `CREDENTIAL_ALREADY_IN_USE`, `REQUIRES_RECENT_LOGIN`,
`SESSION_EXPIRED`/`CODE_EXPIRED` del SMS, `QUOTA_EXCEEDED` ni `WEB_CONTEXT_CANCELLED`.

**D4. Teléfono.** `Platform.android.kt` (`onVerificationFailed`, fallo de auto-verificación y
`verifyPhoneCode`) crea `AuthError.Unknown(e.message)` sin pasar por ningún mapper. En iOS
`PhoneAuthProviderIOS` solo recibe `verificationId?`/`userId?` desde Swift: el error se pierde en la
frontera (fuera de alcance, ver D-5).

**D5. Textos poco accionables** en `composeResources/values*/strings.xml` (9 locales). Además hay
tres claves sin ningún uso: `auth_error_registration_failed`, `auth_error_phone_unexpected`,
`auth_error_phone_verification_failed`.

**D6. Diagnóstico ciego.** `runAuth` no loguea nada; `mapFirebaseErrorMessage` crea
`Unknown(errorMessage)` sin `cause`.

**D7. Un resultado positivo se muestra como error.** `RegisterViewModel` emite
`RegisterEffect.ShowError(AuthError.RequiresEmailVerification())` cuando el alta ha ido bien y solo
falta verificar el correo.

**D8. En iOS, loguear tumba la app (medido en Paparcar, 2026-09-13).** `Logger.ios.kt` hace
`NSLog("%@", "[$tag] W: $message")`. `NSLog` es variádica y Kotlin/Native pasa un `String` en
posición variádica como **cadena C** (`char*`: en el desensamblado aparecen
`CValuesRef.getPointer` / `CPointer-box` / `ArenaBase.clearImpl` en `Logger.ios.kt:13`), no como
`NSString`. `%@` espera un objeto, así que Foundation hace `objc_opt_respondsToSelector` sobre los bytes
del texto → `EXC_BAD_ACCESS (code=1)` con una dirección que es ASCII (`0x6f6674616c5076` = `…Platfo…`,
de `[Platform] W:`). Lo publicado en `2.0.1` lleva exactamente esto. Reproducción real: login correcto
→ el bootstrap de Paparcar falla → `signOut()` → `clearSocialSignInState()` → `Logger.w("Platform",
"signOutHandler not configured…")` → crash. **Toda** llamada a `Logger.d/w/e` en iOS se cae igual. El
comentario del fichero presenta `"%@"` como la corrección de los `%` del mensaje; el remedio es
correcto en intención, pero el argumento tiene que ser un objeto Objective-C.

Consecuencia directa para este spec: el punto 8 (`Logger.w` en **cada** fallo de `runAuth`)
convertiría cada error de autenticación en iOS en un crash. D8 no es opcional: es prerrequisito.

**D9. Google en Android: el fallo del selector también se volvería silencioso.**
`GoogleSignInProviderAndroid.signIn()` devuelve `null` en tres casos distintos: cancelación
(`GetCredentialCancellationException`), fallo real (`GetCredentialException` del segundo intento,
`IllegalStateException`) y "sin actividad" (`ActivityHolder`). El punto 6 solo convierte en
`SocialTokenResult.Failed` los fallos de `WebOAuthProviderAndroid`; con el punto 4 (`null →
SignInCancelled`, sin mensaje) **el caso más frecuente de configuración rota — SHA-1 no registrado,
`serverClientId` de otro proyecto, API key restringida — pasaría de "Esta operación no está permitida"
a no mostrar nada**. Es el proveedor más usado, así que la regresión sería la más visible.
Lo mismo ocurre en iOS con cualquier fallo del handler Swift de Google/Apple (`(String?) -> Unit`,
D-5): hoy se ve "no permitido", tras este spec no se verá nada.

**D10. Los fallos de configuración del desarrollador no tienen tipo ni rastro.** Caso real en
Paparcar (2026-09-13): Google devolvió el token, pero la API key de Android estaba restringida y
`signInWithCredential` falló; el usuario vio "Se produjo un error inesperado" y el diagnóstico
requirió capturar logcat a mano, porque la librería no deja rastro (D6). Estos errores
(`ERROR_APP_NOT_AUTHORIZED` / 17028, `ERROR_INVALID_API_KEY` / 17023, `CONFIGURATION_NOT_FOUND`,
`OPERATION_NOT_ALLOWED` por proveedor desactivado, "Requests from this Android client application …
are blocked") no son accionables para el usuario final, pero **sí** para quien integra: su sitio es un
tipo `ProviderNotConfigured` en pantalla y el código exacto en el log.

**Objetivo:** que cada fallo que Firebase clasifica llegue a la UI con su tipo correcto en Android e
iOS, que cancelar no sea un error, y que los textos digan al usuario qué puede hacer.

## Alcance

**Dentro:**

0. **`Logger.ios.kt` deja de pasar `String` a la vararg de `NSLog` (D8, prerrequisito del punto 8).**
   Un único punto de salida que entrega un objeto Objective-C al `%@`:
   `NSLog("%@", NSString.create(string = line))` (o `line as NSString`; decidir en `/plan` cuál
   genera un `id` real, verificándolo en simulador). Se mantiene el formato fijo `"%@"`: un `%` del
   mensaje nunca es especificador. Aplica a `d`, `w` y `e`.
1. **`FirebaseAuthFailure` gana `code: String? = null`** (aditivo, en `data/firebase/`).
   `GitLiveFirebaseAuthGateway.runGateway` captura `dev.gitlive.firebase.auth.FirebaseAuthException`
   y pasa `e.code`; `FirebaseNetworkException` y `FirebaseTooManyRequestsException` se traducen a un
   código sintético (`ERROR_NETWORK_REQUEST_FAILED`, `ERROR_TOO_MANY_REQUESTS`). El adaptador sigue
   siendo el **único** fichero de `commonMain` que importa `dev.gitlive.*`.
2. **Nueva función pura `internal` de mapeo por código** (p.ej. `mapFirebaseError(code, message,
   cause)` en `data/DataMapper.kt`). Normaliza las cuatro familias y compara por **igualdad**, no por
   subcadena:
   - Android nativo: `ERROR_EMAIL_ALREADY_IN_USE`
   - Web: `auth/email-already-in-use` y `email-already-in-use`
   - REST: `EMAIL_EXISTS`, `CREDENTIAL_TOO_OLD_LOGIN_AGAIN`, …
   - iOS numérico: `17007`

   Con `code` nulo o no reconocido, cae a `mapFirebaseErrorMessage(message)` (comportamiento actual,
   que se conserva). `Unknown` conserva `message` original y `cause`.
3. **Siete variantes nuevas de `AuthError`**, cada una con `toStringRes()` y clave en los 9 locales:

   | Variante | Android / Web | iOS | REST (supuesto, validar en `/plan`) |
   |---|---|---|---|
   | `AccountExistsWithDifferentCredential` | `ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL` / `account-exists-with-different-credential` | 17012 | — |
   | `CredentialAlreadyInUse` | `ERROR_CREDENTIAL_ALREADY_IN_USE` / `credential-already-in-use` | 17025 | `FEDERATED_USER_ID_ALREADY_LINKED` |
   | `RequiresRecentLogin` | `ERROR_REQUIRES_RECENT_LOGIN` / `requires-recent-login` | 17014 | `CREDENTIAL_TOO_OLD_LOGIN_AGAIN` |
   | `VerificationCodeExpired` | `ERROR_SESSION_EXPIRED` / `code-expired` | 17051 | `SESSION_EXPIRED` |
   | `QuotaExceeded` | `ERROR_QUOTA_EXCEEDED` / `quota-exceeded` | 17052 | `QUOTA_EXCEEDED` |
   | `SignInCancelled` | `ERROR_WEB_CONTEXT_CANCELED` y `…_CANCELLED` / `web-context-cancelled`, `popup-closed-by-user` | 17058 | — |
   | `ProviderNotConfigured` | *(no viene del SDK; lo emite la librería)* | — | — |

   Tabla iOS completa del mapeo numérico (verificada contra `AuthErrors.swift` de
   `firebase-ios-sdk`): 17004 `InvalidCredentials`, 17009 `InvalidCredentials`, 17011 `UserNotFound`,
   17007 `EmailAlreadyInUse`, 17026 `WeakPassword`, 17008 `InvalidEmail`, 17010 `TooManyRequests`,
   17005 `UserDisabled`, 17006 `OperationNotAllowed`, 17020 `NetworkError`, 17042
   `PhoneNumberInvalid`, 17044 `InvalidVerificationCode`, 17029/17030 `InvalidResetCode`, más las
   cinco nuevas de la tabla anterior.

   `mapFirebaseErrorMessage` aprende también los nombres `ERROR_*` de las variantes nuevas (el mensaje
   de iOS los contiene), colocados **antes** de la regla genérica `NETWORK`.
4. **Cancelar no es un error.** Token social `null` → `AuthError.SignInCancelled`. `LoginViewModel`,
   `RegisterViewModel` y `ReauthViewModel` **no** emiten `ShowError` ni rellenan `authError` para
   `SignInCancelled`; solo limpian el estado de carga.
5. **Mal configurado no es "no permitido".** Proveedor sin credencial construible y Magic Link sin
   `MagicLinkConfig` → `AuthError.ProviderNotConfigured`. (`Credentials.RefreshToken` en
   `reauthenticate` es un error de programación y **se queda** en `OperationNotAllowed`.)
6. **Fallo social distinguible de cancelación (D-2: incluido).** Nuevo caso
   `SocialTokenResult.Failed(code: String?, message: String)`. Android lo produce en
   `WebOAuthProviderAndroid` (`addOnFailureListener`, que hoy devuelve `null`) con el `errorCode` de
   la excepción; `FirebaseAuthProvider` lo pasa por el mapper de código. Sin esto, el punto 4 **silencia**
   fallos reales: Apple/GitHub/Microsoft en Android pasan por `startActivityForSignInWithProvider`, que
   es precisamente donde aparece `ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL`.
   **Google en Android también (D9)**: `GoogleSignInProviderAndroid` distingue sus tres salidas —
   `GetCredentialCancellationException` → cancelación (`null`); `NoCredentialException` tras el
   segundo intento, `GetCredentialException` e `IllegalStateException` → `Failed(type, message)`
   con `e.type` como código; sin actividad → `Failed`. Sin esto el punto 4 silencia el fallo social
   más común.
   **iOS (handlers Swift, D-5 fuera)**: el `null` sigue sin distinguir cancelación de fallo. Ver D-9.
7. **Teléfono Android** (`Platform.android.kt`): los tres puntos que crean `Unknown(e.message)` a
   partir de una excepción pasan por la función común de mapeo (leyendo `errorCode` si la excepción es
   `FirebaseAuthException`). Sin test en `commonTest` (es `androidMain`); basta con que use la función
   común, que sí está cubierta.
8. **Logging en `runAuth`**: `Logger.w` con `code` + **clase** de la excepción (y el `AuthError`
   resultante) de cada fallo. **No** el `message`: en iOS es `NSError.toString()`, que incluye el
   `userInfo` completo, y Firebase iOS mete `FIRAuthErrorUserInfoEmailKey` en errores como
   `accountExistsWithDifferentCredential` (confirmar en `/plan` sobre `AuthErrors.swift`, D-8). El
   `message` solo se loguea cuando `code` es nulo y el resultado es `Unknown` —el único caso en el
   que es la única pista— y truncado. Nunca credenciales, tokens, emails ni teléfonos. Depende del
   punto 0.
9. **`RegisterEffect.EmailVerificationRequired`** (`data object`, nombre en línea con
   `NavigateToHome`/`ShowError`): lo emite `RegisterViewModel` para
   `AuthResult.RequiresEmailVerification`. `RegisterScreen` lo muestra con la clave existente
   `auth_error_requires_email_verification` (no hace falta clave nueva). No hay nada reutilizable en
   slots para esto; el tratamiento visual lo decide `/design-check`.
10. **Textos**: se reescriben en EN (base) y ES, y se traducen en `de, fr, it, nl, pl, pt, ro`, las
    claves de la tabla de "Textos propuestos". Siete claves nuevas en los 9 locales.
11. **Errores de configuración → `ProviderNotConfigured` (D10).** El mapper por código envía a esa
    variante `ERROR_APP_NOT_AUTHORIZED` (17028), `ERROR_INVALID_API_KEY` (17023),
    `CONFIGURATION_NOT_FOUND` y el `ERROR_INTERNAL_ERROR` cuyo mensaje contenga
    `are blocked` / `API key not valid` (restricción de API key). `OPERATION_NOT_ALLOWED` sigue
    siendo `OperationNotAllowed` (su texto reescrito ya dice "no disponible"). Supuestos de grafía y
    de clase de excepción a verificar en `/plan` provocando cada fallo en la demo (D-8).

**Fuera:**

- **iOS teléfono y handlers sociales Swift**: pasar el `NSError` desde Swift cambia la firma de
  `sendCodeHandler`/`verifyCodeHandler` y de los handlers sociales, que son contrato con el Swift de
  cada consumidor. Ver D-5.
- **Operaciones `Result<Unit>`** (`deleteAccount`, `updateEmail`, `updatePassword`,
  `sendEmailVerification`, `signOut`): siguen devolviendo la excepción cruda. Por eso
  `RequiresRecentLogin` solo será alcanzable en flujos `AuthResult` en este spec. Ver D-4.
- Mensajes con el detalle de Firebase en pantalla: la UI sigue mostrando solo el texto por tipo.
- Borrar las 3 claves muertas (ver D-3: `Res` es **público**).
- Validaciones de formulario (`validation_*`) y su cableado.
- `ForgotPassword`, `ResetPassword`, `MagicLink` y `PhoneAuth` ViewModels: no cambian (se benefician
  del mapeo sin tocarlos).
- Subir GitLive o el BOM de Firebase.
- Publicar la release.

## Conocimiento reutilizable

- **engram #111** (Fledge, "GitLive Firebase Auth: trampas verificadas") — *se reutiliza tal cual*:
  `FirebaseNetworkException`/`FirebaseTooManyRequestsException` son **hermanas** de
  `FirebaseAuthException` en Android; capturar solo la de auth no basta. Es la razón del código
  sintético del punto 1.
- **Spec 002** (`specs/002-firebase-auth-gateway`) — *se reutiliza tal cual*: el puerto
  `FirebaseAuthGateway` + `FakeFirebaseAuthGateway` (`failWith`) permite probar el mapeo extremo a
  extremo del provider desde `commonTest`. *Se adapta*: su corrección de `/plan` decidió "solo
  transportar el `message`". Este spec revierte parcialmente esa decisión porque en Android el mensaje
  no contiene el código; el `message` se sigue transportando intacto como fallback.
- **Spec 012** (`specs/012-strings-nine-locales`) — *se reutiliza tal cual*:
  `StringResourcesLocaleParityTest` (`androidUnitTest`) ya garantiza paridad de claves en los 9
  locales, cero `\'` y placeholders. *Se adapta*: su `EXPECTED_KEY_COUNT = 92` pasa a 99.
- **kmp-recipes `ui/i18n-compose-resources`** — *se reutiliza*: estructura `values*/strings.xml`,
  `stringResource`/`getString`. *Se adapta*: aquí el base (`values/`) es inglés, no español.
- **kmp-recipes `testing/kotlin-test-turbine-fakes`** — *se adapta*: Given/When/Then y fakes a mano;
  este repo **no** usa Turbine (efectos recogidos con `launch` + lista, como en
  `RegisterViewModelTest`). Se mantiene el patrón existente.
- **kmp-recipes `auth/login-library-slots`** — contrato MVI `Action/Effect/UiState`: el efecto nuevo
  del punto 9 sigue ese molde.
- Códigos numéricos iOS verificados contra
  `firebase-ios-sdk/FirebaseAuth/Sources/Swift/Utilities/AuthErrors.swift`.

## Textos propuestos

Base EN y ES. Los otros siete locales se traducen en `/implement` con las invariantes del spec 012
(apóstrofo crudo, sin plurales).

| Clave | EN (base) | ES |
|---|---|---|
| `auth_error_unknown` *(reescrita)* | We couldn't complete the operation. Please try again. | No hemos podido completar la operación. Inténtalo de nuevo. |
| `auth_error_operation_not_allowed` *(reescrita)* | This sign-in method isn't available right now. | Este método de acceso no está disponible en este momento. |
| `auth_error_weak_password` *(reescrita)* | The password is too weak. Try a longer password that is harder to guess. | La contraseña es demasiado débil. Prueba con una más larga y difícil de adivinar. |
| `auth_error_user_disabled` *(reescrita)* | This account is disabled. Please contact support. | Esta cuenta está desactivada. Contacta con soporte. |
| `auth_error_invalid_email` *(reescrita)* | Enter a valid email address. | Introduce un correo electrónico válido. |
| `auth_error_invalid_otp` *(reescrita)* | The code is incorrect. Check it or request a new one. | El código no es correcto. Revísalo o solicita uno nuevo. |
| `auth_error_account_exists_with_different_credential` *(nueva)* | An account already exists with this email. Sign in with the method you used before. | Ya existe una cuenta con este correo. Inicia sesión con el método que usaste la primera vez. |
| `auth_error_credential_already_in_use` *(nueva)* | This account is already linked to another user. | Esta cuenta ya está vinculada a otro usuario. |
| `auth_error_requires_recent_login` *(nueva)* | For your security, sign in again to continue. | Por seguridad, vuelve a iniciar sesión para continuar. |
| `auth_error_verification_code_expired` *(nueva)* | The code has expired. Request a new one. | El código ha caducado. Solicita uno nuevo. |
| `auth_error_quota_exceeded` *(nueva)* | The sending limit has been reached. Please try again later. | Se ha alcanzado el límite de envíos. Inténtalo más tarde. |
| `auth_error_sign_in_cancelled` *(nueva)* | Sign-in was cancelled. | Has cancelado el inicio de sesión. |
| `auth_error_provider_not_configured` *(nueva)* | This sign-in method isn't set up. | Este método de acceso no está configurado. |

> `auth_error_sign_in_cancelled` no la muestran las pantallas de la librería (AC-12..14), pero existe
> para que `toStringRes()` sea total y los consumidores con UI propia puedan usarla.
> El texto de contraseña débil no cita un mínimo: la librería no conoce la política de contraseñas
> del proyecto de Firebase ni la del consumidor. Ver D-6.

## Criterios de aceptación (Gherkin)

```gherkin
# ---------- Mapeo por código (función pura, commonTest) ----------

Scenario [AC-01]: Los códigos nativos de Android se clasifican aunque el mensaje no los contenga
  Given un fallo con code y un message humano de Android sin código
  When  se mapea con la función de mapeo por código
  Then  se obtiene el AuthError de cada fila:
    | code                                         | message                                                   | AuthError                            |
    | ERROR_EMAIL_ALREADY_IN_USE                   | The email address is already in use by another account.  | EmailAlreadyInUse                    |
    | ERROR_INVALID_EMAIL                          | The email address is badly formatted.                     | InvalidEmail                         |
    | ERROR_WEAK_PASSWORD                          | The given password is invalid. [ Password should be ... ] | WeakPassword                         |
    | ERROR_USER_NOT_FOUND                         | There is no user record corresponding to this identifier. | UserNotFound                         |
    | ERROR_USER_DISABLED                          | The user account has been disabled by an administrator.   | UserDisabled                         |
    | ERROR_INVALID_CREDENTIAL                     | The supplied auth credential is incorrect.                | InvalidCredentials                   |
    | ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL | An account already exists with the same email address.  | AccountExistsWithDifferentCredential |
    | ERROR_CREDENTIAL_ALREADY_IN_USE              | This credential is already associated with another user.  | CredentialAlreadyInUse               |
    | ERROR_REQUIRES_RECENT_LOGIN                  | This operation is sensitive and requires recent auth.     | RequiresRecentLogin                  |
    | ERROR_SESSION_EXPIRED                        | The sms code has expired.                                 | VerificationCodeExpired              |
    | ERROR_QUOTA_EXCEEDED                         | The sms quota for this project has been exceeded.         | QuotaExceeded                        |
    | ERROR_WEB_CONTEXT_CANCELED                   | The web operation was canceled by the user.               | SignInCancelled                      |

Scenario [AC-02]: Los códigos numéricos de iOS se clasifican
  Given un fallo cuyo code es el número de FIRAuthErrorCode
  When  se mapea con la función de mapeo por código
  Then  17004 y 17009 dan InvalidCredentials, 17011 UserNotFound, 17007 EmailAlreadyInUse,
        17026 WeakPassword, 17008 InvalidEmail, 17010 TooManyRequests, 17005 UserDisabled,
        17006 OperationNotAllowed, 17020 NetworkError, 17042 PhoneNumberInvalid,
        17044 InvalidVerificationCode, 17029 y 17030 InvalidResetCode,
        17012 AccountExistsWithDifferentCredential, 17025 CredentialAlreadyInUse,
        17014 RequiresRecentLogin, 17051 VerificationCodeExpired, 17052 QuotaExceeded
        y 17058 SignInCancelled

Scenario [AC-03]: Los formatos web y REST se normalizan al mismo tipo
  Given los codes "auth/requires-recent-login", "requires-recent-login",
        "CREDENTIAL_TOO_OLD_LOGIN_AGAIN" y "ERROR_REQUIRES_RECENT_LOGIN"
  When  se mapea cada uno
  Then  los cuatro producen RequiresRecentLogin
   And  "auth/email-already-in-use", "EMAIL_EXISTS" y "ERROR_EMAIL_ALREADY_IN_USE" producen EmailAlreadyInUse

Scenario [AC-04]: Sin código reconocible se conserva el comportamiento actual
  Given un fallo con code nulo o "ERROR_SOMETHING_NEW" y un message que contiene "wrong-password"
  When  se mapea con la función de mapeo por código
  Then  se obtiene InvalidCredentials, lo mismo que devuelve mapFirebaseErrorMessage
   And  los tests existentes de DataMapperTest siguen en verde sin modificarse

Scenario [AC-05]: El código manda sobre el mensaje
  Given un fallo con code "ERROR_USER_DISABLED" y message "network unreachable"
  When  se mapea
  Then  el resultado es UserDisabled y no NetworkError

Scenario [AC-06]: Unknown conserva el mensaje original y la causa
  Given un fallo sin código reconocible, message "boom" y una causa IllegalStateException
  When  se mapea
  Then  el resultado es AuthError.Unknown con message "boom"
   And  su cause es esa misma IllegalStateException

Scenario [AC-07]: mapFirebaseErrorMessage reconoce los nombres ERROR_* de las variantes nuevas
  Given un message de iOS que contiene "FIRAuthErrorUserInfoNameKey=ERROR_CREDENTIAL_ALREADY_IN_USE"
  When  se mapea solo por mensaje
  Then  el resultado es CredentialAlreadyInUse

# ---------- Provider (FakeFirebaseAuthGateway, commonTest) ----------

Scenario [AC-08]: El provider usa el código que trae el gateway
  Given un gateway falso que lanza FirebaseAuthFailure con code "ERROR_EMAIL_ALREADY_IN_USE"
        y message "The email address is already in use by another account."
  When  el provider ejecuta signUp
  Then  el resultado es AuthResult.Failure(AuthError.EmailAlreadyInUse)

Scenario [AC-09]: Cancelar el login social es SignInCancelled y no toca el SDK
  Given una capa social falsa que devuelve null para Google
  When  el provider ejecuta signIn con Credentials.OAuthToken(Google)
   And  el provider ejecuta reauthenticate con Credentials.OAuthToken(Google)
  Then  ambos resultados son AuthResult.Failure(AuthError.SignInCancelled)
   And  el gateway registra cero interacciones

Scenario [AC-10]: Un fallo social de plataforma conserva su tipo  (D-2: incluido)
  Given una capa social falsa que devuelve SocialTokenResult.Failed con code
        "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" para Apple
  When  el provider ejecuta signIn con Credentials.OAuthToken(Apple)
  Then  el resultado es AuthResult.Failure(AuthError.AccountExistsWithDifferentCredential)
   And  el gateway registra cero interacciones

Scenario [AC-11]: Proveedor o Magic Link sin configurar es ProviderNotConfigured
  Given una capa social falsa que devuelve un Token para IdentityProvider.Custom
  When  el provider ejecuta signIn con Credentials.OAuthToken de ese proveedor
  Then  el resultado es AuthResult.Failure(AuthError.ProviderNotConfigured)
   And  dado un AuthRepositoryImpl con magicLinkConfig nulo, sendMagicLink devuelve
        AuthResult.Failure(AuthError.ProviderNotConfigured)

# ---------- ViewModels (FakeAuthRepository, commonTest) ----------

Scenario [AC-12]: Login no muestra error al cancelar un login social
  Given un repositorio falso cuyo signIn devuelve Failure(SignInCancelled)
  When  el usuario pulsa el botón de Google en LoginViewModel
  Then  no se emite ningún LoginEffect.ShowError
   And  uiState.loadingProvider vuelve a null

Scenario [AC-13]: Registro no muestra error al cancelar un login social
  Given un repositorio falso cuyo signIn devuelve Failure(SignInCancelled)
  When  el usuario lanza SignUpWithOAuth(Google) en RegisterViewModel
  Then  no se emite ningún RegisterEffect.ShowError
   And  uiState.loadingProvider vuelve a null

Scenario [AC-14]: Reautenticación no muestra error al cancelar
  Given un repositorio falso cuyo reauthenticate devuelve Failure(SignInCancelled)
  When  el usuario lanza SubmitOAuth(Google) en ReauthViewModel
  Then  uiState.authError es null
   And  no se emite ningún ReauthEffect.ShowError
   And  uiState.loadingProvider vuelve a null

Scenario [AC-15]: Alta pendiente de verificación no es un error
  Given un repositorio falso cuyo signUp devuelve AuthResult.RequiresEmailVerification
  When  el usuario envía un formulario de registro válido
  Then  se emite RegisterEffect.EmailVerificationRequired
   And  no se emite ningún RegisterEffect.ShowError

Scenario [AC-16]: Los demás fallos sociales se siguen mostrando
  Given un repositorio falso cuyo signIn devuelve Failure(AccountExistsWithDifferentCredential)
  When  el usuario pulsa el botón de Google en LoginViewModel
  Then  se emite LoginEffect.ShowError con ese mismo error

# ---------- Recursos ----------

Scenario [AC-17]: Cada variante de AuthError tiene su propio texto
  Given todas las variantes de AuthError, incluidas las siete nuevas
  When  se llama a toStringRes() sobre cada una
  Then  todas devuelven un StringResource y no hay dos iguales

Scenario [AC-18]: Las claves nuevas existen en los nueve locales
  Given el árbol composeResources de :baselogin
  When  se ejecuta StringResourcesLocaleParityTest
  Then  cada locale declara las mismas 99 claves que el base
   And  ninguno contiene la secuencia \'

# ---------- Verificable por inspección / build / smoke ----------

Scenario [AC-19]: El SDK sigue encerrado en el adaptador
  Given el cambio aplicado
  When  se ejecuta grep -rn "dev.gitlive" baselogin/src/commonTest/
  Then  no hay resultados
   And  en commonMain solo GitLiveFirebaseAuthGateway.kt importa dev.gitlive.*

Scenario [AC-20]: En Android el usuario ve el motivo real
  Given la app demo en un dispositivo Android con locale es
  When  se intenta registrar un email ya registrado
   And  se cancela el selector de cuenta de Google
  Then  el primer caso muestra "Este correo electrónico ya está registrado"
   And  el segundo no muestra ningún mensaje

Scenario [AC-21]: El proyecto construye y pasa el estilo
  When  se ejecutan ktlintCheck, :baselogin:testDebugUnitTest,
        :composeApp:linkDebugFrameworkIosSimulatorArm64 y :composeApp:assembleDebug
  Then  todos terminan en BUILD SUCCESSFUL

# ---------- Añadidos tras la integración en Paparcar (D8–D10) ----------

Scenario [AC-22]: Loguear en iOS no tumba la app  (D8)
  Given la app demo en el simulador de iOS
  When  se ejecuta un signOut sin signOutHandler configurado
   And  se provoca un fallo de autenticación (registro con email existente)
  Then  la consola muestra "[Platform] W: signOutHandler not configured…" y el warning de runAuth
   And  la app no se cae
   And  un mensaje que contiene "%20" o "%@" se imprime literal

Scenario [AC-23]: El fallo del selector de Google en Android se muestra; la cancelación no  (D9)
  Given una capa social falsa que devuelve SocialTokenResult.Failed(code="android.credentials.GetCredentialException.TYPE_NO_CREDENTIAL") para Google
  When  el provider ejecuta signIn con Credentials.OAuthToken(Google)
  Then  el resultado es AuthResult.Failure con un AuthError distinto de SignInCancelled
   And  el gateway registra cero interacciones
   And  en la app demo, con el SHA-1 de depuración retirado del proyecto de Firebase, pulsar Google muestra un error

Scenario [AC-24]: Un fallo de configuración se clasifica como ProviderNotConfigured  (D10)
  Given los codes "ERROR_APP_NOT_AUTHORIZED", "17028", "ERROR_INVALID_API_KEY", "17023" y "CONFIGURATION_NOT_FOUND"
   And  un code "ERROR_INTERNAL_ERROR" con message que contiene "Requests from this Android client application <empty> are blocked"
  When  se mapea cada uno
  Then  todos producen ProviderNotConfigured
   And  "ERROR_INTERNAL_ERROR" con cualquier otro message sigue produciendo Unknown

Scenario [AC-25]: El log de un fallo no contiene PII  (punto 8)
  Given un fallo con code "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" y un message que contiene "FIRAuthErrorUserInfoEmailKey=ana@example.com"
  When  runAuth lo registra
  Then  la línea de log contiene el code y la clase de la excepción
   And  no contiene "ana@example.com"
   And  un token social null para Google produce una línea con "google.com" y "SignInCancelled"
```

## Desglose de tareas (ligero)

- [ ] **T0** — `Logger.ios.kt`: objeto Objective-C en la vararg de `NSLog`. Primero, porque T5 añade
      logs en cada fallo. Validación en simulador (no hay test de `iosMain` que lo cubra). → AC-22
- [ ] **T1** *(/test)* — Esqueletos con `TODO()`: función de mapeo por código, 7 variantes de
      `AuthError` (compilan), `FirebaseAuthFailure.code`, `RegisterEffect.EmailVerificationRequired`,
      `SocialTokenResult.Failed` (D-2: incluido). Claves nuevas en los 9 `strings.xml` con texto provisional
      para que compile `Res`.
- [ ] **T2** *(/test)* — Tests nuevos y **ajuste de tests existentes que cambian de contrato** (el
      hook bloquea tests en `/implement`):
      `RegisterViewModelTest` (`…RequiresEmailVerification result emits ShowError`),
      `AuthRepositoryImplTest` (`sendMagicLink returns OperationNotAllowed…`),
      `AuthErrorExtTest.allErrors` (añadir las 7), `StringResourcesLocaleParityTest`
      (`EXPECTED_KEY_COUNT` 92 → 99), `FakeSocialTokenProvider` (`returnsFailure`).
- [ ] **T3** — Mapeo por código + nombres nuevos en `mapFirebaseErrorMessage` + `cause` en
      `Unknown`. → AC-01..07
- [ ] **T4** — `FirebaseAuthFailure.code` y `runGateway` con `FirebaseAuthException.code` y códigos
      sintéticos de red/abuso. → AC-19, AC-20
- [ ] **T5** — `FirebaseAuthProvider`: `toAuthError` usa el código, `SignInCancelled`,
      `ProviderNotConfigured`, `SocialTokenResult.Failed`, `Logger.w` en `runAuth`;
      `AuthRepositoryImpl.sendMagicLink`. → AC-08..11
- [ ] **T6** — `WebOAuthProviderAndroid` y `GoogleSignInProviderAndroid` devuelven
      `Failed(code, message)` en los fallos que no son cancelación; teléfono en
      `Platform.android.kt` pasa por el mapper común. → AC-10, AC-20, AC-23
- [ ] **T6b** — Códigos de configuración → `ProviderNotConfigured` en el mapper; log sin PII en
      `runAuth` y en tokens sociales `null` (el formateo de la línea como función pura para poder
      testearlo). → AC-24, AC-25
- [ ] **T11** — `version = "3.0.0"` en `baselogin/build.gradle.kts`; coordenadas `3.0.0` y sección
      "Migrating to 3.0.0" en el README. Publicar queda fuera. → notas de compatibilidad
- [ ] **T7** — ViewModels Login/Register/Reauth + `RegisterScreen` maneja el efecto nuevo.
      → AC-12..16
- [ ] **T8** — `toStringRes()` + textos EN/ES + traducciones de/fr/it/nl/pl/pt/ro. → AC-17, AC-18
- [ ] **T9** — Documentación: README (sección de migración: variantes nuevas, cambio de
      `OperationNotAllowed` → `SignInCancelled`/`ProviderNotConfigured`), `CLAUDE.md` (§Ports:
      `FirebaseAuthFailure` ya transporta `code`). → notas de compatibilidad
- [ ] **T10** — Validación completa + smoke Android (AC-20) e iOS (registro con email existente). → AC-21

## Notas no funcionales

**Plataformas**: Android e iOS. El arreglo de D1 es sobre todo Android; iOS gana robustez (código
numérico en vez de depender del texto de `NSError.toString()`).

**Compatibilidad (riesgo dominante; librería publicada)**:

| Cambio | Tipo | Impacto en consumidores |
|---|---|---|
| +7 subclases en `sealed class AuthError` | **rompe fuente** | Cualquier `when (error)` exhaustivo **sin `else`** deja de compilar. Hay que revisar Fledge y Paparcar (D-1). |
| `OperationNotAllowed` ya no se emite para cancelación, proveedor no soportado ni Magic Link sin config | **cambio de comportamiento** | Quien detectase la cancelación con `is OperationNotAllowed` deja de verla. |
| +1 caso en `sealed interface RegisterEffect` | rompe fuente (improbable) | Solo afecta a quien recoja `RegisterViewModel.effect` con `when` exhaustivo propio. |
| +1 caso en `sealed interface SocialTokenResult` (D-2: incluido) | rompe fuente (improbable) | Solo a quien implemente `SocialTokenProvider` o haga `when` sobre el resultado. |
| `FirebaseAuthFailure(message, cause, code = null)` | aditivo en fuente | Conservar el constructor de 2 parámetros explícito para no romper binario en JVM. |
| Fallos de configuración → `ProviderNotConfigured` en vez de `Unknown` | cambio de comportamiento | Solo cambia el texto mostrado; nadie puede depender de `Unknown` para esto. |
| `Logger.ios.kt` sin crash (D8) | arreglo | Ningún consumidor iOS puede depender de un crash. Va dentro de `3.0.0`, sin patch previo (D-10). |
| Fallos sociales reales en iOS dejan de mostrar "no permitido" | **cambio de comportamiento** | Se ven igual que una cancelación (sin mensaje) y quedan en el log (D-9). Aviso explícito en la migración. |
| Textos reescritos en claves existentes | visual | Apps que muestren `Res.string.auth_error_*` ven el texto nuevo. `Res` es público (`publicResClass = true`). |

**Versión: `3.0.0`** (D-1 resuelto). El bump de `version` en `baselogin/build.gradle.kts` y las
coordenadas del README van **en esta rama** (T11); publicar en Maven Central sigue fuera. No existe
`CHANGELOG.md`: la nota va en la sección de migración del README ("Migrating to 3.0.0").

**i18n**: 7 claves nuevas + 6 reescritas, en 9 locales. Invariantes del spec 012 (apóstrofo crudo,
placeholders intactos, sin `<plurals>`). El test de paridad protege la completitud.

**Firebase**: no cambia configuración de consola. Los códigos se leen del SDK; nada se envía.

**Privacidad / logging**: confirmado en `/plan` que el `message` de iOS puede llevar el email
(`FIRAuthErrorUserInfoEmailKey`). `Logger.w` registra solo `code`, clase de la excepción, `AuthError`
resultante y, para tokens sociales `null`, el id del proveedor. Nunca `message`, email, contraseña,
tokens ni teléfono. La línea se construye con una función pura testeable (AC-25).

**Accesibilidad / diseño**: sin cambios de layout. **Gate de diseño N/A** (2026-09-13): no hay
pantallas ni componentes nuevos; `EmailVerificationRequired` reutiliza el `CustomSnackBar` que
`RegisterScreen` ya usa para `ShowError`, y el resto del cambio es texto y lógica.

**Testabilidad**: toda la lógica nueva vive en `commonMain` y se prueba con `FakeFirebaseAuthGateway`,
`FakeSocialTokenProvider`, `FakeAuthRepository` y la función pura. Queda **solo bajo inspección o
smoke**: `GitLiveFirebaseAuthGateway` (lectura real de `code`), `WebOAuthProviderAndroid`, teléfono en
`Platform.android.kt` y `Logger` (`expect object`, no falsificable). Regla verificable:
`commonTest` no importa `dev.gitlive.*` (AC-19).

**ROJO legítimo en `/test`**: esqueletos con `TODO()` junto a los tests, para que falle con
`NotImplementedError` y no por compilación. Los tests existentes que cambian de contrato (T2) se
reescriben en `/test`.

## Decisiones (Gate 1)

Resueltas por el humano el 2026-09-13: **D-1** se decide en `/plan` tras revisar Fledge/Paparcar con `gh`;
**D-2** se incluye `SocialTokenResult.Failed`; **D-3** las claves muertas se mantienen; **D-5** iOS a spec
aparte. D-4, D-6 (texto sin número), D-7 y D-8 quedan según la propuesta de la tabla.

Actualización del humano, 2026-09-13 (tras integrar en Paparcar): **D-1 → `3.0.0`**; **D-9 →
silencio + log**; **D-10 → sin patch**, la corrección completa sale en `3.0.0` y Paparcar se actualiza
cuando esté publicada.

| # | Decisión | Propuesta |
|---|---|---|
| **D-1** | **Versión y ruptura**: añadir subclases a `AuthError` rompe `when` exhaustivos. ¿`3.0.0` o `2.1.0` con nota? | **Resuelto: `3.0.0`.** Ningún consumidor conocido tiene `when` exhaustivo sobre `AuthError` (Fledge y Paparcar verificados en `/plan`), pero hay cambios de comportamiento visibles (cancelación silenciosa, `OperationNotAllowed` que deja de emitirse, textos reescritos) y 7 variantes nuevas en un tipo `sealed` público: se sigue semver estricto. |
| **D-2** | **`SocialTokenResult.Failed`** (punto 6 del alcance, añadido por spec-author). Sin él, `null → SignInCancelled` silencia fallos reales del OAuth web en Android. | Incluirlo. Alternativa mínima: dejar `null` como cancelación y aceptar que esos fallos no muestren nada (solo log), lo que es peor que el "no permitido" actual. |
| **D-3** | **Claves muertas** (`auth_error_registration_failed`, `auth_error_phone_unexpected`, `auth_error_phone_verification_failed`). `Res` es público: borrarlas rompe a quien las use. | No borrarlas en este spec; marcarlas para la próxima major. |
| **D-4** | **`RequiresRecentLogin` en operaciones `Result<Unit>`**: `deleteAccount`/`updateEmail`/`updatePassword` devuelven la excepción cruda, que es donde más aparece este error. | Fuera de este spec. Spec aparte: exponer el `AuthError` en esos fallos (p.ej. excepción pública que lo transporte) sin romper `Result<Unit>`. |
| **D-5** | **iOS teléfono y handlers sociales**: el error de Swift se pierde (`(String?) -> Unit`). | Fuera de alcance. Spec aparte, retrocompatible (p.ej. segmento/handler adicional con código, al estilo del `displayName` de Apple). |
| **D-6** | **"Al menos 6 caracteres"** en `auth_error_weak_password` no refleja `passwordPolicy.minLength` si el consumidor lo sube. | **Resuelto:** texto sin número (la librería no conoce el mínimo real). |
| **D-7** | **Nombres de variantes**: `SignInCancelled` (británico, coherente con `SOCIAL_CANCELLED`) y `VerificationCodeExpired` (evita chocar con `SessionExpired` de sesión de usuario). | Aceptar. |
| **D-9** | **iOS social: `null` = cancelación o fallo** (handlers Swift, D-5 fuera). Con el punto 4, los fallos reales de Google/Apple en iOS pasan de "no permitido" a silencio. | **Resuelto: silencio + log.** `null → SignInCancelled` en ambas plataformas; `FirebaseAuthProvider` registra cada token social `null` con el id del proveedor ("social token null for google.com → SignInCancelled") para que un fallo real en iOS se vea en consola, y la migración del README lo avisa. Requiere D8. |
| **D-10** | **¿Patch `2.0.2` con solo D8** (Logger iOS) antes de este spec? | **Resuelto: no.** La corrección completa sale en `3.0.0`; Paparcar actualiza la integración cuando esté publicada. |
| **D-8** | **Supuestos a verificar en `/plan`**: existencia de `dev.gitlive.firebase.FirebaseTooManyRequestsException`/`FirebaseNetworkException` en GitLive 2.6.0; grafía Android `ERROR_WEB_CONTEXT_CANCELED`; códigos REST de la tabla; que `message` de Firebase no incluya PII. | Verificar sobre fuentes/bytecode reales, no documentación (lección de engram #111). |

## Trazabilidad

| AC | Test(s) que lo cubren | ¿Rojo antes de implementar? |
|----|-----------------------|------------------------------|
| AC-01 | `DataMapperTest` › `013 android codes are classified even when the message lacks them` | Sí — `NotImplementedError` (`mapFirebaseError`) |
| AC-02 | `DataMapperTest` › `013 ios numeric codes are classified` | Sí — `NotImplementedError` |
| AC-03 | `DataMapperTest` › `013 web rest and android spellings of requires recent login normalise to the same type`, `013 web rest and android spellings of email already in use normalise to the same type` | Sí — `NotImplementedError` |
| AC-04 | `DataMapperTest` › `013 without a recognisable code the message fallback still applies` + los tests existentes de `DataMapperTest` sin modificar (siguen en verde) | Sí — `NotImplementedError` (los existentes: verdes, guardia de regresión) |
| AC-05 | `DataMapperTest` › `013 the code wins over the message` | Sí — `NotImplementedError` |
| AC-06 | `DataMapperTest` › `013 unknown keeps the original message and cause` | Sí — `NotImplementedError` |
| AC-07 | `DataMapperTest` › `013 message fallback recognises credential already in use in an ios message`, `013 message fallback recognises the other new error names` | Sí — aserción (hoy da `Unknown`) |
| AC-08 | `FirebaseAuthProviderTest` › `013 the provider classifies by the code the gateway carries` | Sí — aserción (hoy da `Unknown`) |
| AC-09 | `FirebaseAuthProviderTest` › `013 a cancelled social sign in is SignInCancelled for sign in and reauthenticate` (el `FLE-90 cancelled social sign in never touches the gateway` se conserva) | Sí — aserción (hoy da `OperationNotAllowed`) |
| AC-10 | `FirebaseAuthProviderTest` › `013 a failed social sign in keeps the type of its platform code`, `013 a failed reauthentication social flow keeps the type of its platform code` | Sí — `NotImplementedError` (rama `Failed`) |
| AC-11 | `FirebaseAuthProviderTest` › `013 a provider without a buildable credential is ProviderNotConfigured`; `AuthRepositoryImplTest` › `013 sendMagicLink returns ProviderNotConfigured when magicLinkConfig is null` (reescrito) | Sí — aserción (hoy da `OperationNotAllowed`) |
| AC-12 | `LoginViewModelTest` › `013 cancelling a social sign in shows no error and clears the loading provider` | Sí — aserción (emite `ShowError(SignInCancelled)`) |
| AC-13 | `RegisterViewModelTest` › `013 cancelling a social sign up shows no error and clears the loading provider` | Sí — aserción (emite `ShowError`) |
| AC-14 | `ReauthViewModelTest` › `013 cancelling an oauth reauthentication shows no error and clears the loading provider` | Sí — aserción (`authError` no nulo) |
| AC-15 | `RegisterViewModelTest` › `013 sign up pending email verification emits EmailVerificationRequired and no error` (reescrito desde `…RequiresEmailVerification result emits ShowError`) | Sí — aserción (emite `ShowError`) |
| AC-16 | `LoginViewModelTest` › `013 other social failures are still shown with the same error` | **No — verde ya**: guardia de regresión para que el silencio de T7 no se coma otros errores |
| AC-17 | `AuthErrorExtTest` › `all AuthError variants return a non-null StringResource`, `all AuthError variants map to distinct StringResources` (+ `RequiresEmailVerification…`/`Unknown maps to a unique resource`), con las 7 variantes en `allErrors` | Sí — `NotImplementedError` (ramas nuevas de `toStringRes`) |
| AC-18 | `StringResourcesLocaleParityTest` › `AC-01 every locale declares the same keys as the base` (`EXPECTED_KEY_COUNT` 99), `013 the seven new auth error keys exist in every locale` | Sí — aserción (92 claves; faltan las 7) |
| AC-19 | *(no unitario)* — grep | n/a — inspección |
| AC-20 | *(no unitario)* — smoke Android | n/a — smoke |
| AC-21 | *(no unitario)* — build | n/a — build |
| AC-22 | *(no unitario)* — smoke simulador iOS | n/a — smoke |
| AC-23 | `FirebaseAuthProviderTest` › `013 a failed google credential picker is shown and is not a cancellation` + smoke Android | Sí — `NotImplementedError` (rama `Failed`) |
| AC-24 | `DataMapperTest` › `013 developer configuration codes map to ProviderNotConfigured`, `013 an internal error caused by a restricted api key maps to ProviderNotConfigured`, `013 any other internal error stays Unknown` | Sí — `NotImplementedError` |
| AC-25 | `AuthFailureLogTest` › `013 the failure log line carries the code and exception class but not the email`, `013 the failure log line never leaks the message of an Unknown error`, `013 a null google social token is logged with the provider and SignInCancelled` | Sí — `NotImplementedError` (`AuthFailureLog.kt`) |
