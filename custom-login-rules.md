# custom-login · Reglas de Arquitectura y Flujo de Desarrollo

> **Módulo**: `custom-login` (librería KMP)  
> **App consumidora**: `composeApp`  
> **Package base**: `com.apptolast.customlogin`  
> **Stack**: Kotlin Multiplatform · Compose Multiplatform · Koin · Firebase Auth  
> **Targets**: Android · iOS · Desktop  

---

## 1. Visión General del Proyecto

Se trata de una **librería KMP de Login reutilizable** (`custom-login`) que puede ser consumida por cualquier aplicación Android/iOS/Desktop. La app `composeApp` es únicamente un ejemplo de consumo.

### Módulos Gradle
```
root/
├── composeApp/          ← App de ejemplo consumidora. Solo integra, no contiene lógica de auth.
└── custom-login/        ← Librería publicable. Todo el código de auth vive aquí.
    ├── commonMain/      ← Código compartido entre plataformas
    ├── androidMain/     ← Implementaciones específicas Android
    └── iosMain/         ← Implementaciones específicas iOS
```

---

## 2. Estructura de Ficheros

```
custom-login/src/
├── commonMain/kotlin/com/apptolast/customlogin/
│   ├── Platform.kt                          ← expect declarations
│   │
│   ├── data/
│   │   ├── AuthRepositoryImpl.kt            ← impl de AuthRepository
│   │   ├── DataMapper.kt                    ← conversiones Firebase → domain models
│   │   ├── FirebaseAuthProvider.kt          ← email/password via Firebase
│   │   └── PhoneAuthProvider.kt             ← expect class (platform-specific)
│   │
│   ├── di/
│   │   ├── DataModule.kt                    ← Koin bindings de data layer
│   │   ├── KoinInitializer.kt               ← initKoin() entry point
│   │   └── PresentationModule.kt            ← Koin bindings de ViewModels
│   │
│   ├── domain/
│   │   ├── AuthProvider.kt                  ← interface de provider
│   │   ├── AuthRepository.kt                ← interface de repository
│   │   └── model/
│   │       ├── AuthError.kt                 ← sealed class de errores
│   │       ├── AuthRequest.kt               ← request hacia el repository
│   │       ├── AuthResult.kt                ← sealed: Success | Error
│   │       ├── AuthState.kt                 ← estado de sesión global
│   │       ├── IdentityProvider.kt          ← sealed class: Google | Email | Phone | Apple | ...
│   │       ├── PhoneAuthResult.kt           ← resultado específico OTP
│   │       └── UserSession.kt               ← datos del usuario autenticado
│   │
│   └── presentation/
│       ├── navigation/
│       │   ├── NavTransitions.kt            ← animaciones de navegación
│       │   ├── RootNavGraph.kt              ← grafo de navegación de la librería
│       │   └── Routes.kt                   ← sealed class de rutas
│       │
│       ├── screens/
│       │   ├── components/                  ← componentes compartidos entre pantallas
│       │   │   ├── CustomSnackBar.kt
│       │   │   ├── DefaultAuthContainer.kt
│       │   │   ├── DividerContent.kt
│       │   │   ├── HeaderContent.kt
│       │   │   └── RegisterLinkButtonContent.kt
│       │   │
│       │   ├── login/                       ← LoginAction · LoginEffect · LoginLoadingState
│       │   │                                   LoginMapper · LoginScreen · LoginUiState · LoginViewModel
│       │   ├── register/                    ← RegisterAction · RegisterEffect · RegisterMapper
│       │   │                                   RegisterScreen · RegisterUiState · RegisterViewModel
│       │   ├── forgotpassword/              ← ForgotPasswordAction · Effect · Screen · UiState · ViewModel
│       │   ├── resetpassword/               ← ResetPasswordAction · Effect · Mapper · Screen · UiState · ViewModel
│       │   ├── phone/                       ← PhoneAuthAction · Effect · Screen · UiState · ViewModel
│       │   └── welcome/                     ← WelcomeScreen (pantalla inicial de bienvenida)
│       │
│       └── slots/
│           ├── AuthSlots.kt                 ← data class con todos los slots configurables
│           └── defaultslots/
│               ├── DefaultButtons.kt        ← botones por defecto (Login, Register, Forgot…)
│               ├── DefaultFields.kt         ← campos por defecto (Email, Password, Phone…)
│               ├── DefaultLayouts.kt        ← containers y layouts por defecto
│               ├── DefaultLinks.kt          ← links por defecto (RegisterLink, ForgotLink…)
│               └── DefaultProviders.kt      ← botones de provider (Google, Apple, GitHub…)
│
├── androidMain/kotlin/com/apptolast/customlogin/
│   ├── AndroidManifest.xml
│   ├── Platform.android.kt                  ← actual declarations
│   ├── data/
│   │   ├── GoogleAuthProviderAndroid.kt     ← actual Google Sign In para Android
│   │   └── PhoneAuthProviderAndroid.kt      ← actual Phone auth para Android
│   └── platform/
│       └── ActivityHolder.kt               ← referencia weak a la Activity actual
│
└── iosMain/kotlin/com/apptolast/customlogin/
    ├── Platform.ios.kt                      ← actual declarations
    └── data/
        ├── GoogleAuthProviderIOS.kt         ← actual Google Sign In para iOS
        └── PhoneAuthProviderIOS.kt          ← actual Phone auth para iOS
```

