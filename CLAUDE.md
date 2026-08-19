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

# iOS - build Kotlin framework.
# The task lives in :composeApp, not :custom-login — the library declares no
# binaries.framework of its own, it is export()ed through the demo's ComposeApp framework.
./gradlew :composeApp:linkDebugFrameworkIosArm64
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# iOS demo app - open iosApp/iosApp.xcodeproj (NOT a .xcworkspace: there is no CocoaPods)
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug \
  -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build
```

## iOS native dependencies

**Swift Package Manager only. No CocoaPods anywhere — do not reintroduce a `Podfile` or the
`kotlin.cocoapods` plugin** (removed in FLE-91).

`iosApp.xcodeproj` declares two SPM packages:

| Package | Products | Version rule |
|---|---|---|
| `firebase/firebase-ios-sdk` | `FirebaseCore`, `FirebaseAuth`, `FirebaseAppCheck` | `upToNextMinor` from **11.8.0** |
| `google/GoogleSignIn-iOS` | `GoogleSignIn` | `upToNextMajor` from **9.0.0** |

The 11.8.x pin is not arbitrary: GitLive's cinterop is built against that version of the Firebase iOS
SDK, and it is the same pin Fledge uses. Keeping them equal is what makes validating iOS here say
anything about consumers. If you bump `gitlive-firebase` in the catalog, check what Firebase iOS
version its cinterop targets and move the SPM pin with it.

`FirebaseAuth` is linked even though no Swift file imports it: the cinterop klib of
`dev.gitlive:firebase-auth` requires `-framework FirebaseAuth` at link time.

The Kotlin framework is compiled by the **`Compile Kotlin Framework`** build phase, which must stay
the *first* phase of the `iosApp` target and runs
`./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`. Without it Xcode links against a stale or
missing `ComposeApp.framework`, and the symptom — Kotlin changes not showing up — is confusing to
diagnose.

Signing reads `DEVELOPMENT_TEAM` from `${TEAM_ID}`, defined in `iosApp/Configuration/Config.xcconfig`.
Set it locally; **never hardcode a team id into `project.pbxproj`**.

The bundle id is fixed at `com.apptolast.login.Login` — deliberately **without** the `$(TEAM_ID)`
suffix the KMP template ships. It has to match three things at once: the iOS app registered in
Firebase, the iOS OAuth client behind the reversed client id in `Info.plist`, and
`MagicLinkConfig.iosBundleId` in `MainViewController.kt`. With the suffix, setting `TEAM_ID` locally
silently changed the app's identity and broke all three.

## Files the demo needs and the repository does not have

Both Firebase config files are in `.gitignore`, and **neither is in the tree**. Until you add them:

| File | Where | Without it |
|---|---|---|
| `composeApp/google-services.json` | `composeApp/` | `:composeApp:assembleDebug` **fails** at `processDebugGoogleServices` |
| `GoogleService-Info.plist` | `iosApp/iosApp/` | the app builds and **crashes at launch** in `FirebaseApp.configure()` |

Download both from the Firebase console for this project. The iOS one must be registered for the
bundle id above. `iosApp/iosApp/` is a `fileSystemSynchronizedGroup`, so dropping the plist in the
folder is enough — no `project.pbxproj` edit.

This is why `:custom-login` tasks are the ones to run for a quick check: they need neither file.

## Module Structure

```
custom-login/          ← Library module (the deliverable)
  src/commonMain/      ← Shared code
  src/androidMain/     ← Android-specific (Credential Manager, Logger)
  src/iosMain/         ← iOS-specific (Swift sign-in handlers, Logger)
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
- `data/firebase/` — the ports that keep the SDK testable (see below)
- `di/` — Koin modules: `dataModule`, `presentationModule`; `KoinInitializer.kt` with `LoginLibraryConfig`
- `presentation/` — ViewModels, Screens, Slots system

