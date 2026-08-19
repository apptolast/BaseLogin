import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinx.serialization)
    id("maven-publish")
}

group = "com.github.apptolast"
version = "1.1.0"

// Gradle names the Compose-resources archive task of each Apple target `<publication>` + this suffix.
private val RESOURCES_ZIP_TASK_SUFFIX = "ZipMultiplatformResourcesForPublication"

kotlin {
    androidTarget().apply {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // iOS targets - framework is exported through composeApp's binary framework (SPM in iosApp.xcodeproj)
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

    // Compose multiplatform resources (every string and drawable of this library) travel per Apple
    // target in a separate archive, published as
    // `<artifact>-<version>-kotlin_resources.kotlin_resources.zip` — classifier plus double extension.
    // JitPack REWRITES Gradle module metadata when it serves a build and flattens that name to
    // `<artifact>-<version>.zip`, while the uploaded file keeps its original name, so the URL the
    // metadata advertises 404s. Gradle resolves the resources variant leniently: the consumer build
    // stays GREEN and the app dies at runtime on iOS with MissingResourceException the first time a
    // composable of this library asks for a string. Android never noticed because its resources ride
    // inside the AAR assets. Publishing the same archive a second time under the flat name JitPack
    // advertises makes both coordinates resolvable — the untouched module metadata keeps working for
    // Maven-style consumers, and JitPack's rewritten URL now points at a file that exists.
    tasks.names.filter { it.endsWith(RESOURCES_ZIP_TASK_SUFFIX) }.forEach { taskName ->
        val publicationName = taskName.removeSuffix(RESOURCES_ZIP_TASK_SUFFIX)
        val publication = publishing.publications.findByName(publicationName) as? MavenPublication
            ?: return@forEach
        publication.artifact(tasks.named(taskName)) {
            classifier = null
            extension = "zip"
        }
    }
}

// No `ktlint { }` block on purpose: android=false, ignoreFailures=false and outputToConsole=true are
// already the plugin defaults, and every style decision lives in .editorconfig. Referencing
// KtlintExtension here would also break the JitPack build, because the Kotlin DSL compiles the whole
// script and the type would be missing from the classpath even inside this `if`.
if (rootProject.extra["ktlintEnabled"] as Boolean) {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}
