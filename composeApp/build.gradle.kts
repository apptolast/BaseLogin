import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.google.services) // Required for native Firebase SDK
    alias(libs.plugins.kotlinx.serialization) // Required for type-safe navigation in this module
}

kotlin {
    // This is a mandatory target for a KMP app module
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // Native iOS dependencies come from SPM in iosApp.xcodeproj, not from CocoaPods.
    // Xcode compiles this framework through the "Compile Kotlin Framework" build phase.
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            export(project(":baselogin"))
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                // Add dependency to our new login module
                api(project(":baselogin"))

                // Add navigation dependency for NavGraphBuilder
                implementation(libs.navigation.compose)

                // Material Icons
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)

                // Coil for KMP
                implementation(libs.coil.compose)
                implementation(libs.coil.network)

                // Koin
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.splashscreen)
                implementation(libs.koin.android)
//                implementation(libs.compose.ui.tooling)
//                debugImplementation(libs.compose.ui.test.manifest)

                // GitLive Firebase (common)
                implementation(libs.firebase.auth)

                // Firebase App Check (Android native)
                implementation(libs.firebase.appcheck.playintegrity)
                implementation(libs.firebase.appcheck.debug)
            }
        }
    }
}

android {
    namespace = "com.apptolast.login"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.apptolast.login"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

// No `ktlint { }` block on purpose: android=false, ignoreFailures=false and outputToConsole=true are
// already the plugin defaults, and every style decision lives in .editorconfig. Referencing
// KtlintExtension here would also break the JitPack build, because the Kotlin DSL compiles the whole
// script and the type would be missing from the classpath even inside this `if`.
if (rootProject.extra["ktlintEnabled"] as Boolean) {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}
