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

## KMP guardrails

This is a **published library**, not an app. Two consequences that override the usual app instincts:

- **The public API is a contract.** Anything not `internal` or `private` is consumed by apps pinned to
  a commit. Breaking a signature means every consumer has to change. Prefer adding over changing; when
  a break is unavoidable, say so explicitly in the spec.
- **Consumers cannot patch you.** A bug shipped here is a bug in every app until a new pin lands.

Source sets: code goes to `commonMain` by default. Drop to `androidMain`/`iosMain` only for platform
API, always via `expect/actual`. **No hand-written cinterop on iOS** — native iOS dependencies are
managed by package manager (SPM/CocoaPods) in the consuming app.

Layering: `domain/` holds interfaces and models and knows nothing about Firebase or Compose;
`data/` holds implementations; `presentation/` holds MVI. Constructor injection always.

Coroutines: no `runBlocking` or `GlobalScope` in production code. `viewModelScope` in ViewModels.

**Testability rule (the one that matters most here).** Types from a third-party SDK must not cross
into a testable seam. `dev.gitlive.firebase.*` classes are platform `expect` types that cannot be
faked from `commonTest`, so any class that takes one directly is untestable by construction — which is
exactly why `FirebaseAuthProvider` has no real coverage today. Put a **port** in `commonMain` that
speaks only in library-owned types, and keep the SDK handle inside a thin adapter. Verifiable rule:
**`commonTest` must never import `dev.gitlive.*`.**

## Testing

The suite lives in `custom-login/src/commonTest`. Stack: `kotlin.test` + `kotlinx-coroutines-test`
(`runTest`) + hand-written fakes. **No mocking libraries.**

Existing fakes to reuse rather than duplicate: `test/FakeAuthProvider.kt` and
`test/FakeAuthRepository.kt`.

Naming: backticked function describing the scenario, prefixed with the ticket when one applies, and a
body with `// Given` / `// When` / `// Then` blocks:

```kotlin
@Test
fun `FLE-90 apple sign in propagates the display name`() = runTest { ... }
```

⚠️ A fake asserting against another fake is not coverage. `FirebaseAuthProviderTest` currently
exercises `FakeAuthRepository`, so it proves nothing about `FirebaseAuthProvider`. Do not extend that
pattern.

## Commands

```bash
# Unit tests (host)
./gradlew :custom-login:testDebugUnitTest

# iOS compile + link
./gradlew :custom-login:linkDebugFrameworkIosSimulatorArm64

# Full check before opening a PR
./gradlew :custom-login:testDebugUnitTest :custom-login:linkDebugFrameworkIosSimulatorArm64 \
  :composeApp:assembleDebug --console=plain
```

`ktlint` is **not configured yet**; the `/validate` gate of the SDD harness runs `./gradlew
ktlintCheck`, so it has to be added before that gate can pass. When it is, its configuration (style,
exclusions, per-file rules) belongs in `.editorconfig`, not in `filter {}` blocks in Gradle.

## How this library is consumed

Published through **JitPack** as `com.github.apptolast.BaseLogin:baselogin` (see `jitpack.yml`, which
runs `:custom-login:publishToMavenLocal`; the artifactId is `baselogin` even though the module is
`custom-login`). Consumers pin a **commit SHA**, not a tag.

The loop is therefore: change here → commit → push → consumer bumps the pin → consumer rebuilds. It is
slower than a local project dependency, so batch related changes into one commit where it makes sense.

Known consumers: **Fledge** (`apptolast/Fledge`, pinned in `gradle/libs.versions.toml` as
`baselogin = "<sha>"`).

Integration seam: `loginDataModule(authProvider = null)` registers `Firebase.auth` plus the built-in
`FirebaseAuthProvider`; passing a non-null `authProvider` lets a consumer substitute its own. Prefer
making the built-in provider good enough that consumers do not need to.

## Workflow: SDD harness

This repo uses the **sdd-flow** plugin (enabled at user level, so it works here with nothing to
install). Seven phases with four human gates:

`/spec` → `/plan` → `/design-check` → `/test` → `/implement` → `/validate` → `/promote`

- Specs live in `specs/NNN-slug/{spec.md,plan.md}` and acceptance criteria are written in **Gherkin**
  (`Scenario [AC-01] … Given/When/Then`).
- **Strict TDD**: in `/test` the tests are written and confirmed **RED** for lack of implementation —
  not because of compilation errors. In `/implement` a hook **blocks** any edit to test files, so
  deleting or renaming a test file has to happen in `/test`.
- `/validate` requires `AC-xx → test(s)` traceability and blocks on implementation without a prior red
  test.
- Phase state is kept in `.claude/.sdd-state.json`.

Branches follow the harness convention `feature/NNN-slug`; commits are conventional and lowercase, and
reference the Jira ticket when there is one (`feat(fle-90): …`). Work driven by Fledge is tracked in
the **FLE** Jira project with the `baselogin` label, since this repo has no project of its own.