---

## 3. Capas de Arquitectura

### Diagrama de dependencias
```
[ Presentation ]  →  [ Domain ]  ←  [ Data ]
                          ↑
                        [ DI ]  (orquesta todo, no tiene lógica)
```

### Responsabilidades por capa

| Capa | Responsabilidad | Puede importar |
|------|----------------|----------------|
| **domain** | Contratos (interfaces), modelos puros | Nada externo |
| **data** | Implementaciones, Firebase, mappers | domain únicamente |
| **presentation** | UI, ViewModels, navegación, slots | domain únicamente |
| **di** | Wiring de Koin | data + presentation + domain |

> **NUNCA** importar clases de `data` en `presentation` ni viceversa. El dominio es el punto de encuentro.

---

## 4. Patrón MVI por Pantalla

Cada pantalla de la librería sigue MVI estrictamente.

### Ficheros obligatorios por pantalla

| Fichero | Tipo | Contenido |
|---------|------|-----------|
| `XxxAction.kt` | `sealed interface` | Intenciones del usuario hacia el ViewModel |
| `XxxUiState.kt` | `data class` inmutable | Estado completo observable de la pantalla |
| `XxxEffect.kt` | `sealed class` | Eventos de una sola vez (nav, snackbar, toast) |
| `XxxViewModel.kt` | `ViewModel` Koin | Lógica, expone StateFlow + Channel de effects |
| `XxxScreen.kt` | `@Composable` | UI que colecta state/effects y recibe slots |
| `XxxMapper.kt` | funciones puras | Convierte errores de dominio a strings UI (si es necesario) |

### Estructura de ViewModel

```kotlin
class XxxViewModel(
    private val authRepository: AuthRepository,
    // otros deps via Koin
) : ViewModel() {

    private val _state = MutableStateFlow(XxxUiState())
    val state: StateFlow<XxxUiState> = _state.asStateFlow()

    private val _effects = Channel<XxxEffect>(Channel.BUFFERED)
    val effects: Flow<XxxEffect> = _effects.receiveAsFlow()

    fun handleAction(action: XxxAction) = when (action) {
        is XxxAction.SomeAction -> doSomething()
        is XxxAction.Navigate   -> sendEffect(XxxEffect.NavigateTo(...))
    }

    private fun doSomething() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = authRepository.someOperation()
            result.fold(
                onSuccess = { session ->
                    _state.update { it.copy(isLoading = false) }
                    sendEffect(XxxEffect.NavigateToHome)
                },
                onError = { error ->
                    _state.update { it.copy(isLoading = false, error = XxxMapper.mapError(error)) }
                }
            )
        }
    }

    private fun sendEffect(effect: XxxEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
```

### Estructura de Screen

