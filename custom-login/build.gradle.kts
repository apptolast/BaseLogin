import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinx.serialization)
    id("maven-publish")
    alias(libs.plugins.ktlint.jlleitschuh)
}

group = "com.github.apptolast"
version = "1.1.0"

kotlin {
    androidTarget().apply {
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
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)

                // Koin
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)

                // Navigation
                implementation(libs.navigation.compose)

                // GitLive Firebase (common)
                implementation(libs.firebase.auth)
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
    packageOfResClass = "login.custom_login.generated.resources"
    generateResClass = always
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
            if (name == "kotlinMultiplatform") {
                artifactId = "baselogin"
            }
            pom {
                name.set("BaseLogin")
                description.set(
                    "Composable Kotlin Multiplatform authentication library with Firebase defaults and replaceable auth providers.",
                )
                url.set("https://github.com/apptolast/BaseLogin")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("apptolast")
                        name.set("AppToLast")
                    }
                }
                scm {
                    url.set("https://github.com/apptolast/BaseLogin")
                    connection.set("scm:git:https://github.com/apptolast/BaseLogin.git")
                    developerConnection.set("scm:git:ssh://git@github.com:apptolast/BaseLogin.git")
                }
            }
        }
    }
}

ktlint {
    android = false
    ignoreFailures = false
    outputToConsole = true
}
