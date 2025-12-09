import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.google.services) // Required for native Firebase SDK
    alias(libs.plugins.kotlin.cocoapods)
    alias(libs.plugins.kotlinx.serialization) // Required for type-safe navigation in this module
}

kotlin {
    // This is a mandatory target for a KMP app module
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                
                // Add dependency to our new login module
                api(project(":custom-login"))

                // Add navigation dependency for NavGraphBuilder
                implementation(libs.navigation.compose)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.koin.android)

                // GitLive Firebase (common)
                implementation(libs.firebase.auth)

                // Firebase App Check (Android native)
                implementation(libs.firebase.appcheck.playintegrity)
                implementation(libs.firebase.appcheck.debug)
            }
        }
    }

    cocoapods {
        name = "ComposeApp"
        version = "1.0.0"
        summary = "Login con firebase"
        homepage = "https://apptolast.com"
        ios.deploymentTarget = "26.1"

        framework {
            baseName = "ComposeApp"
            isStatic = true
            export(project(":custom-login"))
        }

        pod("FirebaseCore") {            
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        pod("FirebaseAuth") {            
            extraOpts += listOf("-compiler-option", "-fmodules")
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
