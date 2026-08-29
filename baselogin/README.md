# BaseLogin — the `:baselogin` library module

Kotlin Multiplatform authentication UI and domain library for Android and iOS. It ships Firebase-backed defaults, but the auth backend can be replaced by providing your own `AuthProvider`.

## Dependency

For local development:

```kotlin
dependencies {
    implementation(project(":baselogin"))
}
```

Published artifact (Maven Central):

```kotlin
dependencies {
    implementation("io.github.apptolast:baselogin:2.0.0")
}
```

> **The coordinate changed in 2.0.0.** Up to 1.1.0 this was `com.github.apptolast.BaseLogin:baselogin`
> on JitPack; those versions stay there and nothing new is published under that group. Sonatype does
> not grant namespaces under `com.github.*`, so moving to Central meant a new group, not just a new
> version. See the [root README](../README.md#project-setup) for the migration notes.

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
