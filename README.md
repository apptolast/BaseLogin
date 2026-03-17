# BaseLogin — custom-login KMP Library

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin%20Multiplatform-2.0-orange?logo=kotlin" />
  <img src="https://img.shields.io/badge/Compose%20Multiplatform-1.10.0-4285F4?logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/Firebase-Auth-FFCA28?logo=firebase&logoColor=black" />
  <img src="https://img.shields.io/badge/Android-API%2024+-3DDC84?logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/iOS-15+-000000?logo=apple&logoColor=white" />
  <img src="https://img.shields.io/badge/License-MIT-blue.svg" />
</p>

---

## What is this?

**custom-login** is a **Kotlin Multiplatform library** that provides a complete, production-ready authentication module for Android and iOS apps. It ships with all screens, navigation, ViewModels, validation, and error handling already built — you configure the providers you need and optionally replace any UI component with your own.

It is designed to be the **standard authentication baseline** for any new KMP project: drop it in, wire up Firebase, and have a fully working login system in minutes.

---

## Platforms

| Platform | Min version | Notes |
|----------|------------|-------|
| **Android** | API 24 (Android 7.0) | Tested up to API 36 |
| **iOS** | iOS 15 | Arm64 + Simulator Arm64 |

---

## Tech Stack

### Core

| Technology | Version | Role |
|-----------|---------|------|
| [Kotlin](https://kotlinlang.org/) | 2.3.0 | Language |
| [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) | 2.3.0 | Shared code for Android + iOS |
| [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) | 1.10.0 | Declarative UI on both platforms |
| [Material 3](https://m3.material.io/) | (via Compose MP) | Design system |

### Authentication

| Technology | Version | Role |
|-----------|---------|------|
| [Firebase Authentication](https://firebase.google.com/docs/auth) | BOM 33.7.0 | Auth backend |
| [GitLive Firebase SDK](https://github.com/GitLiveApp/firebase-kotlin-sdk) | 2.4.0 | KMP wrapper for Firebase |
| [Google Sign-In (Android)](https://developer.android.com/identity/sign-in/credential-manager) | Credential Manager 1.5.0 | Native Google sign-in on Android |

### Architecture & DI

| Technology | Version | Role |
|-----------|---------|------|
| [Koin](https://insert-koin.io/) | 4.1.1 | Dependency injection |
| [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) | 2.9.1 | In-app navigation |
| [Lifecycle ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) | 2.9.6 | MVI ViewModels |

### Other

| Technology | Version | Role |
|-----------|---------|------|
| [Coil](https://coil-kt.github.io/coil/) | 3.3.0 | Image loading (KMP) |
| [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) | 1.10.0 | Route serialization for navigation |
| [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) | 1.10.2 | Async / Flow |

### Testing

| Technology | Version | Role |
|-----------|---------|------|
| [kotlin.test](https://kotlinlang.org/api/latest/kotlin.test/) | 2.3.0 | Multiplatform unit tests |
| [kotlinx-coroutines-test](https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-test) | 1.10.2 | ViewModel / Flow testing |

---

## Authentication Providers

| Provider | Android | iOS | Method |
|----------|---------|-----|--------|
| Email / Password | ✅ | ✅ | Firebase built-in |
| Google | ✅ | ✅ | Credential Manager (Android) / GIDSignIn (iOS) |
| Apple | — | ✅ | AuthenticationServices (iOS only) |
| GitHub | ✅ | ✅ | Firebase web OAuth |
| Microsoft | ✅ | ✅ | Firebase web OAuth |
| Twitter / X | ✅ | ✅ | Firebase web OAuth |
| Facebook | ✅ | ✅ | Firebase web OAuth |
| Phone OTP | ✅ | ✅ | Firebase PhoneAuthProvider |
| Magic Link | ✅ | ✅ | Firebase email link |

All providers are **opt-in** via `LoginLibraryConfig`. Disabled providers are not shown in the UI.

---

## Screens

| Screen | Description |
|--------|-------------|
| Welcome | Entry point with Login / Register options |
| Login | Email+password sign-in + social providers |
| Register | Account creation with validation |
| Forgot Password | Sends a reset email |
| Reset Password | Confirms new password with the reset code |
| Phone Auth | Phone number entry + SMS OTP verification |
| Magic Link | Passwordless email link sign-in |
| Re-authentication | Confirm identity before sensitive operations |

---

## Table of Contents

1. [Features](#features)
2. [Architecture Overview](#architecture-overview)
3. [Prerequisites](#prerequisites)
4. [Project Setup](#project-setup)
5. [Initialization](#initialization)
6. [Integrating the Navigation Flow](#integrating-the-navigation-flow)
7. [Provider Configuration](#provider-configuration)
   - [Google Sign-In](#google-sign-in)
   - [Apple Sign-In](#apple-sign-in)
   - [GitHub](#github)
   - [Microsoft](#microsoft)
   - [Twitter / X](#twitter--x)
   - [Facebook](#facebook)
   - [Phone OTP](#phone-otp)
   - [Magic Link](#magic-link)
8. [iOS Platform Setup](#ios-platform-setup)
   - [Google (iOS)](#google-ios)
   - [Apple (iOS)](#apple-ios)
   - [GitHub / Microsoft / Twitter / Facebook (iOS)](#github--microsoft--twitter--facebook-ios)
   - [Phone OTP (iOS)](#phone-otp-ios)
9. [Customizing the UI — Slots System](#customizing-the-ui--slots-system)
10. [Re-authentication Screen](#re-authentication-screen)
11. [AuthRepository Public API](#authrepository-public-api)
12. [Error Handling](#error-handling)
13. [Localization](#localization)
14. [Module Structure](#module-structure)

---

## Features

- Email / Password sign-in and registration
- Social sign-in: Google, Apple, GitHub, Microsoft, Twitter/X, Facebook
- Passwordless: Phone OTP and Magic Link (email)
- Re-authentication screen for sensitive operations
- Password reset (forgot + reset flows)
- Fully customizable UI via a **slots system** — replace any component without touching the library
- MVI architecture per screen (Action → ViewModel → UiState + Effect)
- Typed error handling via `AuthError` sealed class
- Full localization support (EN, ES, FR, IT, PT)
- Edge-to-edge display with proper insets handling

---

## Architecture Overview

```
custom-login/
├── domain/
│   ├── AuthProvider.kt          ← Interface for auth backends (Firebase, Supabase, etc.)
│   ├── AuthRepository.kt        ← Public API consumed by the host app and ViewModels
│   └── model/                   ← AuthResult, AuthError, Credentials, IdentityProvider…
├── data/
│   ├── FirebaseAuthProvider.kt  ← Firebase implementation of AuthProvider
│   ├── AuthRepositoryImpl.kt    ← Delegates to AuthProvider; reads config
│   └── DataMapper.kt            ← Maps Firebase exceptions to typed AuthError
├── di/
│   ├── KoinInitializer.kt       ← initLoginKoin() entry point
│   ├── LoginLibraryConfig.kt    ← All feature flags and provider configs
│   ├── DataModule.kt
│   └── PresentationModule.kt
└── presentation/
    ├── screens/                 ← One folder per screen (MVI: Action/UiState/Effect/VM/Screen)
    ├── slots/                   ← AuthScreenSlots + per-screen slots data classes
    │   └── defaultslots/        ← Default Composable implementations
    └── navigation/
        └── RootNavGraph.kt      ← authRoutesFlow() extension on NavGraphBuilder
```

Each screen follows the same MVI pattern:

| File | Role |
|------|------|
| `XxxAction` | All user inputs — sealed interface |
| `XxxUiState` | Persistent state driving recomposition |
| `XxxEffect` | One-time events (navigation, snackbars) |
| `XxxViewModel` | Processes actions, updates state, emits effects |
| `XxxScreen` | Composable — renders state, forwards actions |

---

## Prerequisites

1. **Firebase project** with Authentication enabled and the desired sign-in methods activated in the Firebase console.
2. Add `google-services.json` (Android) and `GoogleService-Info.plist` (iOS) to your project.
3. Koin dependency injection configured in the host app (the library registers its own modules via `initLoginKoin`).

---

## Project Setup

**`settings.gradle.kts`** — include the module:
```kotlin
include(":custom-login")
```

**`build.gradle.kts`** (app / composeApp module):
```kotlin
dependencies {
    implementation(project(":custom-login"))
}
```

The library's own dependencies (Firebase, Koin, Compose, etc.) are defined in `custom-login/build.gradle.kts` and are transitively available.

---

## Initialization

Call `initLoginKoin` **once at app startup**, before any Composable is shown. Pass a `LoginLibraryConfig` with the providers you want to enable.

### Android — `Application.onCreate()`

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initLoginKoin(
            config = LoginLibraryConfig(
                googleSignInConfig = GoogleSignInConfig(
                    webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com",
                    iosClientId = "YOUR_IOS_CLIENT_ID.apps.googleusercontent.com" // optional
                ),
                appleSignInConfig = AppleSignInConfig(),  // iOS only
                githubEnabled = true,
                microsoftEnabled = true,
                twitterEnabled = false,
                facebookEnabled = false,
                phoneEnabled = true,
                magicLinkConfig = MagicLinkConfig(
                    continueUrl = "https://yourapp.page.link/signin",
                    iosBundleId = "com.yourcompany.yourapp"
                )
            )
        ) {
            // Optional: additional Koin modules from the host app
            androidContext(this@MyApplication)
            modules(yourAppModule)
        }
    }
}
```

### iOS — `MainViewController.kt` (Kotlin side)

```kotlin
fun MainViewController() = ComposeUIViewController {
    initLoginKoin(
        config = LoginLibraryConfig(
            googleSignInConfig = GoogleSignInConfig(
                webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com",
                iosClientId = "YOUR_IOS_CLIENT_ID.apps.googleusercontent.com"
            ),
            appleSignInConfig = AppleSignInConfig(),
            githubEnabled = true,
            phoneEnabled = true,
        )
    )
    App()
}
```

---

## Integrating the Navigation Flow

The library exposes `authRoutesFlow`, a `NavGraphBuilder` extension. Add it to your existing NavHost:

```kotlin
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "auth") {

        authRoutesFlow(
            navController = navController,
            onNavigateToHome = {
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                }
            }
            // slots = AuthScreenSlots()  ← optional, see Slots section
        )

        composable("home") { HomeScreen() }
    }
}
```

The auth flow contains: **Welcome → Login / Register → Forgot Password → Reset Password → Phone Auth → Magic Link**. Navigation between them is handled internally.

---

## Provider Configuration

All provider flags live in `LoginLibraryConfig`. Providers not configured are simply not shown in the UI.

### Google Sign-In

```kotlin
LoginLibraryConfig(
    googleSignInConfig = GoogleSignInConfig(
        webClientId = "123456789-abc.apps.googleusercontent.com",  // required on both platforms
        iosClientId = "123456789-ios.apps.googleusercontent.com"   // required for iOS
    )
)
```

Get the Web Client ID from the Firebase console → Authentication → Sign-in method → Google → Web SDK configuration.

**Android** — uses the Credential Manager API. No extra code required.
**iOS** — requires a Swift handler. See [Google (iOS)](#google-ios).

---

### Apple Sign-In

```kotlin
LoginLibraryConfig(
    appleSignInConfig = AppleSignInConfig(
        scopes = listOf("email", "name")  // default
    )
)
```

Apple Sign-In is **iOS only**. It will not appear as a provider on Android.
Requires the **Sign in with Apple** capability in Xcode and the entitlement in your app.
**iOS** — requires a Swift handler with nonce. See [Apple (iOS)](#apple-ios).

---

### GitHub

```kotlin
LoginLibraryConfig(githubEnabled = true)
```

Enable **GitHub** in the Firebase console (Authentication → Sign-in method → GitHub) and provide your GitHub OAuth App credentials there.

**Android** — handled via Firebase web OAuth (Chrome Custom Tab). No extra code required.
**iOS** — requires a Swift handler. See [GitHub / Microsoft / Twitter / Facebook (iOS)](#github--microsoft--twitter--facebook-ios).

---

### Microsoft

```kotlin
LoginLibraryConfig(microsoftEnabled = true)
```

Enable **Microsoft** in Firebase console.
**Android** — Firebase web OAuth. No extra code required.
**iOS** — requires a Swift handler. See [GitHub / Microsoft / Twitter / Facebook (iOS)](#github--microsoft--twitter--facebook-ios).

---

### Twitter / X

```kotlin
LoginLibraryConfig(twitterEnabled = true)
```

Enable **Twitter** in Firebase console and add your Twitter API key and secret.
**Android** — Firebase web OAuth. No extra code required.
**iOS** — requires a Swift handler. See [GitHub / Microsoft / Twitter / Facebook (iOS)](#github--microsoft--twitter--facebook-ios).

---

### Facebook

```kotlin
LoginLibraryConfig(facebookEnabled = true)
```

Enable **Facebook** in Firebase console. You also need a Facebook Developer App with the correct OAuth redirect URI configured (`https://<project-id>.firebaseapp.com/__/auth/handler`).
**Android** — Firebase web OAuth. No extra code required.
**iOS** — requires a Swift handler. See [GitHub / Microsoft / Twitter / Facebook (iOS)](#github--microsoft--twitter--facebook-ios).

---

### Phone OTP

```kotlin
LoginLibraryConfig(phoneEnabled = true)  // default
```

Enable **Phone** in Firebase console. The library provides a full Phone Auth screen with country code picker and OTP verification step.
**Android** — uses Firebase native `PhoneAuthProvider` with SIM-based instant verification support.
**iOS** — requires two Swift handlers. See [Phone OTP (iOS)](#phone-otp-ios).

---

### Magic Link

```kotlin
LoginLibraryConfig(
    magicLinkConfig = MagicLinkConfig(
        continueUrl = "https://yourapp.page.link/signin",  // App Link / Universal Link
        iosBundleId = "com.yourcompany.yourapp"            // required for iOS
    )
)
```

Enable **Email link (passwordless)** in Firebase console.

The flow: user enters email → Firebase sends a link → user taps it → app opens via deep link → call `authRepository.signInWithMagicLink(email, link)`.

You must:
1. Set up **App Links** (Android) or **Universal Links** (iOS) for `continueUrl`.
2. In the Activity/scene that receives the link, call:

```kotlin
// Android — in Activity.onNewIntent or similar
val link = intent.data?.toString()
if (link != null) {
    val email = preferences.getString("pending_magic_link_email", null)
    if (email != null) {
        authRepository.signInWithMagicLink(email, link)
    }
}
```

---

## iOS Platform Setup

The library's Kotlin side is complete. iOS providers use a **callback pattern**: Kotlin suspends the coroutine and waits for Swift to execute the native sign-in and call back with the result.

Set up all handlers **before** the first Composable renders, typically in `AppDelegate` or immediately in `MainViewController`.

---

### Google (iOS)

```swift
import GoogleSignIn

// In AppDelegate.application(_:didFinishLaunchingWithOptions:) or equivalent:
GoogleSignInProviderIOS.companion.signInHandler = { clientId, completion in
    guard let clientId = clientId,
          let rootVC = UIApplication.shared.connectedScenes
              .compactMap({ ($0 as? UIWindowScene)?.keyWindow?.rootViewController })
              .first else {
        completion(nil)
        return
    }

    let config = GIDConfiguration(clientID: clientId)
    GIDSignIn.sharedInstance.configuration = config
    GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { result, error in
        guard let user = result?.user, error == nil else {
            completion(nil)
            return
        }
        // Combine idToken and accessToken with the "|||accessToken|||" separator
        let idToken = user.idToken?.tokenString ?? ""
        let accessToken = user.accessToken.tokenString
        completion("\(idToken)|||accessToken|||\(accessToken)")
    }
}
```

---

### Apple (iOS)

Apple Sign-In requires a cryptographic nonce to prevent replay attacks.

```swift
import AuthenticationServices
import CryptoKit

class AppleSignInDelegate: NSObject, ASAuthorizationControllerDelegate,
                            ASAuthorizationControllerPresentationContextProviding {

    private var currentRawNonce: String?
    private var completion: ((String?) -> Void)?

    func setup() {
        AppleSignInProviderIOS.companion.signInHandler = { [weak self] _, completion in
            self?.completion = completion
            self?.startSignIn()
        }
    }

    private func startSignIn() {
        let rawNonce = randomNonceString()
        currentRawNonce = rawNonce

        let request = ASAuthorizationAppleIDProvider().createRequest()
        request.requestedScopes = [.fullName, .email]
        request.nonce = sha256(rawNonce)

        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.presentationContextProvider = self
        controller.performRequests()
    }

    func authorizationController(controller: ASAuthorizationController,
                                  didCompleteWithAuthorization authorization: ASAuthorization) {
        guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
              let tokenData = credential.identityToken,
              let idToken = String(data: tokenData, encoding: .utf8) else {
            completion?(nil); return
        }
        let rawNonce = currentRawNonce ?? ""
        // Pass token with the "|||rawNonce|||" separator
        let combined = rawNonce.isEmpty ? idToken : "\(idToken)|||rawNonce|||\(rawNonce)"
        completion?(combined)
        completion = nil
    }

    func authorizationController(controller: ASAuthorizationController,
                                  didCompleteWithError error: Error) {
        completion?(nil)
        completion = nil
    }

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        UIApplication.shared.connectedScenes
            .compactMap { ($0 as? UIWindowScene)?.keyWindow }
            .first!
    }

    // Nonce helpers
    private func randomNonceString(length: Int = 32) -> String {
        let charset = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        var result = ""
        var remainingLength = length
        while remainingLength > 0 {
            var randoms = [UInt8](repeating: 0, count: 16)
            SecRandomCopyBytes(kSecRandomDefault, randoms.count, &randoms)
            randoms.forEach { random in
                if remainingLength == 0 { return }
                if random < charset.count { result.append(charset[Int(random)]); remainingLength -= 1 }
            }
        }
        return result
    }

    private func sha256(_ input: String) -> String {
        let data = Data(input.utf8)
        let hash = SHA256.hash(data: data)
        return hash.compactMap { String(format: "%02x", $0) }.joined()
    }
}
```

---

### GitHub / Microsoft / Twitter / Facebook (iOS)

These four providers share the same pattern. Firebase handles the full OAuth flow from Swift. The Kotlin library only needs to know when it is complete.

Replace `"github.com"` with `"microsoft.com"`, `"twitter.com"`, or `"facebook.com"` as needed.

```swift
import FirebaseAuth

// GitHub example — call this at app startup
GitHubSignInProviderIOS.companion.signInHandler = { _, completion in
    let provider = OAuthProvider(providerID: "github.com")
    provider.scopes = ["user:email"]

    provider.getCredentialWith(nil) { credential, error in
        guard let credential = credential, error == nil else {
            completion(nil); return
        }
        Auth.auth().signIn(with: credential) { _, error in
            // Tell Kotlin that Firebase sign-in is complete
            completion(error == nil ? "___PLATFORM_AUTH_COMPLETE___" : nil)
        }
    }
}

// Microsoft
MicrosoftSignInProviderIOS.companion.signInHandler = { _, completion in
    let provider = OAuthProvider(providerID: "microsoft.com")
    provider.scopes = ["email", "profile"]
    // Optional: provider.customParameters = ["tenant": "your-tenant-id"]
    provider.getCredentialWith(nil) { credential, error in
        guard let credential = credential, error == nil else { completion(nil); return }
        Auth.auth().signIn(with: credential) { _, error in
            completion(error == nil ? "___PLATFORM_AUTH_COMPLETE___" : nil)
        }
    }
}

// Twitter
TwitterSignInProviderIOS.companion.signInHandler = { _, completion in
    let provider = OAuthProvider(providerID: "twitter.com")
    provider.getCredentialWith(nil) { credential, error in
        guard let credential = credential, error == nil else { completion(nil); return }
        Auth.auth().signIn(with: credential) { _, error in
            completion(error == nil ? "___PLATFORM_AUTH_COMPLETE___" : nil)
        }
    }
}

// Facebook
FacebookSignInProviderIOS.companion.signInHandler = { _, completion in
    let provider = OAuthProvider(providerID: "facebook.com")
    provider.scopes = ["email", "public_profile"]
    provider.getCredentialWith(nil) { credential, error in
        guard let credential = credential, error == nil else { completion(nil); return }
        Auth.auth().signIn(with: credential) { _, error in
            completion(error == nil ? "___PLATFORM_AUTH_COMPLETE___" : nil)
        }
    }
}
```

> The sentinel string `"___PLATFORM_AUTH_COMPLETE___"` tells the Kotlin layer that Firebase sign-in was already completed by Swift, so it only needs to refresh the session.

---

### Phone OTP (iOS)

Phone auth requires two handlers — one for sending the code and one for verifying it.

```swift
import FirebaseAuth

// Handler 1: send the OTP
PhoneAuthProviderIOS.companion.sendCodeHandler = { phoneNumber, completion in
    PhoneAuthProvider.provider().verifyPhoneNumber(phoneNumber, uiDelegate: nil) { verificationId, error in
        completion(verificationId)  // nil on failure
    }
}

// Handler 2: verify the OTP and sign in
PhoneAuthProviderIOS.companion.verifyCodeHandler = { verificationId, smsCode, completion in
    let credential = PhoneAuthProvider.provider()
        .credential(withVerificationID: verificationId, verificationCode: smsCode)
    Auth.auth().signIn(with: credential) { result, error in
        completion(result?.user.uid)  // nil on failure
    }
}
```

---

## Customizing the UI — Slots System

Every screen exposes a `*ScreenSlots` data class. Pass your own Composables for any slot you want to replace; all others fall back to the built-in defaults.

```kotlin
val mySlots = AuthScreenSlots(
    login = LoginScreenSlots(
        // Replace the header with your own logo
        header = {
            Image(painter = painterResource(R.drawable.my_logo), contentDescription = null)
        },
        // Replace the submit button with a branded button
        submitButton = { onClick, isLoading, enabled, text ->
            MyBrandedButton(onClick = onClick, loading = isLoading, enabled = enabled, label = text)
        }
        // All other slots use defaults
    )
)
```

Then pass `mySlots` to `authRoutesFlow`:

```kotlin
authRoutesFlow(
    navController = navController,
    slots = mySlots,
    onNavigateToHome = { /* ... */ }
)
```

### Available slots per screen

| Screen | Replaceable slots |
|--------|------------------|
| **Login** | `header`, `emailField`, `passwordField`, `submitButton`, `socialProviders`, `forgotPasswordLink`, `registerLink`, `footer` |
| **Register** | `header`, `nameField`, `emailField`, `passwordField`, `confirmPasswordField`, `termsCheckbox`, `submitButton`, `socialProviders`, `loginLink`, `logo`, `footer` |
| **Forgot Password** | `header`, `description`, `emailField`, `submitButton`, `successContent` |
| **Reset Password** | `header`, `description`, `passwordField`, `confirmPasswordField`, `submitButton`, `successContent` |
| **Phone Auth** | `phoneHeader`, `phoneDescription`, `phoneField`, `sendCodeButton`, `otpHeader`, `otpDescription`, `otpField`, `verifyButton` |
| **Magic Link** | `header`, `description`, `emailField`, `submitButton`, `successContent` |
| **Re-auth** | `header`, `description`, `emailField`, `passwordField`, `errorMessage`, `submitButton`, `socialSection` |

All `submitButton` slots share the same signature:
```kotlin
(onClick: () -> Unit, isLoading: Boolean, enabled: Boolean, text: String) -> Unit
```

All `socialProviders` slots share the same signature:
```kotlin
(providers: List<IdentityProvider>, loadingProvider: IdentityProvider?, onProviderClick: (IdentityProvider) -> Unit) -> Unit
```

---

## Re-authentication Screen

The library includes a re-authentication screen for sensitive operations (delete account, change email/password). It is not part of the main `authRoutesFlow` — the host app launches it independently when needed.

```kotlin
// Add to your own NavGraph
composable("reauth") {
    ReauthScreen(
        slots = mySlots.reauth,
        onReauthSuccess = { navController.navigate("delete_account") },
        onNavigateBack = { navController.popBackStack() }
    )
}
```

---

## AuthRepository Public API

Inject `AuthRepository` anywhere in your app (via Koin) to interact with auth state programmatically.

```kotlin
class MyViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // Observe auth state changes
    val authState = authRepository.observeAuthState()  // Flow<AuthState>

    // Check sign-in status
    suspend fun checkSession() = authRepository.isSignedIn()

    // Get current session (refreshes token)
    suspend fun getUser() = authRepository.getCurrentSession()  // UserSession?

    // Get ID token for backend verification
    suspend fun getToken() = authRepository.getIdToken(forceRefresh = false)

    // Sign out
    suspend fun signOut() = authRepository.signOut()

    // Account management
    suspend fun deleteAccount() = authRepository.deleteAccount()
    suspend fun updateDisplayName(name: String) = authRepository.updateDisplayName(name)
    suspend fun updateEmail(email: String) = authRepository.updateEmail(email)
    suspend fun updatePassword(pass: String) = authRepository.updatePassword(pass)
    suspend fun sendEmailVerification() = authRepository.sendEmailVerification()

    // Re-authenticate before sensitive operations
    suspend fun reauth(email: String, pass: String) =
        authRepository.reauthenticate(Credentials.EmailPassword(email, pass))

    // Complete Magic Link sign-in (call from deep link handler)
    suspend fun completeMagicLink(email: String, link: String) =
        authRepository.signInWithMagicLink(email, link)
}
```

### `AuthState` values

```kotlin
sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val session: UserSession) : AuthState()
    data class Error(val error: AuthError) : AuthState()
}
```

---

## Error Handling

All auth operations return typed results — no raw exceptions propagate to the UI layer.

```kotlin
when (val result = authRepository.signIn(credentials)) {
    is AuthResult.Success -> { /* result.session: UserSession */ }
    is AuthResult.Failure -> {
        when (result.error) {
            is AuthError.InvalidCredentials -> showError("Wrong email or password")
            is AuthError.UserNotFound       -> showError("No account with that email")
            is AuthError.NetworkError       -> showError("Check your connection")
            is AuthError.TooManyRequests    -> showError("Too many attempts, try later")
            else                            -> showError(result.error.message)
        }
    }
    AuthResult.RequiresEmailVerification -> showError("Please verify your email first")
    AuthResult.PasswordResetSent  -> showSuccess("Reset email sent")
    AuthResult.PasswordResetSuccess -> navigateToLogin()
    AuthResult.MagicLinkSent      -> showSuccess("Check your inbox")
}
```

### Full `AuthError` hierarchy

| Error | Cause |
|-------|-------|
| `InvalidCredentials` | Wrong password or unified credential error |
| `UserNotFound` | No account with that email |
| `EmailAlreadyInUse` | Email already registered |
| `WeakPassword` | Password too short/simple |
| `InvalidEmail` | Malformed email address |
| `InvalidResetCode` | Reset link expired or already used |
| `TooManyRequests` | Rate-limited by Firebase |
| `UserDisabled` | Account disabled in Firebase console |
| `OperationNotAllowed` | Sign-in method not enabled in Firebase |
| `NetworkError` | No connectivity or request timeout |
| `SessionExpired` | Token expired, user needs to sign in again |
| `RequiresEmailVerification` | Account exists but email not verified |
| `PhoneNumberInvalid` | Malformed E.164 phone number |
| `InvalidVerificationCode` | Wrong or expired SMS OTP |
| `Unknown` | Unrecognised Firebase error |

---

## Localization

The library ships with strings in **5 languages**: English (default), Spanish, French, Italian, Portuguese.

String resources are in `custom-login/src/commonMain/composeResources/`:
- `values/strings.xml` (EN)
- `values-es/strings.xml` (ES)
- `values-fr/strings.xml` (FR)
- `values-it/strings.xml` (IT)
- `values-pt/strings.xml` (PT)

The active locale is picked up automatically from the device language. To add a new language, create a new `values-xx/strings.xml` with all keys from the default `values/strings.xml`.

---

## Module Structure

```
custom-login/
└── src/
    ├── commonMain/          ← All shared Kotlin: domain, data, presentation, DI
    │   ├── composeResources/
    │   │   └── values[-xx]/ ← String resources (5 locales)
    │   └── kotlin/com/apptolast/customlogin/
    │       ├── config/      ← GoogleSignInConfig, AppleSignInConfig, MagicLinkConfig
    │       ├── data/        ← FirebaseAuthProvider, AuthRepositoryImpl, DataMapper
    │       ├── di/          ← KoinInitializer, LoginLibraryConfig, DataModule, PresentationModule
    │       ├── domain/      ← AuthProvider, AuthRepository interfaces; model classes
    │       ├── presentation/
    │       │   ├── navigation/   ← authRoutesFlow, route objects
    │       │   ├── screens/      ← login, register, forgotpassword, resetpassword,
    │       │   │                    phone, magiclink, reauth, welcome
    │       │   ├── slots/        ← AuthScreenSlots + per-screen slots
    │       │   │   └── defaultslots/ ← Default Composable implementations
    │       │   └── util/         ← AuthErrorExt (toStringRes)
    │       └── util/        ← Logger (expect/actual), Validators, ValidationError(Ext)
    ├── androidMain/         ← Android implementations
    │   └── kotlin/com/apptolast/customlogin/
    │       ├── Platform.android.kt          ← getSocialIdToken, phone auth (actual)
    │       ├── provider/
    │       │   ├── GoogleSignInProviderAndroid.kt  ← Credential Manager API
    │       │   └── WebOAuthProviderAndroid.kt      ← Firebase web OAuth for all others
    │       └── util/Logger.android.kt
    └── iosMain/             ← iOS implementations
        └── kotlin/com/apptolast/customlogin/
            ├── Platform.ios.kt              ← getSocialIdToken, phone auth (actual)
            ├── provider/
            │   ├── GoogleSignInProviderIOS.kt
            │   ├── AppleSignInProviderIOS.kt
            │   ├── GitHubSignInProviderIOS.kt
            │   ├── MicrosoftSignInProviderIOS.kt
            │   ├── TwitterSignInProviderIOS.kt
            │   └── FacebookSignInProviderIOS.kt
            ├── data/PhoneAuthProviderIOS.kt
            └── util/Logger.ios.kt

composeApp/                  ← Sample consumer app (Android + iOS)
```
