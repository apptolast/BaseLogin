# BaseLogin — custom-login KMP Library

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.4.0-7F52FF?logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin%20Multiplatform-2.4.0-orange?logo=kotlin" />
  <img src="https://img.shields.io/badge/Compose%20Multiplatform-1.11.1-4285F4?logo=jetpackcompose&logoColor=white" />
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
| [Kotlin](https://kotlinlang.org/) | 2.4.0 | Language |
| [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) | 2.4.0 | Shared code for Android + iOS |
| [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) | 1.11.1 | Declarative UI on both platforms |
| [Material 3](https://m3.material.io/) | (via Compose MP) | Design system |

### Authentication

| Technology | Version | Role |
|-----------|---------|------|
| [Firebase Authentication](https://firebase.google.com/docs/auth) | BOM 34.14.1 | Auth backend |
| [GitLive Firebase SDK](https://github.com/GitLiveApp/firebase-kotlin-sdk) | 2.5.0 | KMP wrapper for Firebase |
| [Google Sign-In (Android)](https://developer.android.com/identity/sign-in/credential-manager) | Credential Manager 1.6.0 | Native Google sign-in on Android |

### Architecture & DI

| Technology | Version | Role |
|-----------|---------|------|
| [Koin](https://insert-koin.io/) | 4.2.2 | Dependency injection |
| [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) | 2.9.2 | In-app navigation |
| [Lifecycle ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) | 2.10.0 | MVI ViewModels |

### Other

| Technology | Version | Role |
|-----------|---------|------|
| [Coil](https://coil-kt.github.io/coil/) | 3.5.0 | Image loading in the sample app |
| [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) | 1.11.0 | Route serialization for navigation |
| [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) | 1.11.0 | Async / Flow |

### Testing

| Technology | Version | Role |
|-----------|---------|------|
| [kotlin.test](https://kotlinlang.org/api/latest/kotlin.test/) | 2.4.0 | Multiplatform unit tests |
| [kotlinx-coroutines-test](https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-test) | 1.11.0 | ViewModel / Flow testing |

---

## Authentication Providers

| Provider | Android | iOS | Method |
|----------|---------|-----|--------|
| Email / Password | ✅ | ✅ | Firebase built-in |
| Google | ✅ | ✅ | Credential Manager (Android) / GIDSignIn (iOS) |
| Apple | ✅ | ✅ | Firebase web OAuth (Android) / AuthenticationServices (iOS) |
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
3. Koin dependency injection configured in the host app. Use `initLoginKoin` if the library should start/load its own modules, or `loginModules` when the host owns `startKoin`.

---

## Project Setup

There are two ways to consume the library. Host apps use **JitPack**; the local module is for working on the library itself.

### Option A — JitPack (host apps)

**`settings.gradle.kts`** — add the JitPack repository:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

**`build.gradle.kts`** — KMP host, declare it in `commonMain`:
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.github.apptolast.BaseLogin:baselogin:1.1.0")
        }
    }
}
```

Android-only host:
```kotlin
dependencies {
    implementation("com.github.apptolast.BaseLogin:baselogin:1.1.0")
}
```

**Get the group exactly right — it is the single most common way to break this.**

| Coordinate | What Gradle gets |
|---|---|
| `com.github.apptolast.BaseLogin:baselogin` | ✅ the real `.module` file, with variant metadata for every target |
| `com.github.apptolast:baselogin` | ❌ resolves, but only to JitPack's aggregate POM — no variant metadata, and the KMP metadata jar it serves is empty |

The group must carry the **repository** name (`BaseLogin`, capitalised) after the user name, even though `custom-login/build.gradle.kts` declares `group = "com.github.apptolast"` — JitPack republishes multi-module builds under `com.github.<user>.<repo>`. The two-segment form still answers with a POM, so the failure is not a 404: it surfaces later as unresolved symbols or an empty metadata jar.

The artifact id is **`baselogin`, not `custom-login`**: the root Kotlin Multiplatform publication is renamed in `custom-login/build.gradle.kts`. From it Gradle resolves the per-target artifacts (`custom-login-android`, `custom-login-iosarm64`, `custom-login-iossimulatorarm64`) automatically — never depend on those directly.

`dev.gitlive:firebase-auth` resolves transitively from Maven Central, so no extra repository is required.

#### Pinning a commit instead of a tag

Any commit on `develop` is a valid version, which is how you consume a fix before it is tagged:
```kotlin
implementation("com.github.apptolast.BaseLogin:baselogin:35a5e15")
```

#### If the Android classpath fails on iOS sub-modules

Gradle reads every `available-at` variant from the root `.module` and may validate the iOS
sub-modules while resolving an Android configuration. If that bites, exclude them from the leaf
resolution configurations only — excluding them higher up propagates and silently drops
`custom-login-android` from the Android compile classpath:

```kotlin
afterEvaluate {
    configurations
        .filter { it.name.endsWith("CompileClasspath") || it.name.endsWith("RuntimeClasspath") }
        .forEach { it.exclude(group = "com.github.apptolast.BaseLogin", module = "custom-login-iosarm64") }
}
```

A second historical workaround excluded the group from `*CInterop` configurations, because the
consumer's CocoaPods plugin looked for cinterop variants this library never published. That one is
obsolete for hosts that have moved to SPM.

#### Publishing a new version

1. Set `version` in `custom-login/build.gradle.kts`.
2. Merge into `develop` and tag with **exactly that same string** — `git tag 1.2.0 && git push origin 1.2.0`. If the tag and `version` disagree, the JitPack build succeeds but serves nothing under that tag.
3. JitPack builds **on demand**: the first request for a version triggers it. Per `jitpack.yml` it runs on macOS with JDK 17 and executes `:custom-login:publishToMavenLocal`, which takes a few minutes. A 404 immediately after tagging usually means "not built yet", not "broken".

Build status for every version: <https://jitpack.io/#apptolast/BaseLogin>

> **Check that page before pinning.** Not every tag here has been published — a tag existing in git does not mean JitPack ever served it.

### Option B — local module (working on the library)

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

This is what `composeApp/` does in this repository.

The library's own dependencies (Firebase, Koin, Compose, etc.) are defined in `custom-login/build.gradle.kts` and are transitively available.

---

## Initialization

Call `initLoginKoin` before any auth Composable is shown. If Koin is already running, `initLoginKoin` loads the login modules into the existing container instead of calling `startKoin` again. Apps that need full control can use `loginModules` directly.

### Android — `Application.onCreate()`

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        CustomLoginAndroid.initialize(this)

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
                phoneAuthConfig = PhoneAuthConfig(
                    defaultCountryCode = "+34",
                    timeoutSeconds = 90
                ),
                passwordPolicy = PasswordPolicyConfig(minLength = 8),
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

Attach the foreground `Activity` for Google, web OAuth, and phone flows:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        CustomLoginAndroid.attachActivity(this)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        CustomLoginAndroid.detachActivity(this)
    }
}
```

### Host-owned Koin

```kotlin
startKoin {
    androidContext(appContext)
    modules(appModule)
    modules(loginModules(loginConfig))
}
```

For a custom backend, pass your own provider:

```kotlin
modules(loginModules(loginConfig, authProvider = MyAuthProvider(api)))
```

When `authProvider` is provided, the library does not register the default `FirebaseAuthProvider`.

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

    NavHost(navController = navController, startDestination = AuthRoutesFlow) {

        authRoutesFlow(
            navController = navController,
            startDestination = WelcomeRoute,
            onNavigateToHome = {
                navController.navigate("home") {
                    popUpTo(AuthRoutesFlow) { inclusive = true }
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

Apple Sign-In uses a native AuthenticationServices handler on iOS and Firebase web OAuth on Android.
On iOS it requires the **Sign in with Apple** capability in Xcode and the entitlement in your app.
**iOS** — requires a Swift handler with nonce. See [Apple (iOS)](#apple-ios).

---

### GitHub

```kotlin
LoginLibraryConfig(
    githubOAuthConfig = OAuthProviderConfig(
        enabled = true,
        scopes = listOf("user:email", "read:user")
    )
)
```

Enable **GitHub** in the Firebase console (Authentication → Sign-in method → GitHub) and provide your GitHub OAuth App credentials there.

**Android** — handled via Firebase web OAuth (Chrome Custom Tab). No extra code required.
**iOS** — requires a Swift handler. See [GitHub / Microsoft / Twitter / Facebook (iOS)](#github--microsoft--twitter--facebook-ios).

---

### Microsoft

```kotlin
LoginLibraryConfig(
    microsoftOAuthConfig = OAuthProviderConfig(
        enabled = true,
        scopes = listOf("email", "profile"),
        customParameters = mapOf("tenant" to "common")
    )
)
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
LoginLibraryConfig(
    phoneEnabled = true,  // default
    phoneAuthConfig = PhoneAuthConfig(
        defaultCountryCode = "+34",
        timeoutSeconds = 90
    )
)
```

Enable **Phone** in Firebase console. The library provides a full Phone Auth screen with country code picker and OTP verification step.
**Android** — uses Firebase native `PhoneAuthProvider` with SIM-based instant verification support.
**iOS** — requires two Swift handlers. See [Phone OTP (iOS)](#phone-otp-ios).

### Password Policy

```kotlin
LoginLibraryConfig(
    passwordPolicy = PasswordPolicyConfig(
        minLength = 10,
        rejectBlank = true
    )
)
```

The policy is applied by the default Register and Reset Password screens.

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
GoogleSignInProviderIOS.Companion.shared.signInHandler = { clientId, completion in
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

// Wire this too, or the user can never switch accounts
GoogleSignInProviderIOS.Companion.shared.signOutHandler = {
    GIDSignIn.sharedInstance.signOut()
}
```

> `signOut()` in Firebase does not touch GoogleSignIn. `GIDSignIn.sharedInstance.currentUser` lives in the keychain and survives, so without the handler above the next sign-in silently reuses the previous account. It is the iOS half of what `clearSocialSignInState()` already does for Credential Manager on Android.

---

### Apple (iOS)

The reference implementation is in the demo app: **[`iosApp/iosApp/AppleSignInCoordinator.swift`](iosApp/iosApp/AppleSignInCoordinator.swift)**. Copy that file into your app and call `AppleSignInCoordinator.shared.register()` from `application(_:didFinishLaunchingWithOptions:)`. What follows is what it does and why.

**Token format** — call the completion with exactly one of:

| Value | Meaning |
|---|---|
| `idToken` + `\|\|\|rawNonce\|\|\|` + `rawNonce` | signed in, with replay protection |
| the same, plus `\|\|\|displayName\|\|\|` + `name` | signed in, and the library persists the name |
| `idToken` alone | no nonce — accepted for compatibility, **not** fit for production |
| `nil` | cancelled or failed |

```swift
import AuthenticationServices
import CryptoKit

final class AppleSignInCoordinator: NSObject {

    static let shared = AppleSignInCoordinator()

    private var currentRawNonce: String?
    private var completion: ((String?) -> Void)?
    private var controller: ASAuthorizationController?   // the system does not retain it

    func register() {
        AppleSignInProviderIOS.shared.signInHandler = { [weak self] _, completion in
            self?.start(completion: completion)
        }
    }

    private func start(completion: @escaping (String?) -> Void) {
        DispatchQueue.main.async { [weak self] in            // AuthenticationServices is main-thread only
            guard let self, self.completion == nil, let rawNonce = Self.randomNonceString() else {
                completion(nil)
                return
            }
            self.completion = completion
            self.currentRawNonce = rawNonce

            let request = ASAuthorizationAppleIDProvider().createRequest()
            request.requestedScopes = [.fullName, .email]
            request.nonce = Self.sha256(rawNonce)           // Apple gets the hash, Firebase the raw one

            let controller = ASAuthorizationController(authorizationRequests: [request])
            controller.delegate = self
            controller.presentationContextProvider = self
            self.controller = controller
            controller.performRequests()
        }
    }

    private func finish(_ token: String?) {                  // single exit point, called exactly once
        let completion = self.completion
        self.completion = nil
        self.currentRawNonce = nil
        self.controller = nil
        completion?(token)
    }
}

extension AppleSignInCoordinator: ASAuthorizationControllerDelegate {

    func authorizationController(controller: ASAuthorizationController,
                                 didCompleteWithAuthorization authorization: ASAuthorization) {
        guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
              let tokenData = credential.identityToken,
              let idToken = String(data: tokenData, encoding: .utf8),
              let rawNonce = currentRawNonce
        else {
            finish(nil)
            return
        }

        // Apple sends fullName only on the FIRST authorisation ever. Send it now or it is lost.
        var packed = "\(idToken)|||rawNonce|||\(rawNonce)"
        if let name = Self.displayName(from: credential.fullName) {
            packed += "|||displayName|||\(name)"
        }
        finish(packed)
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        finish(nil)   // includes ASAuthorizationError.canceled
    }
}

extension AppleSignInCoordinator: ASAuthorizationControllerPresentationContextProviding {

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        let scenes = UIApplication.shared.connectedScenes.compactMap { $0 as? UIWindowScene }
        let scene = scenes.first { $0.activationState == .foregroundActive } ?? scenes.first
        return scene?.keyWindow ?? scene?.windows.first ?? UIWindow()
    }
}

private extension AppleSignInCoordinator {

    /// Returns nil rather than falling back to a predictable value: a guessable nonce is worse
    /// than no sign-in.
    static func randomNonceString(length: Int = 32) -> String? {
        let charset = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        var result = ""
        while result.count < length {
            var randoms = [UInt8](repeating: 0, count: 16)
            guard SecRandomCopyBytes(kSecRandomDefault, randoms.count, &randoms) == errSecSuccess else {
                return nil
            }
            for random in randoms where result.count < length && random < charset.count {
                result.append(charset[Int(random)])
            }
        }
        return result
    }

    static func sha256(_ input: String) -> String {
        SHA256.hash(data: Data(input.utf8)).map { String(format: "%02x", $0) }.joined()
    }

    static func displayName(from components: PersonNameComponents?) -> String? {
        guard let components else { return nil }
        let formatter = PersonNameComponentsFormatter()
        formatter.style = .long
        let name = formatter.string(from: components).trimmingCharacters(in: .whitespacesAndNewlines)
        return name.isEmpty ? nil : name
    }
}
```

**Four failure modes that only show up at runtime:**

1. **Not retaining the `ASAuthorizationController`** — it is deallocated before the delegate fires and the sheet never appears.
2. **Not calling the completion on some path** (typically cancellation) — the Kotlin coroutine never resumes and the button spins forever.
3. **Reusing or dropping the nonce** — Firebase rejects the token, or worse, accepts a replayed one. Generate it per attempt with `SecRandomCopyBytes`; never `arc4random` or a UUID.
4. **Not sending the name** — Apple returns `fullName` only on the very first authorisation of each user. After that it is gone for good, and the user has to revoke the app under *Settings → Apple Account → Sign in with Apple* to get it back.

**Production checklist**

- [ ] **Sign in with Apple** capability enabled on the App ID in the Apple Developer portal, and the entitlement in the app (`iosApp/iosApp/iosApp.entitlements`).
- [ ] Apple provider enabled in the **Firebase console** for this project.
- [ ] The app's bundle id matches the iOS app registered in Firebase.
- [ ] If your app offers **account deletion**, wire `AppleSignInProviderIOS.shared.revokeHandler` too. App Review guideline 5.1.1(v) requires revoking the Apple token, not just deleting the Firebase user, and `deleteAccount()` calls this handler before deleting — but only if you set it, so an app that already ships a deletion flow does not suddenly get an Apple sheet. See the `.revoke` branch of `AppleSignInCoordinator`: it asks Apple to authorise again, because `revokeToken(withAuthorizationCode:)` needs a code that is fresh and single-use. A failed revocation is logged and the account is deleted anyway.

---

### GitHub / Microsoft / Twitter / Facebook (iOS)

Reference implementation: **[`iosApp/iosApp/FirebaseOAuthCoordinator.swift`](iosApp/iosApp/FirebaseOAuthCoordinator.swift)**, registered from the `AppDelegate`.

These four providers share the same pattern. Firebase handles the full OAuth flow from Swift. The Kotlin library only needs to know when it is complete.

Two things this flow needs that the sign-in code cannot do for you:

- **Retain the `OAuthProvider`** while the web flow runs. The SDK does not, and a provider held in a local variable is deallocated before the credential callback fires — the browser opens and nothing ever comes back.
- **Route the callback URL.** Register your app's `REVERSED_CLIENT_ID` (from `GoogleService-Info.plist`) as a URL scheme in `Info.plist`, and forward incoming URLs to `Auth.auth().canHandle(url)`. Under the SwiftUI lifecycle that means `.onOpenURL` on your scene — `application(_:open:options:)` is **not** called when scenes are in use, which is the most common reason these four providers hang.

Replace `"github.com"` with `"microsoft.com"`, `"twitter.com"`, or `"facebook.com"` as needed.

```swift
import FirebaseAuth

// GitHub example — call this at app startup
GitHubSignInProviderIOS.shared.signInHandler = { _, completion in
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
MicrosoftSignInProviderIOS.shared.signInHandler = { _, completion in
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
TwitterSignInProviderIOS.shared.signInHandler = { _, completion in
    let provider = OAuthProvider(providerID: "twitter.com")
    provider.getCredentialWith(nil) { credential, error in
        guard let credential = credential, error == nil else { completion(nil); return }
        Auth.auth().signIn(with: credential) { _, error in
            completion(error == nil ? "___PLATFORM_AUTH_COMPLETE___" : nil)
        }
    }
}

// Facebook
FacebookSignInProviderIOS.shared.signInHandler = { _, completion in
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

Reference implementation: **[`iosApp/iosApp/PhoneAuthCoordinator.swift`](iosApp/iosApp/PhoneAuthCoordinator.swift)**, registered from the `AppDelegate`.

Phone auth requires two handlers — one for sending the code and one for verifying it.

Before any SMS goes out, Firebase verifies that the request comes from your app: silently through an APNs push, and failing that through a reCAPTCHA page in the browser. So the `AppDelegate` also needs `setAPNSToken(_:type:)` in `didRegisterForRemoteNotificationsWithDeviceToken`, `canHandleNotification(_:)` in `didReceiveRemoteNotification`, and the same URL routing as the OAuth providers above for the reCAPTCHA callback. For the silent path you need the **Push Notifications** capability and an **APNs key uploaded to the Firebase console**; without it the flow still works, it just detours through the browser.

```swift
import FirebaseAuth

// Handler 1: send the OTP
PhoneAuthProviderIOS.shared.sendCodeHandler = { phoneNumber, completion in
    PhoneAuthProvider.provider().verifyPhoneNumber(phoneNumber, uiDelegate: nil) { verificationId, error in
        completion(verificationId)  // nil on failure
    }
}

// Handler 2: verify the OTP and sign in
PhoneAuthProviderIOS.shared.verifyCodeHandler = { verificationId, smsCode, completion in
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
