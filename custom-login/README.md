# CustomLogin - KMP Authentication Library

A **Kotlin Multiplatform** authentication library for Android and iOS with **Compose Multiplatform** UI.

## Features

- ✅ **Complete Auth Flow**: Login → Register → Forgot Password → Reset Password
- ✅ **Provider Architecture**: Swap between Firebase, Supabase, or custom backends
- ✅ **Customizable UI**: Theming system + Slots pattern for full UI customization
- ✅ **Multiplatform**: Shared code for Android and iOS
- ✅ **Type-Safe**: Sealed classes for results and errors
- ✅ **Coroutines-First**: Suspend functions and Flow for reactive state

## Quick Start

### 1. Add Dependency

```kotlin
// settings.gradle.kts
include(":custom-login")

// Your app's build.gradle.kts
dependencies {
    implementation(project(":custom-login"))
}
```

### 2. Initialize Koin

```kotlin
// In your Application class or App entry point
initKoin {
    modules(
        configModule,     // LoginConfig
        dataModule,       // Repository + Provider
        presentationModule // ViewModels
    )
}
```

### 3. Use the AuthFlow

```kotlin
@Composable
fun App() {
    CustomLogin.AuthFlow(
        onAuthSuccess = {
            // Navigate to your main app
        }
    )
}
```

## Customization

### Custom Theme

```kotlin
CustomLogin.AuthFlow(
    theme = AuthTheme(
        colors = AuthColors.Light.copy(
            primary = Color(0xFF0D47A1),
            onPrimary = Color.White
        ),
        typography = AuthTypography.Default,
        shapes = AuthShapes.Default,
        spacing = AuthSpacing.Default
    ),
    onAuthSuccess = { }
)
```

### Custom Slots

Replace any UI component with your own:

```kotlin
CustomLogin.AuthFlow(
    slots = AuthScreenSlots(
        login = LoginScreenSlots(
            logo = { 
                Image(
                    painter = painterResource(R.drawable.my_logo),
                    contentDescription = "Logo"
                )
            },
            socialProviders = { onProviderClick ->
                GoogleSignInButton { onProviderClick("google") }
                AppleSignInButton { onProviderClick("apple") }
            },
            submitButton = { onClick, isLoading, enabled, text ->
                MyCustomButton(
                    onClick = onClick,
                    isLoading = isLoading,
                    enabled = enabled,
                    text = text
                )
            }
        )
    ),
    onAuthSuccess = { }
)
```

### LoginConfig

Configure app branding and features:

```kotlin
val myConfig = LoginConfig(
    appName = "My App",
    subtitle = "Welcome back!",
    drawableResource = Res.drawable.my_logo,
    
    // Features
    emailEnabled = true,
    googleEnabled = true,
    appleEnabled = true,
    showRegisterLink = true,
    showForgotPassword = true,
    
    // Validation
    passwordMinLength = 8,
    
    // Labels (i18n)
    signInButtonText = "Sign In",
    signInWithGoogleText = "Continue with Google"
)
```

## Architecture

```
custom-login/
├── domain/
│   ├── model/          # AuthResult, UserSession, Credentials, AuthError
│   ├── provider/       # AuthProvider interface, AuthProviderRegistry
│   └── repository/     # AuthRepository interface
├── data/
│   ├── provider/       # FirebaseAuthProvider implementation
│   └── repository/     # AuthRepositoryImpl
├── presentation/
│   ├── navigation/     # Routes, RootNavGraph
│   ├── screens/        # Login, Register, ForgotPassword, ResetPassword
│   └── theme/          # AuthTheme, AuthColors, AuthSlots
└── di/                 # Koin modules
```

## Adding a Custom Provider

Implement the `AuthProvider` interface:

```kotlin
class MyCustomAuthProvider(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : AuthProvider {
    override val id: String = "custom"
    override val displayName: String = "My Backend"
    
    override suspend fun signIn(credentials: Credentials): AuthResult {
        return when (credentials) {
            is Credentials.EmailPassword -> {
                try {
                    val response = httpClient.post("$baseUrl/auth/login") {
                        setBody(LoginRequest(credentials.email, credentials.password))
                    }
                    AuthResult.Success(response.body<UserSession>())
                } catch (e: Exception) {
                    AuthResult.Failure(AuthError.NetworkError(e.message ?: "Error"))
                }
            }
            else -> AuthResult.Failure(AuthError.OperationNotAllowed("Unsupported"))
        }
    }
    
    // ... implement other methods
}

// Register your provider
CustomLogin.registerProvider(myProvider, isDefault = true)
```

## Auth Results

```kotlin
sealed interface AuthResult {
    data class Success(val session: UserSession)
    data class Failure(val error: AuthError)
    data object RequiresEmailVerification
    data object PasswordResetSent
}

sealed class AuthError(val message: String) {
    class InvalidCredentials
    class UserNotFound
    class EmailAlreadyInUse
    class WeakPassword
    class NetworkError
    class SessionExpired
    // ... more
}
```

## Observing Auth State

```kotlin
val authRepository: AuthRepository by inject()

// In a ViewModel
authRepository.observeAuthState()
    .collect { state ->
        when (state) {
            is AuthState.Loading -> showLoading()
            is AuthState.Authenticated -> navigateToHome(state.session)
            is AuthState.Unauthenticated -> showLogin()
            is AuthState.Error -> showError(state.error)
        }
    }
```

## Platforms Supported

| Platform | Status         |
|----------|----------------|
| Android  | ✅ Stable       |
| iOS      | ✅ Stable       |
| Desktop  | 🚧 Coming Soon |
| Web      | 🚧 Coming Soon |

## Dependencies

- **Kotlin**: 2.2.21
- **Compose Multiplatform**: 1.9.3
- **Koin**: 4.1.1
- **Firebase (GitLive)**: 2.4.0
- **Navigation Compose**: 2.9.1

## License

Apache 2.0

---

Made with ❤️ for the KMP community
