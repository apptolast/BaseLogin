package com.apptolast.customlogin.domain.model

/**
 * Configuration for Google Sign-In.
 *
 * @property webClientId The OAuth 2.0 Web Client ID from Firebase/Google Cloud Console.
 *                       This is required for both Android and iOS to get an ID token.
 * @property iosClientId The iOS Client ID from Firebase/Google Cloud Console.
 *                       Only required for iOS platform.
 */
data class GoogleSignInConfig(
    val webClientId: String,
    val iosClientId: String? = null
)