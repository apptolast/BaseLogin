# BaseLogin — the `:baselogin` library module

Kotlin Multiplatform authentication UI and domain library for Android and iOS. It ships Firebase-backed defaults, but the auth backend can be replaced by providing your own `AuthProvider`.

## Dependency

For local development:

```kotlin
dependencies {
    implementation(project(":baselogin"))
}
```

Published artifact (JitPack):

```kotlin
dependencies {
    implementation("com.github.apptolast.BaseLogin:baselogin:2.0.0")
}
```

> The group must carry the **repository** name after the user name —
> `com.github.apptolast.BaseLogin`, capitalised — even though this module declares
> `group = "com.github.apptolast"`. JitPack republishes multi-module builds under
> `com.github.<user>.<repo>`. The two-segment form `com.github.apptolast:baselogin` still answers
> with a POM, so it does not fail as a 404: it resolves to JitPack's aggregate POM, with no variant
> metadata and an empty KMP metadata jar, and surfaces much later as unresolved symbols. See the
> [root README](../README.md#project-setup) for the full table.

## Koin Integration

If the host app does not start Koin elsewhere, use the convenience initializer:

```kotlin
initLoginKoin(
    config = LoginLibraryConfig(
        googleSignInConfig = GoogleSignInConfig("YOUR_WEB_CLIENT_ID"),
        phoneAuthConfig = PhoneAuthConfig(defaultCountryCode = "+34", timeoutSeconds = 90),
        passwordPolicy = PasswordPolicyConfig(minLength = 8),
        githubOAuthConfig = OAuthProviderConfig(
            enabled = true,
            scopes = listOf("user:email", "read:user")
        )
    )
)
```

If the host app already owns Koin startup, do not call `startKoin` twice. Add the library modules to the existing container:

```kotlin
startKoin {
    modules(appModule)
    modules(loginModules(loginConfig))
}
```

For runtime loading or tests:

```kotlin
val modules = loadLoginKoinModules(loginConfig)
unloadLoginKoinModules(modules)
```

## Custom Auth Provider

Pass a custom provider when your backend is not Firebase:

```kotlin
val customProvider = MyAuthProvider(api)

startKoin {
    modules(appModule)
    modules(loginModules(loginConfig, authProvider = customProvider))
}
```

The library will not register `Firebase.auth` when `authProvider` is provided.

If the provider supports an SMS timeout, implement `PhoneAuthTimeoutProvider` as well. Otherwise the library falls back to `AuthProvider.sendPhoneOtp(phoneNumber)`.

## Android Setup

Initialize the Android integration helper from `Application.onCreate()`:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        BaseLoginAndroid.initialize(this)
        initLoginKoin(config = loginConfig) {
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}
```

Attach the foreground activity for Google, web OAuth, and phone auth flows:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        BaseLoginAndroid.attachActivity(this)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        BaseLoginAndroid.detachActivity(this)
    }
}
```

## Navigation

Use `authRoutesFlow` inside your existing Compose Navigation graph:

```kotlin
NavHost(navController = navController, startDestination = AuthRoutesFlow) {
    authRoutesFlow(
        navController = navController,
        startDestination = WelcomeRoute,
        onNavigateToHome = { navController.navigate("home") }
    )
}
```

UI customization is exposed through `AuthScreenSlots`.

## AGP 9

This repository currently stays on AGP `8.13.2`. The Google AGP 9 migration recipe requires running Android Studio's AGP Upgrade Assistant before applying the Gradle DSL migration manually.
