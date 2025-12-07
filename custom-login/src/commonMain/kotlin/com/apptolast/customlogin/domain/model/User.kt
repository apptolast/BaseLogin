package com.apptolast.customlogin.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents basic user information.
 * For full session data, use [UserSession].
 */
@Serializable
data class User(
    val uid: String,
    val email: String?,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false
) {
    /**
     * Convert to a UserSession with minimal data.
     */
    fun toSession(
        providerId: String = "firebase",
        accessToken: String? = null,
        refreshToken: String? = null,
        expiresAt: Long? = null
    ): UserSession = UserSession(
        userId = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
        isEmailVerified = isEmailVerified,
        providerId = providerId,
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresAt = expiresAt
    )
}