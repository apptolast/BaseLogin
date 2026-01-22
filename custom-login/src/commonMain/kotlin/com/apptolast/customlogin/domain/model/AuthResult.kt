package com.apptolast.customlogin.domain.model

/**
 * A sealed interface representing the result of an authentication operation.
 */
sealed interface AuthResult {

    /**
     * Represents a successful authentication.
     * @property session The authenticated user's session data.
     */
    data class Success(val session: UserSession) : AuthResult

    /**
     * Represents a failed authentication.
     * @property error The typed authentication error.
     */
    data class Failure(val error: AuthError) : AuthResult

    /**
     * Represents that email verification is required.
     */
    data object RequiresEmailVerification : AuthResult

    /**
     * Represents that password reset email was sent successfully.
     * This is a success state for the `sendPasswordResetEmail` operation.
     */
    data object PasswordResetSent : AuthResult

    /**
     * Represents that the password was successfully reset.
     * This is a terminal success state for the `confirmPasswordReset` operation.
     */
    data object PasswordResetSuccess : AuthResult
}
