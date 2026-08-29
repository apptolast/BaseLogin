// ktlint is a developer-only tool and is applied through `buildscript` instead of the `plugins {}`
// block so that it can be skipped entirely.
//
// Reason: publishing must never depend on a linter. It did once, under JitPack: the builder could
// not resolve the ktlint plugin from the Gradle Plugin Portal, the whole publication failed, and
// every consumer pinning a newer commit broke. JitPack is gone as of 2.0.0 — the release job
// publishes to Maven Central instead — but the escape hatch stays and that job passes
// `-PskipKtlint=true`. `JITPACK` is never set any more; it is kept so an old tag still builds if
// anyone ever requests one from JitPack.
//
// Locally nothing changes: ktlintCheck and ktlintFormat work as usual.
val ktlintEnabled: Boolean = System.getenv("JITPACK") == null &&
    providers.gradleProperty("skipKtlint").orNull != "true"

extra["ktlintEnabled"] = ktlintEnabled

buildscript {
    val enabled = System.getenv("JITPACK") == null &&
        providers.gradleProperty("skipKtlint").orNull != "true"
    if (enabled) {
        repositories { gradlePluginPortal() }
        dependencies { classpath("org.jlleitschuh.gradle:ktlint-gradle:14.0.1") }
    }
}

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.google.services) apply false
}
