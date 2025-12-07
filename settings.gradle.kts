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
        // Also add the GitLive repository here for the libraries
        maven("https://gitlive.github.io/firebase-kotlin-sdk/maven/")
    }
}

rootProject.name = "Login"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":composeApp")
include(":custom-login")
