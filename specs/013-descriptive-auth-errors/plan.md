# Plan 013: Los errores de autenticación dicen qué ha pasado

> Rama: `fix/013-descriptive-auth-errors` · Spec: [`spec.md`](./spec.md) · Estado: draft
> Sale de `develop` (`5d17339`, `version = "2.0.1"`).

---

## Verificaciones hechas antes de planificar (D-1, D-8)

Todo contrastado con fuentes y binarios reales, no con documentación (lección de engram #111).

| Supuesto | Resultado | Fuente |
|---|---|---|
| `dev.gitlive.firebase.FirebaseNetworkException` / `FirebaseTooManyRequestsException` existen en 2.6.0 | **Sí**, en `commonMain` de `firebase-app` como `expect`, hermanas de `FirebaseException`. En Android `FirebaseNetworkException` es `typealias` a la nativa | `firebase-app-2.6.0-sources.jar`, `firebase.kt:112-122` |
| `FirebaseAuthException.code` en iOS | **Numérico**: `actual val code get() = authCode`, y GitLive construye con `code.toString()` → `"17007"` | `firebase-auth-iossimulatorarm64-2.6.0-sources.jar`, `auth.kt:164,211-270` |
| En iOS `FirebaseNetworkException` / `FirebaseTooManyRequestsException` no llevan `code` | **Correcto**: se construyen solo con `toString()` | ídem |
| Grafía Android de la cancelación web | **`ERROR_WEB_CONTEXT_CANCELED`** (una L) | `firebase-auth-24.2.0` `classes.jar`, constante en `zzaew` |
| `ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL`, `ERROR_CREDENTIAL_ALREADY_IN_USE`, `ERROR_REQUIRES_RECENT_LOGIN`, `ERROR_SESSION_EXPIRED`, `ERROR_QUOTA_EXCEEDED` | **Existen** | ídem |
| REST `CREDENTIAL_TOO_OLD_LOGIN_AGAIN`, `FEDERATED_USER_ID_ALREADY_LINKED` | **Existen** en el mapeo REST → código nativo | ídem |
| PII en `message` | **Android: no.** Mensajes estáticos ("The email address is already in use by another account."). **iOS: sí puede.** `NSError.toString()` vuelca `userInfo`, que incluye `FIRAuthErrorUserInfoEmailKey` en colisiones de cuenta | binario Android + `auth.kt` iOS |

**D-1 (versión)**: se buscó el uso en los consumidores.
- **Fledge** (`gradle/libs.versions.toml`): fijado a un SHA antiguo con la coordenada JitPack. Solo usa
  `AuthError.Unknown(...)` en `AppModulesTest.kt`. No tiene ningún `when` sobre `AuthError`,
  `RegisterEffect` ni `SocialTokenResult`.
- **Paparcar**: solo `AuthError.Unknown("stub")` en `FakeAuthRepository.kt`.

Ningún consumidor conocido se rompe en compilación. **Decidido por el humano (2026-09-13): `3.0.0`**,
semver estricto: 7 variantes nuevas en un `sealed` público y cambios de comportamiento visibles
(cancelación silenciosa, `OperationNotAllowed` que deja de emitirse, textos reescritos). El bump de
`version` y la migración del README van **en esta rama** (T11); publicar en Maven Central, no. Paparcar
actualiza la integración cuando `3.0.0` esté publicada.

## Correcciones al spec

1. **Logging sin `message`** (resuelve la duda de privacidad de las notas no funcionales). Como el
   `message` de iOS puede llevar el email, `runAuth` loguea solo `code`, la clase de la excepción y el
   `AuthError` resultante. El `message` y la causa siguen disponibles para el consumidor dentro de
   `AuthError.Unknown`; la librería no los escribe en el log.
2. **Google en Android también distingue fallo de cancelación** (añadido). Hoy
   `GoogleSignInProviderAndroid.signIn()` devuelve `null` tanto al cancelar como ante un
   `GetCredentialException` real (SHA-1 mal registrado, `webClientId` erróneo). Con `null →
   SignInCancelled`, un Google mal configurado dejaría de mostrar nada. Hoy muestra "no permitido".
   Se añade un método `internal` que devuelve `SocialTokenResult?` con `Failed` para los errores no
   de cancelación. El `signIn(): String?` público no cambia.
3. **Riesgo aceptado en iOS (D-5, D-9 resuelto: silencio + log)**: los handlers Swift solo devuelven
   `String?`, así que en iOS un fallo social real seguirá llegando como `null` y a partir de ahora **no
   mostrará nada** en vez de "no permitido". Para que no sea invisible, `FirebaseAuthProvider`
   registra **cada** token social `null` con el id del proveedor. Se documenta en la migración.
4. **`Logger.ios.kt` crashea en iOS (D8, medido en Paparcar)**. `NSLog("%@", "[$tag] W: $message")`:
   Kotlin/Native pasa un `String` en posición variádica como `char*`, no como `NSString`, y `%@` hace
   `objc_opt_respondsToSelector` sobre los bytes del texto → `EXC_BAD_ACCESS`. Está en la `2.0.1`
   publicada y cualquier `Logger.d/w/e` en iOS lo dispara. **Prerrequisito del log de `runAuth`**: sin
   este arreglo, cada fallo de auth en iOS sería un crash. Se corrige en T0.
5. **Errores de configuración → `ProviderNotConfigured` (D10)**. Caso real en Paparcar: API key de
   Android restringida, Google devuelve token y `signInWithCredential` falla → hoy `Unknown`. Entran en
   la tabla canónica `APP_NOT_AUTHORIZED` (17028), `INVALID_API_KEY` (17023) y
   `CONFIGURATION_NOT_FOUND`; y en el fallback por mensaje, `INTERNAL_ERROR` con `are blocked` o
   `API key not valid`. Grafías y clase de la excepción a confirmar provocando cada fallo en la demo
   antes de fijar los tests (si alguna no existe, se retira de AC-24 y se anota aquí).
6. **Corrección a la corrección 2**: `NoCredentialException` en la **segunda** pasada
   (`GetSignInWithGoogleOption`) **no** es cancelación. La cancelación en Credential Manager es
   siempre `GetCredentialCancellationException`; un `NoCredentialException` tras el selector a pantalla
   completa indica que no hay forma de obtener credencial (Play Services, configuración) → `Failed`.
   Solo la primera pasada trata `NoCredentialException` como "probar el fallback".
7. **Proveedor sin configurar en Android no es una cancelación** (decidido por el humano en
   `/implement`, 2026-09-13). En `Platform.android.kt`, `googleSignInConfig == null` y la rama `else`
   de proveedores sin implementar devolvían `null` y acababan en `SignInCancelled`, sin mensaje.
   Ahora devuelven `SocialTokenResult.Failed("CONFIGURATION_NOT_FOUND", …)`, que se clasifica como
   `ProviderNotConfigured`. Solo afecta a `androidMain` y no hay test unitario que lo cubra: se
   comprueba en el smoke.

---

## Enfoque técnico

```
GitLive SDK
  └─ GitLiveFirebaseAuthGateway.runGateway   ── FirebaseAuthFailure(message, cause, code)   [T4]
       └─ FirebaseAuthProvider.runAuth       ── Logger.w(code, clase, tipo)                  [T5]
            └─ mapFirebaseError(code, message, cause)                                        [T3]
                 ├─ code reconocido  → AuthError tipado    (igualdad sobre código normalizado)
                 └─ si no            → mapFirebaseErrorMessage(message)   (comportamiento actual)
androidMain: WebOAuth / Google / teléfono ─ Throwable.toAuthFailureCode() → mismo mapper     [T6]
ViewModels: SignInCancelled → solo limpiar loading                                          [T7]
```

### Normalización de códigos (`data/DataMapper.kt`)

Una sola tabla `private val AUTH_ERRORS_BY_CODE: Map<String, () -> AuthError>` indexada por la
**forma canónica** del código:

```
canonical(code) = code.trim()
                      .removePrefix("auth/")          // web: auth/email-already-in-use
                      .uppercase().replace('-', '_')  // web: email-already-in-use
                      .removePrefix("ERROR_")         // Android: ERROR_EMAIL_ALREADY_IN_USE
```

Así `ERROR_EMAIL_ALREADY_IN_USE`, `auth/email-already-in-use` y `email-already-in-use` dan todos
`EMAIL_ALREADY_IN_USE`. Las claves REST que no coinciden (`EMAIL_EXISTS`,
`CREDENTIAL_TOO_OLD_LOGIN_AGAIN`, …) y los números de iOS (`"17007"`) son entradas extra de la misma
tabla. La comparación es por **igualdad**, nunca por subcadena: esa es la razón de AC-05.

Entradas (canónico → variante):

| Variante | Claves |
|---|---|
| `InvalidCredentials` | `INVALID_CREDENTIAL`, `INVALID_LOGIN_CREDENTIALS`, `INVALID_PASSWORD`, `WRONG_PASSWORD`, `17004`, `17009` |
| `UserNotFound` | `USER_NOT_FOUND`, `17011` |
| `EmailAlreadyInUse` | `EMAIL_ALREADY_IN_USE`, `EMAIL_EXISTS`, `17007` |
| `WeakPassword` | `WEAK_PASSWORD`, `17026` |
| `InvalidEmail` | `INVALID_EMAIL`, `17008` |
| `InvalidResetCode` | `INVALID_ACTION_CODE`, `EXPIRED_ACTION_CODE`, `INVALID_OOB_CODE`, `EXPIRED_OOB_CODE`, `17029`, `17030` |
| `TooManyRequests` | `TOO_MANY_REQUESTS`, `TOO_MANY_ATTEMPTS_TRY_LATER`, `17010` |
| `UserDisabled` | `USER_DISABLED`, `17005` |
| `OperationNotAllowed` | `OPERATION_NOT_ALLOWED`, `17006` |
| `NetworkError` | `NETWORK_REQUEST_FAILED`, `NETWORK_ERROR`, `17020` |
| `PhoneNumberInvalid` | `INVALID_PHONE_NUMBER`, `17042` |
| `InvalidVerificationCode` | `INVALID_VERIFICATION_CODE`, `17044` |
| `AccountExistsWithDifferentCredential` | `ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL`, `17012` |
| `CredentialAlreadyInUse` | `CREDENTIAL_ALREADY_IN_USE`, `FEDERATED_USER_ID_ALREADY_LINKED`, `17025` |
| `RequiresRecentLogin` | `REQUIRES_RECENT_LOGIN`, `CREDENTIAL_TOO_OLD_LOGIN_AGAIN`, `17014` |
| `VerificationCodeExpired` | `SESSION_EXPIRED`, `CODE_EXPIRED`, `17051` |
| `QuotaExceeded` | `QUOTA_EXCEEDED`, `17052` |
| `SignInCancelled` | `WEB_CONTEXT_CANCELED`, `WEB_CONTEXT_CANCELLED`, `POPUP_CLOSED_BY_USER`, `CANCELLED_POPUP_REQUEST`, `17058` |

`NetworkError` y `Unknown` se construyen con `message` y `cause`; el resto con su texto por defecto.
Esto conserva el contrato actual, en el que `NetworkError(errorMessage)` lleva el mensaje.

`mapFirebaseErrorMessage` (fallback, AC-07) gana reglas `containsAny` para los seis nombres nuevos,
colocadas **al principio del `when`**, antes de `INVALID_CREDENTIAL` y de `NETWORK`, para que ninguna
regla genérica se las coma. Con `cause` añadido como parámetro opcional, `Unknown` lo conserva (AC-06).

---

## Ficheros a tocar por source-set

### `commonMain`

| Fichero | Cambio | AC |
|---|---|---|
| `domain/model/AuthError.kt` | +7 `data class`: `AccountExistsWithDifferentCredential`, `CredentialAlreadyInUse`, `RequiresRecentLogin`, `VerificationCodeExpired`, `QuotaExceeded`, `SignInCancelled`, `ProviderNotConfigured` (mismo molde: `message` con default en inglés) | AC-01..03, 17 |
| `data/DataMapper.kt` | `internal fun mapFirebaseError(code: String?, message: String, cause: Throwable? = null): AuthError` + tabla (incluye `ProviderNotConfigured`, corrección 5); `mapFirebaseErrorMessage(message, cause = null)` con reglas nuevas y la de API key bloqueada | AC-01..07, 24 |
| `data/AuthFailureLog.kt` *(nuevo, `internal`)* | Funciones puras que construyen la línea de log: `authFailureLogLine(code, exceptionClass, error)` y `nullSocialTokenLogLine(providerId)`. Nunca reciben `message` | AC-25 |
| `data/firebase/FirebaseAuthGateway.kt` | `FirebaseAuthFailure(message, cause, val code: String?)` + **constructor secundario `(message, cause = null)`**, para no romper la firma pública ni el binario | AC-08 |
| `data/firebase/GitLiveFirebaseAuthGateway.kt` | `runGateway`: `catch FirebaseAuthException → code = e.code`; `FirebaseNetworkException → "ERROR_NETWORK_REQUEST_FAILED"`; `FirebaseTooManyRequestsException → "ERROR_TOO_MANY_REQUESTS"`; resto `code = null`. Sigue siendo el único con `dev.gitlive.*` | AC-19, 20 |
| `Platform.kt` | `SocialTokenResult.Failed(val code: String?, val message: String)` | AC-10 |
| `data/FirebaseAuthProvider.kt` | `toAuthError()` usa `(this as? FirebaseAuthFailure)?.code`; `null → SignInCancelled` + `Logger.w(nullSocialTokenLogLine(provider.id))`; `Failed → mapFirebaseError`; `unsupported() → ProviderNotConfigured`; `Logger.w(authFailureLogLine(…))` en `runAuth` (sin `message`). Aplica a `signInWithOAuth` y `reauthenticate`. `RefreshToken` en reauth sigue en `OperationNotAllowed` | AC-08..11, 25 |
| `data/AuthRepositoryImpl.kt` | Magic Link sin config → `ProviderNotConfigured` | AC-11 |
| `presentation/util/AuthErrorExt.kt` | 7 ramas nuevas en `toStringRes()` | AC-17 |
| `presentation/screens/login/LoginViewModel.kt` | `onSocialSignIn`: no emitir `ShowError` si `SignInCancelled` | AC-12, 16 |
| `presentation/screens/register/RegisterEffect.kt` | `data object EmailVerificationRequired` | AC-15 |
| `presentation/screens/register/RegisterViewModel.kt` | social: silencio con `SignInCancelled`; `RequiresEmailVerification → EmailVerificationRequired` | AC-13, 15 |
| `presentation/screens/register/RegisterScreen.kt` | rama nueva del `when`: snackbar con `auth_error_requires_email_verification` (reutiliza `CustomSnackBar`) | AC-15 |
| `presentation/screens/reauth/ReauthViewModel.kt` | `onSubmitOAuth`: `authError = error.takeUnless { it is SignInCancelled }` | AC-14 |
| `composeResources/values*/strings.xml` (×9) | +7 claves, 6 reescritas (tabla del spec). Las 3 claves muertas se quedan (D-3) | AC-18 |

Para no repetir la comprobación en tres ViewModels se añade `internal val AuthError.isUserCancellation: Boolean`
en `presentation/util/AuthErrorExt.kt`. Así queda un único punto donde ampliar si mañana hay más
"errores silenciosos".

### `androidMain`

| Fichero | Cambio | AC |
|---|---|---|
| `data/firebase/AndroidAuthFailures.kt` *(nuevo, `internal`)* | `Throwable.firebaseErrorCode(): String?`: `FirebaseAuthException.errorCode`; `FirebaseNetworkException → "ERROR_NETWORK_REQUEST_FAILED"`; `FirebaseTooManyRequestsException → "ERROR_TOO_MANY_REQUESTS"`. Y `Throwable.toAuthError()` = `mapFirebaseError(firebaseErrorCode(), message ?: "", this)` | AC-20 |
| `provider/WebOAuthProviderAndroid.kt` | `addOnFailureListener → SocialTokenResult.Failed(e.firebaseErrorCode(), e.message ?: "")`. `addOnCanceledListener` y actividad ausente siguen devolviendo `null` | AC-10, 20 |
| `provider/GoogleSignInProviderAndroid.kt` | nuevo `internal suspend fun signInForToken(): SocialTokenResult?`. Solo `GetCredentialCancellationException` → `null`; `NoCredentialException` en la segunda pasada, otro `GetCredentialException`, `IllegalStateException` y actividad ausente → `Failed(code, message)`, con `code = e.type` para `GetCredentialException` y `null` para `IllegalStateException` / actividad ausente (corrección 6). `signIn(): String?` público se mantiene y delega | corrección 2, 6 · AC-23 |
| `Platform.android.kt` | Google usa `signInForToken()`; las 3 construcciones `AuthError.Unknown(e.message …)` a partir de una excepción pasan a `e.toAuthError()`. Las que no nacen de una excepción (actividad ausente, "no user returned") siguen en `Unknown` | AC-20 |

### `iosMain`

| Fichero | Cambio | AC |
|---|---|---|
| `util/Logger.ios.kt` | Un único `private fun log(line: String) = NSLog("%@", NSString.create(string = line))` usado por `d`, `w` y `e` (corrección 4). Se mantiene el formato fijo `"%@"`. Confirmar en simulador que el argumento llega como `id` (si `NSString.create` no lo garantiza, `line as NSString`) | AC-22 |

Handlers sociales y teléfono sin cambios (D-5). `Platform.ios.kt` no hace `when` sobre
`SocialTokenResult`, así que el caso nuevo no le afecta.

### `commonTest` / `androidUnitTest` *(se tocan en `/test`, nunca en `/implement`)*

| Fichero | Cambio |
|---|---|
| `data/DataMapperTest.kt` | sección nueva `mapFirebaseError`: AC-01..07 (los tests existentes **no** se tocan, AC-04) |
| `data/provider/FirebaseAuthProviderTest.kt` | AC-08..11; `FLE-90 cancelled social sign in…` se amplía para afirmar `SignInCancelled` |
| `data/AuthRepositoryImplTest.kt` | reescribir `sendMagicLink returns OperationNotAllowed…` → `ProviderNotConfigured` (AC-11) |
| `test/FakeFirebaseAuthGateway.kt` | `FakeSocialTokenProvider.returnsFailure(provider, code, message)` |
| `presentation/login/LoginViewModelTest.kt` | AC-12, AC-16 |
| `presentation/register/RegisterViewModelTest.kt` | AC-13; reescribir `…RequiresEmailVerification result emits ShowError` → AC-15 |
| `presentation/reauth/ReauthViewModelTest.kt` | AC-14 |
| `presentation/util/AuthErrorExtTest.kt` | `allErrors` + 7 variantes (AC-17) |
| `data/DataMapperTest.kt` (cont.) | AC-24: códigos de configuración → `ProviderNotConfigured`; `INTERNAL_ERROR` sin "are blocked" sigue en `Unknown` |
| `data/AuthFailureLogTest.kt` *(nuevo)* | AC-25: la línea lleva code y clase, no el email; `null` social lleva proveedor y `SignInCancelled` |
| `data/provider/FirebaseAuthProviderTest.kt` (cont.) | AC-23: `Failed` para Google produce un `AuthError` distinto de `SignInCancelled` y cero interacciones con el gateway |
| `androidUnitTest/.../StringResourcesLocaleParityTest.kt` | `EXPECTED_KEY_COUNT` 92 → 99 (AC-18) |

**ROJO legítimo en `/test`**:
- Los tipos nuevos se añaden ya en `/test` para que los tests compilen: las 7 variantes,
  `SocialTokenResult.Failed`, `RegisterEffect.EmailVerificationRequired`, `FirebaseAuthFailure.code`,
  `signInForToken` y la firma de `mapFirebaseError`.
- Los cuerpos nuevos quedan con `TODO()`: `mapFirebaseError`, las ramas nuevas de `toStringRes()` y
  `isUserCancellation`.
- Los tests fallarán por `NotImplementedError` o por aserción, nunca por compilación.
- `RegisterScreen` necesita la rama nueva ya en `/test` para compilar. Se hace con `Unit` provisional.

---

## Wiring de DI (Koin) y ports

**Sin cambios en Koin.** No hay bindings, módulos ni ports nuevos:
- `FirebaseAuthGateway` y `SocialTokenProvider` ya existen (spec 002) y son los que permiten probar
  AC-08..11 desde `commonTest`, con `FakeFirebaseAuthGateway.failWith` y `FakeSocialTokenProvider`.
- El único contrato que se amplía es el de los datos que viajan por ellos:
  `FirebaseAuthFailure.code` y `SocialTokenResult.Failed`.

## Impacto en plataformas

- **Android**: es donde se nota el arreglo (D1). Todos los errores de email y contraseña, los de OAuth
  web, los de Google y los de teléfono llegan tipados.
- **iOS**:
  - Email y contraseña y Magic Link ganan robustez: se clasifican por código numérico en lugar de por
    el texto de `NSError`.
  - Social y teléfono siguen igual, salvo que la cancelación o el fallo pasan a no mostrar nada
    (corrección 3).
- **Web**: no hay target `wasmJs` en `:baselogin`; la normalización `auth/…` queda preparada y cubierta
  por AC-03.
- **i18n**: 7 claves nuevas y 6 reescritas en 9 locales; invariantes del spec 012.
- **`/design-check`**: **N/A**. No hay pantallas ni componentes nuevos: `EmailVerificationRequired`
  reutiliza el `CustomSnackBar` que ya usa `RegisterScreen`.

## Conocimiento reutilizado

- **engram #111**: la jerarquía hermana de `FirebaseNetworkException` explica los códigos sintéticos,
  tanto en el gateway como en `AndroidAuthFailures.kt`.
- **engram #246**: el diagnóstico de este spec.
- **Spec 002**: los ports y fakes existentes. Este plan revierte su decisión de transportar solo el
  `message`: ahora también viaja el `code`, y el `message` sigue como fallback.
- **Spec 012**: el test de paridad de locales e invariantes de strings.
- **kmp-recipes `testing/kotlin-test-turbine-fakes`**, adaptada: sin Turbine, se sigue el patrón
  `launch { effect.collect }` del repo.

---

## Desglose de tareas

| # | Fase | Tarea | AC |
|---|---|---|---|
| **T0** | `/implement` (primero) | `Logger.ios.kt` (corrección 4). Sin test unitario posible (`iosMain`, `NSLog`); validación en simulador en T10 | 22 |
| **T1** | `/test` | Tipos y esqueletos que compilan: 7 variantes, `SocialTokenResult.Failed`, `RegisterEffect.EmailVerificationRequired` (+ rama provisional en `RegisterScreen`), `FirebaseAuthFailure.code` + constructor secundario, `mapFirebaseError` / `isUserCancellation` / ramas de `toStringRes` con `TODO()`, `signInForToken` con `TODO()` | — |
| **T2** | `/test` | Tests nuevos AC-01..18 y reescritura de los 4 tests que cambian de contrato; `FakeSocialTokenProvider.returnsFailure`. Confirmar ROJO por `NotImplementedError` o aserción | 01..18, 23..25 |
| **T3** | `/implement` | `mapFirebaseError` + tabla canónica (con configuración) + reglas nuevas y `cause` en `mapFirebaseErrorMessage` | 01..07, 24 |
| **T4** | `/implement` | `GitLiveFirebaseAuthGateway.runGateway` con `code` y códigos sintéticos | 08, 19, 20 |
| **T5** | `/implement` | `AuthFailureLog.kt`; `FirebaseAuthProvider` (code, `SignInCancelled` + log de `null`, `Failed`, `ProviderNotConfigured`, log de fallos) + `AuthRepositoryImpl.sendMagicLink` | 08..11, 25 |
| **T6** | `/implement` | `AndroidAuthFailures.kt`, `WebOAuthProviderAndroid`, `GoogleSignInProviderAndroid.signInForToken` (corrección 6), teléfono en `Platform.android.kt` | 10, 20, 23 |
| **T7** | `/implement` | `isUserCancellation`; ViewModels Login/Register/Reauth; `RegisterScreen` muestra el snackbar de verificación | 12..16 |
| **T8** | `/implement` | `toStringRes()` + strings EN/ES + de/fr/it/nl/pl/pt/ro | 17, 18 |
| **T9** | `/implement` | README: "Migrating to 3.0.0" (variantes nuevas, `OperationNotAllowed` → `SignInCancelled`/`ProviderNotConfigured`, silencio en iOS social) · `CLAUDE.md` §Ports: `FirebaseAuthFailure` transporta `code` | notas |
| **T10** | `/validate` | `ktlintCheck`, `:baselogin:testDebugUnitTest`, `:composeApp:linkDebugFrameworkIosSimulatorArm64`, `:composeApp:assembleDebug`; grep AC-19; smoke Android AC-20/AC-23; smoke simulador iOS AC-22 (signOut sin handler + registro con email existente, sin crash) | 19..23 |
| **T11** | `/implement` | `version = "3.0.0"` en `baselogin/build.gradle.kts`; coordenadas `3.0.0` en README y `CLAUDE.md` si las citan. **No** publicar | notas |

Commits previstos (conventional, minúsculas): `fix(ios): pass an objective-c object to nslog` (T0), `test(auth-errors): …` (T1-T2),
`fix(auth-errors): classify firebase failures by error code` (T3-T5),
`fix(auth-errors): keep android social and phone failure codes` (T6),
`fix(auth-errors): do not show cancelled social sign in as an error` (T7),
`feat(i18n): actionable auth error messages in nine locales` (T8), `docs: …` (T9),
`build: bump baselogin to 3.0.0` (T11).

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Colisión de subcadenas en el fallback (p.ej. un mensaje iOS con dos claves `ERROR_*`) | El código manda (AC-05). En iOS el `code` numérico está siempre presente para `FirebaseAuthException`, así que el fallback solo actúa con `code` nulo |
| `ERROR_SESSION_EXPIRED` confundido con sesión de usuario caducada | Solo lo emite el flujo SMS; la sesión de usuario usa `USER_TOKEN_EXPIRED` (17021), que no se toca |
| Silencio de fallos sociales en iOS | Log de cada `null` social (corrección 3) + migración; spec iOS aparte |
| El log de `runAuth` convierte cada fallo en crash en iOS | T0 va primero; AC-22 en simulador antes de dar T5 por buena |
| Grafías de los códigos de configuración (corrección 5) no verificadas | Provocarlos en la demo (API key restringida, SHA-1 retirado) antes de fijar AC-24 |
| Traducciones de 7 locales sin revisión nativa | Mismo criterio que el spec 012; frases cortas y literales |
