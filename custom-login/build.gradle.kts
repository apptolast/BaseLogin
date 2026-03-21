import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinx.serialization) // Required for @Serializable
    id("maven-publish")
}

group = "com.github.apptolast"
version = "1.0.2"

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // iOS targets - framework is exported through composeApp's CocoaPods
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.resources)
                implementation(compose.uiToolingPreview)

                // Material Icons
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)

                // Koin
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.koin.compose.viewmodel.navigation)

                // Navigation
                implementation(libs.navigation.compose)

                // GitLive Firebase (common)
                implementation(libs.firebase.auth)

                // Coil for KMP
                implementation(libs.coil.compose)
                implementation(libs.coil.network)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val androidMain by getting {
            dependencies {
                // Google Sign-In via Credential Manager
                implementation(libs.androidx.credentials)
                implementation(libs.androidx.credentials.play.services.auth)
                implementation(libs.googleid)
            }
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.apptolast.customlogin.generated.resources"
    generateResClass.set(org.jetbrains.compose.resources.GenerateResClass.Always)
}

android {
    namespace = "com.apptolast.customlogin"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications.withType<MavenPublication> {
            artifactId = "baselogin"
        }
    }
}
