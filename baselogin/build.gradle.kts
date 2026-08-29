import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.maven.publish)
}

// `io.github.apptolast` and not `com.apptolast`: the namespace is verified against the GitHub
// organisation. It is also not `com.github.apptolast`, the old JitPack coordinate — Sonatype does
// not grant namespaces under `com.github.*`, so that one could never have reached Central.
group = "io.github.apptolast"
version = "2.0.0"

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
    packageOfResClass = "login.baselogin.generated.resources"
    generateResClass = always
}

android {
    namespace = "com.apptolast.baselogin"
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

    testOptions {
        unitTests {
            // `android.util.Log` is a stub in host tests and throws "not mocked" on every call.
            // The suite runs on the JVM and the library logs from its error paths, so without this
            // any test that walks a catch block fails on the logging rather than on the behaviour.
            isReturnDefaultValues = true
        }
    }
}

mavenPublishing {
    // Uploads to a *pending* deployment and stops there. Releasing is a separate, manual step in the
    // Central Portal, on purpose: a released version on Maven Central can never be deleted or
    // overwritten, while a pending one can still be dropped. Switching to
    // `publishAndReleaseToMavenCentral` would remove that safety net.
    publishToMavenCentral()
    signAllPublications()

    // The per-target artifacts derive from this one: baselogin-android, baselogin-iosarm64, …
    coordinates(group.toString(), "baselogin", version.toString())

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
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("apptolast")
                name.set("AppToLast")
                url.set("https://github.com/apptolast")
            }
        }
        scm {
            url.set("https://github.com/apptolast/BaseLogin")
            connection.set("scm:git:https://github.com/apptolast/BaseLogin.git")
            developerConnection.set("scm:git:ssh://git@github.com:apptolast/BaseLogin.git")
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
