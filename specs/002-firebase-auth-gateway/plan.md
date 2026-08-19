# Plan 002: Puerto de Firebase Auth y capa social completa (FLE-90)

> Rama: `feature/002-firebase-auth-gateway` · Spec: [`spec.md`](./spec.md) · Estado: draft
> Precedido por FLE-91 (spec 001, CocoaPods → SPM), ya mergeado en `develop` (`0acca7e`).

---

## Corrección al spec, antes de nada

El spec proponía crear un `FirebaseAuthErrorClassifier.kt` nuevo. **Es innecesario y estaba mal
documentado por mí**: `data/DataMapper.kt` ya contiene `mapFirebaseErrorMessage(errorMessage: String)`,
una función pura que mapea las tres familias de string —códigos REST (`INVALID_CREDENTIAL`), códigos
del SDK nativo de Android (`ERROR_WRONG_PASSWORD`) y códigos del SDK web (`wrong-password`)— a los
`AuthError` tipados. Ya tiene **43 tests** en `DataMapperTest`.

Lo que sí es cierto, y verificado con `grep`, es el defecto: **`FirebaseAuthProvider` nunca llama a ese
mapper desde sus 12 bloques `catch (e: Exception)`**. Solo lo usa indirectamente vía
`FirebaseAuthException.toAuthError()`. Como `FirebaseNetworkException` y
`FirebaseTooManyRequestsException` **no heredan** de `FirebaseAuthException` —comprobado con `javap`
sobre el bytecode: ambas extienden `com.google.firebase.FirebaseException`, hermana y no ancestro—,
caen al catch genérico y acaban en `AuthError.Unknown(e.message)`.

Es decir: el mapper ya sabía devolver `NetworkError` para ese mensaje, pero nadie se lo preguntaba.

**Consecuencia para el plan**: el arreglo es enrutar el catch genérico por
`mapFirebaseErrorMessage`, no escribir un clasificador nuevo. Mucho menos código y reutiliza 43 tests
existentes. El spec se corrige en consecuencia.

---

## Enfoque técnico

Dos trabajos que se pueden hacer por separado y conviene no mezclar en un commit:

1. **Testabilidad** (el objetivo del ticket): interponer un puerto entre `FirebaseAuthProvider` y el
   SDK, para que `commonTest` pueda ejercitar el provider con un fake escrito a mano.
2. **Tres defectos de comportamiento** que el puerto permite testear por primera vez: el catch
   genérico, el `displayName` de Apple y el `signOut` social.

El orden importa: sin (1), los tests de (2) no se pueden escribir.

## El puerto

`baselogin/src/commonMain/kotlin/com/apptolast/baselogin/data/firebase/`

```
FirebaseAuthGateway.kt          ← interfaz + tipos propios. Ningún dev.gitlive.* lo cruza
GitLiveFirebaseAuthGateway.kt   ← único fichero que importa dev.gitlive.firebase.auth.*
```

**Tipos propios que cruzan la frontera:**

| Tipo | Contenido |
|---|---|
| `FirebaseAuthUser` | `uid`, `email`, `displayName`, `photoUrl`, `isEmailVerified` |
| `FirebaseAuthCredential` | sellado: `EmailPassword(email, password)`, `Google(idToken, accessToken?)`, `OAuth(providerId, idToken, rawNonce?)` |
| `FirebaseAuthFailure` | única excepción que el puerto lanza; lleva el `message` original del SDK |

**Por qué el usuario no se expone como `FirebaseUser`**: es la misma razón que en
`GitLiveFirestoreProvider` (FLE-78) — si el tipo del SDK cruza la interfaz, el fake tendría que
importar `dev.gitlive` y se rompe la regla verificable de que `commonTest` no lo importe.

**Resolución perezosa**: el adaptador no toca `Firebase.auth` al construirse. Koin lo instancia antes
de que `FirebaseApp` esté inicializada, y hoy `loginDataModule` hace `single { Firebase.auth }`, que
reventaría en ese caso.

`toUserSession()` de `DataMapper.kt` pasa a mapear `FirebaseAuthUser` en vez de `FirebaseUser`. Es un
cambio de firma en una función `internal`, así que no rompe a ningún consumidor.

> ⚠️ Detalle a conservar: `toUserSession` aísla el fallo de `getIdToken` para que un `UserSession`
> siempre lleve `userId` aunque no haya red. Ese comportamiento tiene un comentario explicando el
> caso real que lo motivó; **no se toca**.

