package com.apptolast.customlogin.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents an authenticated user session.
 * Contains all necessary information for maintaining authentication state.
 */
@Serializable
data class UserSession(
    val userId: String,
    val email: String?,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val providerId: String = "firebase",
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresAt: Long? = null,
    val metadata: Map<String, String> = emptyMap()
)
