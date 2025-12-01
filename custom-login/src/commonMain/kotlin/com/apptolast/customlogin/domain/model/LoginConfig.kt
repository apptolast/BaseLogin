package com.apptolast.customlogin.domain.model

/**
 * Represents the UI configuration for the Login screen.
 * This allows the consuming application to customize the branding.
 *
 * @property appName The name of the application, displayed on the screen.
 * @property appLogoUrl The URL of the application's logo.
 */
// customlogin/src/commonMain/kotlin/.../domain/model/LoginConfig.kt
data class LoginConfig(
    val appName: String = "App to Last",
    val appLogoUrl: String = "",
    val subtitle: String = "Sign in to continue",
    val emailEnabled: Boolean = true,
    val googleEnabled: Boolean = false
)