## Cuarto defecto, encontrado al leer el contrato en `/plan`

`AuthProvider.getCurrentSession()` documenta, literalmente:

> *Unlike `refreshSession`, this **MUST NOT perform network I/O** — it reads only from the provider's
> local persistent state. This makes it safe to call offline and from latency-sensitive contexts.*

Pero la implementación llama a `currentUser?.toUserSession()`, y `toUserSession()` hace
`getIdToken(false)`. El SDK devuelve el token cacheado si sigue vigente, **pero sale a la red si ha
caducado**. O sea: la implementación puede violar su propio contrato justo en el escenario que el
comentario dice querer proteger — sin conexión o con latencia crítica.

El código aísla el fallo para que `UserSession` conserve el `userId`, lo cual es correcto y hay que
mantener, pero eso mitiga el síntoma, no la violación.

**Arreglo**: `getCurrentSession()` mapea sin token (`accessToken = null`); quien lo necesite usa
`getIdToken(forceRefresh)`, que es lo que el contrato indica. Se cubre con un test que asserta cero
llamadas a `getIdToken` en el gateway falso — imposible de escribir hoy, y ese es justo el argumento
del ticket.

## Los tres defectos restantes

### 1. Errores de red mal mapeados

En cada `catch (e: Exception)`, sustituir `AuthError.Unknown(e.message ?: "...")` por
`mapFirebaseErrorMessage(e.message ?: "...")`. Doce sitios; conviene extraer un helper privado para no
repetirlo. Los `catch (e: FirebaseAuthException)` pueden desaparecer: el genérico ya cubre ambos casos
a través del mismo mapper.

### 2. `displayName` de Apple

Formato actual del canal Swift→Kotlin: `idToken|||rawNonce|||<nonce>`. Se **añade un segmento
opcional**, `|||displayName|||<name>`, en vez de cambiar el formato:

```
idToken                                            ← sigue valiendo
idToken|||rawNonce|||<nonce>                       ← sigue valiendo (integraciones actuales)
idToken|||rawNonce|||<nonce>|||displayName|||<n>   ← nuevo
```

Al recibir nombre y no tener `user.displayName`, el provider llama a `updateProfile`. Apple solo envía
`fullName` en la primerísima autorización de cada usuario: si no se guarda ahí, se pierde para
siempre.

Retrocompatible por construcción: el Swift ya cableado de cualquier consumidor sigue funcionando.

### 3. `signOut` social

Nuevo `expect suspend fun clearSocialSignInState()` en `Platform.kt`, invocado desde
`FirebaseAuthProvider.signOut()` **después** de `gateway.signOut()`.

- `androidMain`: `CredentialManager.create(appContext).clearCredentialState(ClearCredentialStateRequest())`.
- `iosMain`: no-op documentado.

Sin esto, tras cerrar sesión en Android el selector de cuenta de Google no reaparece y el usuario no
puede cambiar de cuenta.

## Wiring de Koin

```kotlin
fun loginDataModule(authProvider: AuthProvider? = null): Module = module {
    if (authProvider == null) {
        single<FirebaseAuthGateway> { GitLiveFirebaseAuthGateway() }   // perezoso
        single<AuthProvider> { FirebaseAuthProvider(get()) }
    } else {
        single<AuthProvider> { authProvider }
    }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
}
```

Desaparece `single { Firebase.auth }`. Ningún binding es `createdAtStart`: en Koin 4.2.x
`koinApplication { }` crea instancias eager por defecto, así que un binding ansioso tocaría el SDK solo
por construir el grafo — es la corrección que costó tres tests en Fledge (FLE-78).

## Tests

Nuevo `commonTest/testing/FakeFirebaseAuthGateway.kt`, a mano, sin librerías de mocking, siguiendo el
patrón de `FakeAuthProvider`/`FakeAuthRepository` que ya existen: propiedades públicas para configurar
respuestas y contadores/registros para assertear interacciones.

`FirebaseAuthProviderTest.kt` **se reescribe**: hoy ejercita `FakeAuthRepository` —un fake comprobando
otro fake— y no prueba nada del provider. Es el fichero que da nombre a la mentira que este ticket
corrige.

Regla verificable en `/validate`: `grep -rn "dev.gitlive" baselogin/src/commonTest/` → 0.

## Orden de tareas

