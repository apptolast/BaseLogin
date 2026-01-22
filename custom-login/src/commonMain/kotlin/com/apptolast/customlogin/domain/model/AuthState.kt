package com.apptolast.customlogin.domain.model

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
