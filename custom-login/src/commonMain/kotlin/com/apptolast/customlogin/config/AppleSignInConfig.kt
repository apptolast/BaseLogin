package com.apptolast.customlogin.config

/**
 * Configuration for Apple Sign-In.
 *
 * Apple Sign-In is natively supported on iOS via AuthenticationServices.
 * On Android, the Apple native SDK is not available; Apple Sign-In is iOS-only.
 *
 * No configuration fields are strictly required — the OS reads your app's
 * Sign in with Apple entitlement automatically. This class acts as a marker
 * to enable the provider in [com.apptolast.customlogin.di.LoginLibraryConfig].
 */
data class AppleSignInConfig(
    val scopes: List<String> = listOf("email", "name")
)