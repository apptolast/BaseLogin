# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**custom-login** is a Kotlin Multiplatform (KMP) authentication library targeting Android and iOS. It provides a configurable login UI and auth flows backed by Firebase Authentication (via GitLive SDK). A sample consumer app lives in `composeApp/`.

**Package namespace:** `com.apptolast.customlogin`

## Build Commands

```bash
# Android
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:installDebug

# Run all tests
./gradlew :custom-login:testDebugUnitTest

# iOS - build Kotlin framework
./gradlew :custom-login:linkDebugFrameworkIosArm64
./gradlew :custom-login:linkDebugFrameworkIosSimulatorArm64
# Then build/run from Xcode in /iosApp
```

## Module Structure

```
custom-login/          ← Library module (the deliverable)
  src/commonMain/      ← Shared code
  src/androidMain/     ← Android-specific (Credential Manager, Logger)
  src/iosMain/         ← iOS-specific (GoogleSignIn pod, Logger)
composeApp/            ← Sample consumer app
  src/androidMain/     ← Android entry (MainActivity, LoginApplication)
  src/iosMain/         ← iOS entry (MainViewController)
```

## Architecture

### MVI Pattern
Every screen uses: `XxxAction` (sealed interface) + `XxxUiState` (data class) + `XxxEffect` (sealed class) + `XxxViewModel` + `XxxScreen`.
- `UiState` = persistent state (drives recomposition)
- `Effect` = one-time events (navigation, snackbars)
- `Action` = all user inputs routed through `viewModel.onAction()`

### Layers
- `domain/` — `AuthProvider` interface, `AuthRepository` interface, domain models (`AuthResult`, `AuthError`, `Credentials`, `IdentityProvider`, etc.)
- `data/` — `FirebaseAuthProvider`, `AuthRepositoryImpl`
- `di/` — Koin modules: `dataModule`, `presentationModule`; `KoinInitializer.kt` with `LoginLibraryConfig`
- `presentation/` — ViewModels, Screens, Slots system

### Slots System
Consumers replace default UI components via `AuthScreenSlots` which contains per-screen `data class` with composable lambdas. All `submitButton` slots have the same signature: `(onClick: () -> Unit, isLoading: Boolean, enabled: Boolean, text: String)`.

Default implementations live in `presentation/slots/defaultslots/`.

### Library Entry Points
- Kotlin: `initLoginKoin(config: LoginLibraryConfig, appDeclaration?)` — call once at app start
- Compose: `AuthNavFlow(authSlots, onAuthSuccess)` from `RootNavGraph.kt`
- iOS helper: `GoogleSignInProviderIOS.signInHandler` must be set from Swift

### Dependency Injection
`LoginLibraryConfig` is registered as a Koin `single`. If `googleSignInConfig != null`, `GoogleSignInConfig` is also registered. `AuthRepositoryImpl` takes `AuthProvider` and `LoginLibraryConfig`.

### expect/actual
- `platform()` — platform name string
- `getSocialIdToken(provider)` — platform-specific OAuth token acquisition
- `Logger` — `internal expect object Logger { d(), w(), e() }` in `util/`

### Key Models
- `AuthResult` — `Success(session)`, `Failure(error)`, `RequiresEmailVerification`, `PasswordResetSent`, `PasswordResetSuccess`
- `AuthError` — typed sealed class (`InvalidCredentials`, `UserNotFound`, `NetworkError`, etc.)
- `Credentials` — `EmailPassword`, `OAuthToken(provider)`, `RefreshToken`
- `IdentityProvider` — `Google`, `Apple`, `Facebook`, `GitHub`, `Phone`, `Custom`

### AuthRepository public API
Key methods: `signIn(credentials)`, `signUp(data)`, `signOut()`, `sendPasswordResetEmail()`, `confirmPasswordReset()`, `reauthenticate(credentials)`, `getAvailableProviders()`, `observeAuthState()`.
Note: `signOut()`, `deleteAccount()`, `update*()`, `sendEmailVerification()` return `Result<Unit>`; auth flow methods return `AuthResult`.

## String Resources
Library strings live in `custom-login/src/commonMain/composeResources/values/strings.xml`. Validation message strings (`validation_*`) are defined there as a foundation for localization, though ViewModels currently use hardcoded English equivalents.