```kotlin
@Composable
fun XxxScreen(
    slots: AuthSlots = AuthSlots(),
    viewModel: XxxViewModel = koinViewModel(),
    onNavigateToY: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is XxxEffect.NavigateToY -> onNavigateToY()
                is XxxEffect.ShowError   -> { /* snackbar */ }
            }
        }
    }

    // Usar slots en lugar de componentes hardcoded
    slots.container {
        slots.header?.invoke()
        slots.emailField(state.email) { viewModel.handleAction(XxxAction.EmailChanged(it)) }
        slots.loginButton(state.isLoading) { viewModel.handleAction(XxxAction.Submit) }
    }
}
```

---

## 5. Sistema de Slots

Los slots permiten a la app consumidora personalizar la UI sin modificar la librería.

### Definición de AuthSlots

```kotlin
@Stable
data class AuthSlots(
    // Layout
    val container: @Composable (content: @Composable () -> Unit) -> Unit
        = { DefaultAuthContainer(it) },
    val header: (@Composable () -> Unit)? = null,

    // Fields
    val emailField: @Composable (String, (String) -> Unit) -> Unit
        = { value, onChange -> DefaultEmailField(value, onChange) },
    val passwordField: @Composable (String, (String) -> Unit) -> Unit
        = { value, onChange -> DefaultPasswordField(value, onChange) },
    val phoneField: @Composable (String, (String) -> Unit) -> Unit
        = { value, onChange -> DefaultPhoneField(value, onChange) },

    // Buttons
    val loginButton: @Composable (Boolean, () -> Unit) -> Unit
        = { loading, onClick -> DefaultLoginButton(loading, onClick) },
    val registerButton: @Composable (Boolean, () -> Unit) -> Unit
        = { loading, onClick -> DefaultRegisterButton(loading, onClick) },

    // Social providers
    val googleButton: (@Composable (() -> Unit))? = { DefaultGoogleButton(it) },
    val appleButton: (@Composable (() -> Unit))? = null,   // null = oculto
    val githubButton: (@Composable (() -> Unit))? = null,

    // Extra
    val extraProviders: (@Composable () -> Unit)? = null,

    // Links
    val forgotPasswordLink: (@Composable (() -> Unit))? = { DefaultForgotPasswordLink(it) },
    val registerLink: (@Composable (() -> Unit))? = { DefaultRegisterLinkButton(it) },
)
```

### Reglas de slots
- **Default siempre funcional**: cada slot tiene un default de `defaultslots/` que funciona sin configuración.
- **Nulo = oculto**: si un slot de provider es `null`, ese botón no se renderiza.
- **Sin lógica de negocio**: los slots solo reciben estado (value) y callbacks. Nunca lanzan corrutinas ni llaman a repositorios.
- **Un slot = una responsabilidad**: no agrupar header + fields en un solo slot.

### Uso desde la app consumidora

```kotlin
// composeApp
CustomLoginEntry(
    slots = AuthSlots(
        header = { MyBrandLogo() },
        loginButton = { loading, onClick ->
            MyBrandButton("Entrar", loading = loading, onClick = onClick)
        },
        googleButton = { onClick -> MyGoogleButton(onClick) },
        appleButton = { onClick -> MyAppleButton(onClick) },  // activa Apple
    )
)
```

---

## 6. Domain Models

### AuthResult (toda operación devuelve esto)
```kotlin
sealed class AuthResult {
    data class Success(val session: UserSession) : AuthResult()
    data class Error(val error: AuthError) : AuthResult()
}
```

### AuthError (errores tipados)
```kotlin
sealed class AuthError {
    data object InvalidCredentials : AuthError()
    data object NetworkError : AuthError()
    data object UserNotFound : AuthError()
    data object EmailAlreadyInUse : AuthError()
    data class Unknown(val message: String?) : AuthError()
    // Añadir nuevos errores específicos de provider aquí
}
```

### IdentityProvider (proveedores disponibles)
```kotlin
sealed class IdentityProvider {
    data object Email  : IdentityProvider()
    data object Google : IdentityProvider()
    data object Phone  : IdentityProvider()
    data object Apple  : IdentityProvider()   // pendiente
    data object GitHub : IdentityProvider()   // pendiente
    // Añadir nuevos aquí
}
```

