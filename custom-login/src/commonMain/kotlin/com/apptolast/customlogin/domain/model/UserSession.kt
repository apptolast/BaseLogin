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
) {
    /**
     * Check if the session has expired.
     * Note: Uses kotlinx-datetime for multiplatform compatibility.
     */
    fun isExpired(currentTimeMillis: Long): Boolean {
        val expiry = expiresAt ?: return false
        return currentTimeMillis > expiry
    }

    companion object {
        /**
         * Create an empty session (for unauthenticated state).
         */
        val EMPTY = UserSession(userId = "", email = null)
    }
}

/**
 * Represents the current authentication state.
 */
sealed interface AuthState {
    /**
     * Authentication state is being determined.
     */
    data object Loading : AuthState

    /**
     * User is not authenticated.
     */
    data object Unauthenticated : AuthState

    /**
     * User is authenticated with an active session.
     */
    data class Authenticated(val session: UserSession) : AuthState

    /**
     * An error occurred while determining auth state.
     */
    data class Error(val error: AuthError) : AuthState
}
