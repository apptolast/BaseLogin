# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**BaseLogin** is a reusable KMP (Kotlin Multiplatform) authentication library (`custom-login`) with a sample consumer app (`composeApp`). Targets: Android, iOS. Stack: Kotlin Multiplatform · Compose Multiplatform · Koin · Firebase Auth (via GitLive SDK).

## Build Commands

```bash
# Build Android debug APK
./gradlew :composeApp:assembleDebug

# Build iOS framework (for Xcode/CocoaPods)
./gradlew :composeApp:podInstall

# Run Android app
./gradlew :composeApp:installDebug

# Run all tests
./gradlew test

# Run tests for a single module
./gradlew :custom-login:test
./gradlew :composeApp:test

# Run a specific test class
./gradlew :custom-login:test --tests "com.apptolast.customlogin.SomeTestClass"

# Build the library only
./gradlew :custom-login:assemble

# Clean build
./gradlew clean
```

For iOS, open `iosApp/iosApp.xcworkspace` in Xcode and run from there.

## Module Structure

```
root/
├── composeApp/    ← Sample consumer app (Android + iOS). Contains no auth logic.
└── custom-login/  ← Publishable KMP library. All auth code lives here.
    ├── commonMain/    ← Shared code (interfaces, ViewModels, Compose UI)
    ├── androidMain/   ← Android-specific implementations (Google/Phone providers)
    └── iosMain/       ← iOS-specific implementations (Google/Phone providers)
```

## Architecture

### Layer Dependencies
```
[ Presentation ] → [ Domain ] ← [ Data ]
                        ↑
                      [ DI ]  (orchestrates, no logic)
```

- **domain**: Pure interfaces (`AuthRepository`, `AuthProvider`) and models. No external imports.
- **data**: Firebase/SDK implementations. Imports domain only. Never throws exceptions upward — always returns `AuthResult`.
- **presentation**: ViewModels + Compose UI. Imports domain only. Never imports data layer.
- **di**: Koin wiring. Imports all layers.

### MVI Pattern

Every screen has exactly these files:
- `XxxAction.kt` — `sealed interface` for user intents
- `XxxUiState.kt` — immutable `data class`, updated via `copy()`
- `XxxEffect.kt` — `sealed class` for one-shot events (navigation, snackbars)
- `XxxViewModel.kt` — exposes `StateFlow<UiState>` and `Flow<Effect>` (via `Channel.receiveAsFlow()`)
- `XxxScreen.kt` — `@Composable` collecting state/effects, using slots

### Slots System

`AuthScreenSlots` (in `AuthSlots.kt`) groups per-screen slot data classes (`LoginScreenSlots`, `RegisterScreenSlots`, etc.). Each slot has a working default. `null` = hidden. Slots receive only state values and callbacks — no business logic.

Consumer app integrates via:
```kotlin
authRoutesFlow(
    navController = navController,
    slots = AuthScreenSlots(login = LoginScreenSlots(header = { MyLogo() })),
    onNavigateToHome = { /* handled by AuthState change */ }
)
```

### Platform-Specific Code (expect/actual)

Used for providers requiring native SDKs. Pattern:
- `commonMain/data/GoogleAuthProvider.kt` — `expect class`
- `androidMain/data/GoogleAuthProviderAndroid.kt` — `actual class`
- `iosMain/data/GoogleAuthProviderIOS.kt` — `actual class`

`ActivityHolder` (Android only) holds a `WeakReference` to the current `ComponentActivity`. Must call `ActivityHolder.setActivity(this)` in `MainActivity.onCreate()` and `ActivityHolder.clearActivity(this)` in `onDestroy()`.

### Key Domain Models

- `AuthResult`: `sealed class` — `Success(session: UserSession)` | `Error(error: AuthError)`
- `AuthError`: typed errors — `InvalidCredentials`, `NetworkError`, `UserNotFound`, `EmailAlreadyInUse`, `Unknown`
- `IdentityProvider`: `Email`, `Google`, `Phone`, `Apple`, `GitHub` (last two pending)
- `AuthState`: `Loading`, `Authenticated(session)`, `Unauthenticated`, `Error`

### DI (Koin)

- Repositories and providers: `single { }`
- ViewModels: `viewModel { }` (never `single`)
- Library entry point: `initLoginKoin { ... }` — accepts a `KoinAppDeclaration` lambda for the consumer app to add `androidContext` and its own modules.

### Auth Flow (composeApp)

`SplashViewModel` observes `AuthRepository.observeAuthState()` eagerly. `App.kt` switches between `AuthNavigation` (unauthenticated) and `MainAppNavigation` (authenticated) using `AnimatedContent`. The splash screen stays visible until `SplashViewModel.isReady` is `true`.

## Adding a New Auth Provider

1. Add `data object NewProvider : IdentityProvider()` in `IdentityProvider.kt`
2. Create `expect class NewAuthProvider()` in `commonMain/data/`
3. Create `actual class` in `androidMain/data/` and `iosMain/data/`
4. Add mapper cases in `DataMapper.kt` if needed
5. Add `IdentityProvider.NewProvider` branch in `AuthRepositoryImpl`
6. Register `single { NewAuthProvider() }` in `DataModule.kt`
7. Add `LoginWithNew : LoginAction` in `LoginAction.kt`
8. Handle the action in `LoginViewModel`
9. Create `DefaultNewButton()` in `DefaultProviders.kt`
10. Add `val newButton: (@Composable (() -> Unit))? = null` in `LoginScreenSlots`
11. Render the slot in `LoginScreen.kt`

## Configuration

`GOOGLE_WEB_CLIENT_ID` is set in `gradle.properties` and injected as a `BuildConfig` field in `custom-login`. For Firebase, place `google-services.json` (Android) in `composeApp/` and `GoogleService-Info.plist` (iOS) in `iosApp/iosApp/`.

## Architecture Rules (from custom-login-rules.md)

- Presentation ↔ Data must never import each other; domain is the shared contract.
- `AuthRepository` never throws; all errors become `AuthResult.Error(AuthError)`.
- `UiState` is always immutable; effects use `Channel<Effect>`, never stored in state.
- No platform checks (`if platform == Android`) in `commonMain`; use expect/actual.
- `GlobalScope` is forbidden; always use `viewModelScope.launch { }`.