### AuthRequest
```kotlin
data class AuthRequest(
    val provider: IdentityProvider,
    val email: String? = null,
    val password: String? = null,
    val phoneNumber: String? = null,
    val otpCode: String? = null,
    val idToken: String? = null,   // para OAuth providers
    val accessToken: String? = null,
)
```

### UserSession
```kotlin
data class UserSession(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val provider: IdentityProvider,
    val isEmailVerified: Boolean = false,
)
```

---

## 7. AuthRepository e AuthProvider

### AuthRepository (interface en domain)
```kotlin
interface AuthRepository {
    suspend fun signIn(request: AuthRequest): AuthResult
    suspend fun signUp(request: AuthRequest): AuthResult
    suspend fun signOut(): AuthResult
    suspend fun sendPasswordResetEmail(email: String): AuthResult
    suspend fun confirmPasswordReset(code: String, newPassword: String): AuthResult
    fun getCurrentSession(): UserSession?
    fun observeAuthState(): Flow<AuthState>
}
```

### AuthProvider (interface en domain)
```kotlin
interface AuthProvider {
    val supportedProviders: List<IdentityProvider>
    suspend fun signIn(request: AuthRequest): AuthResult
    suspend fun signOut(): AuthResult
}
```

### AuthRepositoryImpl (data layer)
- Recibe todos los providers vía Koin
- Delega a cada provider según `request.provider`
- Usa `DataMapper` para convertir respuestas
- Nunca lanza excepciones hacia arriba: las captura y devuelve `AuthResult.Error`

---

## 8. Inyección de Dependencias (Koin)

### DataModule.kt
```kotlin
val dataModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single { FirebaseAuthProvider() }
    single { GoogleAuthProvider() }      // expect/actual resuelto por plataforma
    single { PhoneAuthProvider() }       // expect/actual resuelto por plataforma
    // Añadir nuevos providers aquí al implementarlos
}
```

### PresentationModule.kt
```kotlin
val presentationModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { ForgotPasswordViewModel(get()) }
    viewModel { ResetPasswordViewModel(get()) }
    viewModel { PhoneAuthViewModel(get()) }
    // Añadir nuevo ViewModel aquí al añadir nueva pantalla
}
```

### KoinInitializer.kt
```kotlin
fun initKoin(additionalModules: List<Module> = emptyList()) {
    startKoin {
        modules(dataModule + presentationModule + additionalModules)
    }
}
```

### Reglas de DI
- Repositorios y providers: **`single`**
- ViewModels: **`viewModel { }`** (nunca `single`)
- La app consumidora puede pasar `additionalModules` para extender
- Nunca inyectar `Context` o `Activity` directamente: usar `ActivityHolder` en Android

---

## 9. Expect / Actual (Platform-specific code)

### Cuándo usar expect/actual
- Auth providers que requieren SDKs nativos (Google, Apple, Phone)
- APIs de sistema (ActivityHolder, biometría, etc.)
- **NUNCA** usar `if (platform == Android)` en commonMain

### Patrón estándar
```kotlin
// commonMain/data/GoogleAuthProvider.kt
expect class GoogleAuthProvider() : AuthProvider

// androidMain/data/GoogleAuthProviderAndroid.kt
actual class GoogleAuthProvider : AuthProvider {
    private val credentialManager = CredentialManager.create(ActivityHolder.context)
    actual override suspend fun signIn(request: AuthRequest): AuthResult { ... }
}

// iosMain/data/GoogleAuthProviderIOS.kt
actual class GoogleAuthProvider : AuthProvider {
    actual override suspend fun signIn(request: AuthRequest): AuthResult { ... }
}
```

### ActivityHolder (Android únicamente)
```kotlin
// androidMain/platform/ActivityHolder.kt
object ActivityHolder {
    private var _activity: WeakReference<ComponentActivity>? = null
    val activity: ComponentActivity get() = _activity?.get() ?: error("Activity not set")
    val context: Context get() = activity
    fun set(activity: ComponentActivity) { _activity = WeakReference(activity) }
}
```

---

## 10. DataMapper

Toda conversión entre datos externos y modelos de dominio ocurre en `DataMapper.kt`:

```kotlin
object DataMapper {
    fun FirebaseUser?.toUserSession(provider: IdentityProvider): UserSession? = this?.let {
        UserSession(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            provider = provider,
            isEmailVerified = isEmailVerified,
        )
    }

    fun Exception.toAuthError(): AuthError = when (this) {
        is FirebaseAuthInvalidCredentialsException -> AuthError.InvalidCredentials
        is FirebaseAuthUserCollisionException      -> AuthError.EmailAlreadyInUse
        is FirebaseAuthException                   -> AuthError.Unknown(message)
        else                                       -> AuthError.NetworkError
    }
}
```

---

## 11. Navegación

### Rutas (Routes.kt)
```kotlin
sealed class AuthRoute(val route: String) {
    data object Welcome       : AuthRoute("welcome")
    data object Login         : AuthRoute("login")
    data object Register      : AuthRoute("register")
    data object ForgotPassword: AuthRoute("forgot_password")
    data object ResetPassword : AuthRoute("reset_password/{code}")
    data object PhoneAuth     : AuthRoute("phone_auth")
    // Añadir nueva ruta aquí si se crea nueva pantalla
}
```

### RootNavGraph.kt
- Contiene el `NavHost` interno de la librería
- Recibe `AuthSlots` y los propaga a cada pantalla
- Expone callback `onAuthSuccess: (UserSession) -> Unit` para que la app consumidora reaccione

### Reglas de navegación
- La navegación entre pantallas de auth es interna a la librería
- La librería no sabe nada del NavGraph de la app consumidora
- La app consumidora recibe `onAuthSuccess` y navega donde quiera

---

## 12. Proveedores Implementados y Pendientes

| Provider | Status | commonMain | androidMain | iosMain | Slot |
|----------|--------|-----------|-------------|---------|------|
| Email/Password | ✅ DONE | FirebaseAuthProvider | - | - | emailField, passwordField |
| Google | ✅ DONE | expect | GoogleAuthProviderAndroid | GoogleAuthProviderIOS | googleButton |
| Phone/OTP | ⚙️ WIP | PhoneAuthProvider (expect) | PhoneAuthProviderAndroid | PhoneAuthProviderIOS | phoneField |
| Apple | 📋 TODO | - | - | - | appleButton |
| GitHub | 📋 TODO | - | - | - | githubButton |
| Twitter/X | 📋 TODO | - | - | - | twitterButton |
| Facebook | 📋 TODO | - | - | - | facebookButton |
| Microsoft | 📋 TODO | - | - | - | microsoftButton |
| Anonymous | 📋 TODO | - | - | - | anonymousButton |

---

## 13. Checklist para Añadir un Nuevo Provider

Cuando implementes un nuevo método de autenticación, sigue este orden exacto:

### Paso 1: Domain
- [ ] Añadir `data object NuevoProvider : IdentityProvider()` en `IdentityProvider.kt`
- [ ] Si tiene errores específicos, añadirlos en `AuthError.kt`

### Paso 2: Data - commonMain
- [ ] Crear `expect class NuevoAuthProvider() : AuthProvider` en `commonMain/data/`

### Paso 3: Data - plataformas
- [ ] Crear `actual class NuevoAuthProvider` en `androidMain/data/`
- [ ] Crear `actual class NuevoAuthProvider` en `iosMain/data/`
- [ ] Cada `actual` implementa `signIn(AuthRequest): AuthResult` y `signOut()`

### Paso 4: DataMapper
- [ ] Añadir caso en `DataMapper` si el provider devuelve tipos específicos

### Paso 5: AuthRepositoryImpl
- [ ] Añadir `IdentityProvider.NuevoProvider` al `when` que delega a providers

### Paso 6: DI
- [ ] Registrar `single { NuevoAuthProvider() }` en `DataModule.kt`

### Paso 7: Presentation - Actions
- [ ] Añadir `data object LoginWithNuevo : LoginAction` en `LoginAction.kt`

### Paso 8: Presentation - ViewModel
- [ ] Manejar `LoginAction.LoginWithNuevo` en `LoginViewModel.handleAction()`

