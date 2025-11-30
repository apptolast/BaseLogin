package com.apptolast.customlogin.domain.model

/**
 * Represents the UI configuration for the Login screen.
 * This allows the consuming application to customize the branding.
 *
 * @property appName The name of the application, displayed on the screen.
 * @property appLogoUrl The URL of the application's logo.
 */
data class LoginConfig(
    val appName: String = "Welcome Back",
    val appLogoUrl: String = ""
)