```
T0 ──► T1a ──► T1b ──► T2 ──► T3 ──┬─► T4 ──┐
 (ktlint)      (/test)              ├─► T5 ──┼─► T7 ──► T8
                                    └─► T6 ──┘
```

| # | Tarea | Fase | AC |
|---|---|---|---|
| **T0** | ktlint + `.editorconfig`. Commit de formato **separado** del funcional | prep | AC-12 |
| **T1a** | Esqueletos con `TODO()`: `FirebaseAuthGateway`, tipos propios, adaptador | `/test` | — |
| **T1b** | `FakeFirebaseAuthGateway` + tests; **reescribir** `FirebaseAuthProviderTest` | `/test` | AC-02…AC-11 |
| **T2** | Implementar `GitLiveFirebaseAuthGateway` (perezoso, funnel de excepciones) | `/implement` | AC-01 |
| **T3** | Migrar `FirebaseAuthProvider` al puerto; `toUserSession` sobre `FirebaseAuthUser` | `/implement` | AC-03…AC-06, AC-11 |
| **T4** | Enrutar los `catch` por `mapFirebaseErrorMessage` | `/implement` | AC-09 |
| **T5** | `displayName` de Apple: segmento retrocompatible + `updateProfile` | `/implement` | AC-07, AC-08 |
| **T6** | `clearSocialSignInState()` `expect/actual` + llamada desde `signOut()` | `/implement` | AC-10 |
| **T7** | `loginDataModule`: registrar el gateway, quitar `single { Firebase.auth }` | `/implement` | AC-02 |
| **T8** | `CLAUDE.md` (§Architecture, §expect/actual) + validación + smoke | `/validate` | AC-12 |

**T7 (higiene de `Platform.android.kt`)** del spec se aplaza: el `lateinit var appContext` y el
`object` que resuelve Koin con `try/catch` silencioso son deuda real, pero tocarlos amplía el diff de
un ticket que ya cambia la firma pública del provider. Ticket propio.

## Verificación

```bash
./gradlew ktlintFormat && ./gradlew ktlintCheck
./gradlew :baselogin:testDebugUnitTest
./gradlew :baselogin:linkDebugFrameworkIosSimulatorArm64
./gradlew :composeApp:assembleDebug

xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug \
  -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build

grep -rn "dev.gitlive" baselogin/src/commonTest/   # -> 0
```

> Gotcha de método aprendido en FLE-91: al capturar `xcodebuild`, **no** usar `| grep … | head -N`.
> `head` cierra la tubería, `xcodebuild` muere por SIGPIPE y el `** BUILD SUCCEEDED **` nunca se
> emite. Volcar a fichero y filtrar después.

**Smoke manual** (irreducible, son APIs de plataforma): login con Google en Android comprobando que
tras `signOut()` **reaparece el selector de cuenta**; login con Apple en iOS con una cuenta nueva
comprobando que **el nombre llega al perfil**.

## Decisiones abiertas (🚦 Gate 2)

| # | Decisión | Propuesta |
|---|---|---|
| **D-1** ⚠️ | **Cambio de firma pública**: `FirebaseAuthProvider(firebaseAuth: FirebaseAuth)` → `(gateway: FirebaseAuthGateway)`. Rompe a quien lo construya a mano; no a quien use `loginDataModule()`. | Aceptarlo. Es el precio de la testabilidad. Mantener un constructor secundario con `FirebaseAuth` reintroduciría la dependencia intestable y dejaría el ticket sin sentido. |
| **D-2** | **`:baselogin:iosSimulatorArm64Test`** existe como tarea. Tras FLE-91 las dependencias nativas vienen por SPM, así que puede que ahora enlace (en Fledge falla con `ld: framework 'FirebaseCore' not found`). | Medirlo en `/test`, que es cuando habrá tests que ejecutar. Si enlaza, es cobertura nativa gratis. Si no, se documenta y queda fuera del gate. |
| **D-3** | **Alcance de la reescritura de `FirebaseAuthProviderTest`**: sus 8 tests actuales prueban `FakeAuthRepository`, no el provider. | Reescribirlo entero contra el gateway falso. Los tests de repositorio que se pierdan ya están cubiertos por `AuthRepositoryImplTest`. |
| **D-4** | **`updateProfile` en el login de Apple** añade una llamada de red al flujo. | Hacerla solo si llega nombre **y** el usuario no tiene `displayName`, y no fallar el login si esa llamada falla: el usuario ya está autenticado. |
