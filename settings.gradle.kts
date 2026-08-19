pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // Add the GitLive repository for the Firebase plugin
        maven("https://gitlive.github.io/firebase-kotlin-sdk/maven/")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        // Also add the GitLive repository here for the libraries
        maven("https://gitlive.github.io/firebase-kotlin-sdk/maven/")
    }
}

rootProject.name = "BaseLogin"

// No `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` on purpose. Gradle generates one
// accessor class per project from its name, so `BaseLogin` (root) and `baselogin` (the library)
// both want `Base{l,L}oginProjectDependency.java`. On a case-insensitive filesystem — the macOS
// default — those are the same file, and the build dies compiling its own generated sources.
// Any spelling of the library name collides the same way, since the root is the repository name.
// Nothing used the accessors: both dependencies are declared as `project(":baselogin")`.

include(":composeApp")
include(":baselogin")