### Paso 9: Slots UI
- [ ] Crear `DefaultNuevoButton()` en `DefaultProviders.kt`
- [ ] Añadir `val nuevoButton: (@Composable (() -> Unit))? = null` en `AuthSlots.kt`
- [ ] Renderizar el slot en `LoginScreen.kt` (si no es `null`)

---

## 14. Checklist para Añadir una Nueva Pantalla

Si se necesita una pantalla completamente nueva (ej: pantalla de selección de proveedor):

- [ ] Crear directorio `presentation/screens/nuevapantalla/`
- [ ] Crear `NuevaPantallaAction.kt` (sealed interface)
- [ ] Crear `NuevaPantallaUiState.kt` (data class)
- [ ] Crear `NuevaPantallaEffect.kt` (sealed class)
- [ ] Crear `NuevaPantallaViewModel.kt` (ViewModel Koin)
- [ ] Crear `NuevaPantallaScreen.kt` (@Composable con slots)
- [ ] Crear `NuevaPantallaMapper.kt` si hay conversiones (opcional)
- [ ] Añadir ruta en `Routes.kt`
- [ ] Añadir composable en `RootNavGraph.kt`
- [ ] Registrar ViewModel en `PresentationModule.kt`

---

## 15. Convenciones de Código

### Nombrado
- **Pantallas**: `XxxScreen.kt` — `@Composable fun XxxScreen(...)`
- **ViewModels**: `XxxViewModel.kt` — `class XxxViewModel : ViewModel()`
- **Actions**: `XxxAction.kt` — `sealed interface XxxAction`
- **Effects**: `XxxEffect.kt` — `sealed class XxxEffect`
- **UiState**: `XxxUiState.kt` — `data class XxxUiState(...)`
- **Providers**: `XxxAuthProvider.kt` → `XxxAuthProviderAndroid.kt` / `XxxAuthProviderIOS.kt`

### Corrutinas
- Siempre en `viewModelScope.launch { }` dentro del ViewModel
- Operaciones de I/O en `Dispatchers.IO` (o el dispatcher del repositorio)
- **Nunca** `GlobalScope` ni corrutinas sin scope controlado

### Manejo de errores
- El repositorio **nunca** lanza excepciones hacia el ViewModel
- Toda excepción interna se captura y convierte a `AuthResult.Error(AuthError)`
- El ViewModel actualiza `UiState.error` o envía `Effect.ShowError`

### StateFlow y Channel
```kotlin
// Estado observable (múltiples colectores, último valor cacheado)
val state: StateFlow<XxxUiState>

// Effects de una sola vez (un único colector, sin caché)
val effects: Flow<XxxEffect>  // via Channel.receiveAsFlow()
```

---

## 16. Integración desde la App Consumidora

### Mínimo necesario en composeApp

```kotlin
// Application.kt
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()  // inicializa custom-login
    }
}

// MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHolder.set(this)  // necesario para Google/Phone en Android

        setContent {
            CustomLoginNavHost(
                slots = AuthSlots(/* personalizaciones opcionales */),
                onAuthSuccess = { session ->
                    // navegar a la app principal
                }
            )
        }
    }
}
```

---

## Resumen de Reglas (TL;DR)

1. **Dependencias unidireccionales**: Presentation → Domain ← Data. DI orquesta, nunca contiene lógica.
2. **Interfaces en domain**: `AuthRepository` y `AuthProvider` son interfaces. Nunca clases concretas en domain.
3. **MVI estricto**: Cada pantalla tiene Action + UiState + Effect + ViewModel + Screen. Sin excepciones.
4. **UiState inmutable**: Siempre `data class` con `val`. Se actualiza con `copy()`.
5. **Effects con Channel**: Navegación y snackbars por `Channel<Effect>`, no en UiState.
6. **Expect/Actual para plataformas**: Nunca condiciones de plataforma en commonMain.
7. **Slots con defaults**: Cada slot tiene un composable por defecto. `null` = oculto.
8. **DataMapper centralizado**: Solo `DataMapper.kt` convierte entre Firebase types y domain models.
9. **Koin scopes correctos**: Repos/providers = `single`, ViewModels = `viewModel { }`.
10. **AuthResult siempre**: Toda operación devuelve `AuthResult`. Nunca lanzar excepciones hacia arriba.