### Slots System
Consumers replace default UI components via `AuthScreenSlots` which contains per-screen `data class` with composable lambdas. All `submitButton` slots have the same signature: `(onClick: () -> Unit, isLoading: Boolean, enabled: Boolean, text: String)`.

Default implementations live in `presentation/slots/defaultslots/`.

### Library Entry Points
- Kotlin: `initLoginKoin(config: LoginLibraryConfig, appDeclaration?)` — call once at app start
- Compose: `AuthNavFlow(authSlots, onAuthSuccess)` from `RootNavGraph.kt`
- iOS helper: `GoogleSignInProviderIOS.shared.signInHandler` must be set from Swift

All six iOS providers are `object`s, so Swift reaches every one of them the same way —
`X.shared.…`. There is deliberately no second form: `GoogleSignInProviderIOS` used to be a
`class` whose companion Kotlin/Native exported separately, and the two coexisting spellings are
what let the README publish sign-in examples that did not compile. If a provider ever needs
per-call configuration, it travels as a `signIn(...)` parameter — never in a constructor.

### Dependency Injection
`LoginLibraryConfig` is registered as a Koin `single`. If `googleSignInConfig != null`, `GoogleSignInConfig` is also registered. `AuthRepositoryImpl` takes `AuthProvider` and `LoginLibraryConfig`.

### Ports over the SDK (FLE-90)
`FirebaseAuthProvider` does **not** hold a `FirebaseAuth`. Three ports sit in `data/firebase/`:

| Port | Wraps | Production impl |
|---|---|---|
| `FirebaseAuthGateway` | Firebase Auth | `GitLiveFirebaseAuthGateway` |
| `SocialTokenProvider` | `getSocialIdToken` | `PlatformSocialTokenProvider` |
| `SocialSignInStateCleaner` | `clearSocialSignInState` | `PlatformSocialSignInStateCleaner` |

All three exist for the same reason: `FirebaseAuth` is a platform `expect` class and the other two are
top-level `expect` functions, none of which can be faked from `commonTest`. Anything taking them
directly is untestable by construction — which is why this class had zero real coverage before.

**`GitLiveFirebaseAuthGateway` is the only file in `commonMain` allowed to import `dev.gitlive.*`.**
Verifiable rule: `grep -rn "dev.gitlive" custom-login/src/commonTest/` must return nothing.

Two invariants inside the adapter:

- **Lazy resolution.** `Firebase.auth` is a getter, never a field: Koin builds the graph before the
  host app initialises `FirebaseApp`. For the same reason no binding may be `createdAtStart` — in
  Koin 4.2.x `koinApplication { }` creates eager instances by default.
- **Every throwable becomes `FirebaseAuthFailure` with the message untouched**, because
  `mapFirebaseErrorMessage` classifies by message. Catching `FirebaseAuthException` alone is not
  enough: `FirebaseNetworkException` and `FirebaseTooManyRequestsException` extend
  `FirebaseException` and are its *siblings*, so they slip through and get mis-mapped as `Unknown`.

### Social token format
Packed strings crossing Swift → Kotlin. The separators are literal and shared with the host app's
Swift code, so changing them breaks every integration:

```
Google:  idToken|||accessToken|||<accessToken>
Apple:   idToken|||rawNonce|||<rawNonce>
Apple+:  idToken|||rawNonce|||<rawNonce>|||displayName|||<name>
```

The `displayName` segment is **optional and appended**, never a replacement: hosts already wired
against the two-segment form keep working. It matters because Apple sends the full name **only on the
very first authorisation** of each user — if it is not persisted then, it is lost for good.

### expect/actual
- `platform()` — platform name string
- `getSocialIdToken(provider)` — platform-specific OAuth token acquisition
- `clearSocialSignInState()` — clears Credential Manager's cached account on Android so the picker
  reappears after sign-out; no-op on iOS
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
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# Full check before opening a PR
./gradlew :custom-login:testDebugUnitTest :composeApp:linkDebugFrameworkIosSimulatorArm64 \
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
