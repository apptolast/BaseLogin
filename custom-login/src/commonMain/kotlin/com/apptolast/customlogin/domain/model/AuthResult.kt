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

/**
 * Typed authentication errors for better error handling.
 */
sealed class AuthError(open val message: String, open val cause: Throwable? = null) {
    data class InvalidCredentials(
        override val message: String = "Invalid email or password"
    ) : AuthError(message)

    data class UserNotFound(
        override val message: String = "User not found"
    ) : AuthError(message)

    data class EmailAlreadyInUse(
        override val message: String = "Email is already registered"
    ) : AuthError(message)

    data class WeakPassword(
        override val message: String = "Password is too weak"
    ) : AuthError(message)

    data class InvalidEmail(
        override val message: String = "Invalid email format"
    ) : AuthError(message)

    data class InvalidResetCode(
        override val message: String = "Invalid or expired password reset code"
    ) : AuthError(message)

    data class NetworkError(
        override val message: String = "Network error occurred",
        override val cause: Throwable? = null
    ) : AuthError(message, cause)

    data class SessionExpired(
        override val message: String = "Session has expired"
    ) : AuthError(message)

    data class TooManyRequests(
        override val message: String = "Too many requests. Please try again later"
    ) : AuthError(message)

    data class UserDisabled(
        override val message: String = "This account has been disabled"
    ) : AuthError(message)

    data class OperationNotAllowed(
        override val message: String = "Operation not allowed"
    ) : AuthError(message)

    data class Unknown(
        override val message: String = "An unknown error occurred",
        override val cause: Throwable? = null
    ) : AuthError(message, cause)
}
