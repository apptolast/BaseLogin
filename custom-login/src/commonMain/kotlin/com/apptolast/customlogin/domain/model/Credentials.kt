package com.apptolast.customlogin.domain.model

import kotlinx.serialization.Serializable

/**
 * Credentials used for authentication.
 */
sealed interface Credentials {
    /**
     * Email and password authentication.
     */
    data class EmailPassword(
        val email: String,
        val password: String
    ) : Credentials

    /**
     * OAuth token from external provider (Google, Apple, etc.)
     */
    data class OAuthToken(
        val providerId: String,
        val idToken: String,
        val accessToken: String? = null
    ) : Credentials

    /**
     * Refresh token for session renewal.
     */
    data class RefreshToken(
        val token: String
    ) : Credentials
}

/**
 * Data required for user registration.
 */
@Serializable
data class SignUpData(
    val email: String,
    val password: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Data required for password reset confirmation.
 */
@Serializable
data class PasswordResetData(
    val code: String,
    val newPassword: String
)
