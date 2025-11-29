# GEMINI.md

This file provides instructions for **Google Gemini Code Assist** when
generating or modifying code inside this repository.\
Gemini must follow these rules to maintain consistency with the
project's architecture, style, and patterns.

------------------------------------------------------------------------

# General Guidelines for Gemini

1.  **Do NOT invent APIs or frameworks**\
    Always use real Kotlin, KMP, Compose Multiplatform, Ktor, Koin, and
    Navigation Compose APIs.

2.  **Follow the project architecture strictly**\
    Code must follow MVVM, Repository pattern, and the established
    folder structure.

3.  **When unsure → ask for clarification**\
    Do not guess requirements or generate extra abstractions.

4.  **Use English for code comments**\
    User-facing text may remain in Spanish.

5.  **Generated code must compile**\
    Ensure imports, namespaces, serialization, DI modules, and
    dependencies match the project setup.

------------------------------------------------------------------------

# Project Overview

SpainDecides is a **Kotlin Multiplatform** project using:

-   Compose Multiplatform (shared UI)
-   Ktor Client (network)
-   Koin (dependency injection)
-   Kotlin Serialization
-   MVVM architecture

Namespace:

    com.apptolast.login

Targets: - Android (APK) - iOS (Xcode)

------------------------------------------------------------------------

# Build & Run

## Android

``` bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:assembleRelease
./gradlew :composeApp:installDebug
```

## iOS

Build through Xcode:

1.  Open `/iosApp`
2.  Build & run

Kotlin frameworks:

``` bash
./gradlew :composeApp:linkDebugFrameworkIosArm64
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

------------------------------------------------------------------------

# Architecture Rules (Gemini MUST follow these)

The project uses an MVVM layered structure:

    composeApp/src/commonMain/kotlin/com/apptolast/spaindecides/
    ├── presentation/
    │   ├── ui/
    │   │   ├── App.kt
    │   │   ├── screens/
    │   │   └── components/
    │   └── viewmodel/
    ├── domain/
    │   └── repository/
    ├── data/
    │   ├── model/
    │   ├── remote/
    │   │   ├── api/
    │   │   └── KtorClient.kt
    │   └── repository/
    ├── di/
    └── util/

### ViewModels

-   Always expose **StateFlow**
-   Use constructor injection
-   No navigation logic inside ViewModels
-   No Android-specific APIs in shared code

### Repositories

-   Interface in `domain/repository/`
-   Implementation in `data/repository/`

### API Layer

-   Must use Ktor Client
-   DTOs must use kotlinx.serialization

### UI Layer

-   Screens must be stateless composables
-   State must come from Koin-injected ViewModels using
    `koinViewModel()`

------------------------------------------------------------------------

# Dependency Injection (Koin)

Gemini must use Koin, not Hilt.

### Data module example:

``` kotlin
val dataModule = module {
    single { createHttpClient() }
    singleOf(::ApiService)
    singleOf(::RepositoryImpl) bind Repository::class
}
```

### Presentation module:

``` kotlin
val presentationModule = module {
    viewModelOf(::MainViewModel)
}
```

### ViewModel injection in Compose:

``` kotlin
val viewModel: MainViewModel = koinViewModel()
```

### iOS initialization:

``` swift
KoinInitializerKt.doInitKoin()
```

------------------------------------------------------------------------

# Navigation (Jetpack Compose + Kotlin Serialization)

Gemini must use type-safe navigation.

Routes:

``` kotlin
@Serializable object HomeRoute
@Serializable data class DetailRoute(val itemId: String)
```

Setup:

``` kotlin
NavHost(navController, startDestination = HomeRoute) {
    composable<HomeRoute> { HomeScreen() }
    composable<DetailRoute> { entry ->
        val args: DetailRoute = entry.toRoute()
        DetailScreen(itemId = args.itemId)
    }
}
```

------------------------------------------------------------------------

# Network Layer (Ktor Client)

Correct client configuration:

``` kotlin
fun createHttpClient() = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        })
    }
    install(Logging) { level = LogLevel.INFO }
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
    }
}
```

API example:

``` kotlin
class ApiService(private val http: HttpClient) {
    suspend fun getUsers(): Result<List<User>> = try {
        Result.success(http.get("users").body())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

------------------------------------------------------------------------

# Expect/Actual Rules

Gemini must use expect/actual **only when needed**, such as:

-   No multiplatform API exists
-   Platform-specific APIs are required (NSDate, Vibrations, etc.)

Gemini should prefer **interfaces** when possible.

------------------------------------------------------------------------

# UI & Theming

Theme files must be placed in:

    presentation/ui/theme/

Gemini must always use:

``` kotlin
MaterialTheme.colorScheme
MaterialTheme.typography
```

Never hardcode colors.

------------------------------------------------------------------------

# Adding New Code (Rules for Gemini)

  Type                   Location
  ---------------------- -------------------------------
  Screen                 `presentation/ui/screens/`
  Component              `presentation/ui/components/`
  ViewModel              `presentation/viewmodel/`
  Repository interface   `domain/repository/`
  Repository impl        `data/repository/`
  DTO                    `data/model/`
  API                    `data/remote/api/`
  DI module              `di/`

------------------------------------------------------------------------

# Gemini Forbidden Actions

❌ Inventing functions, libraries, or features\
❌ Hardcoding platform-specific code inside shared `commonMain`\
❌ Placing files in the wrong folder\
❌ Adding Hilt, Dagger, or Retrofit\
❌ Navigation logic in ViewModels\
❌ Extra layers not required by the project (use-case layer, mappers,
etc.)

------------------------------------------------------------------------

# Gemini Expected Behavior

✔ Code must compile\
✔ Match project architecture\
✔ Use provided dependencies\
✔ Use real APIs & imports\
✔ Follow MVVM strictly\
✔ Keep documentation in English

------------------------------------------------------------------------

# End of GEMINI.md
