package com.apptolast.customlogin.data

import com.apptolast.customlogin.data.FirebaseAuthProvider.Companion.PROVIDER_ID
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.UserSession
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.auth.FirebaseUser

/**
 * Maps a [FirebaseUser] to a domain [UserSession] object.
 */
internal suspend fun FirebaseUser.toUserSession(accessToken: String? = null): UserSession? {
    return try {
        UserSession(
            userId = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoURL,
            isEmailVerified = isEmailVerified,
            providerId = PROVIDER_ID,
            accessToken = accessToken ?: getIdToken(false),
            refreshToken = null, // Firebase manages refresh internally
            expiresAt = null,
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Maps a Firebase error message string to a domain [AuthError].
 * Extracted as a pure function for testability.
 *
 * Handles both Firebase REST API codes (e.g. `INVALID_CREDENTIAL`) and
 * native Android SDK codes (e.g. `ERROR_WRONG_PASSWORD`) and web SDK codes
 * (e.g. `wrong-password`).
 */
internal fun mapFirebaseErrorMessage(errorMessage: String): AuthError {
    return when {
        // Unified credential error (Firebase REST API since 2023) + legacy codes
        errorMessage.containsAny(
            "INVALID_CREDENTIAL", "ERROR_INVALID_CREDENTIAL",
            "INVALID_LOGIN_CREDENTIALS",
            "INVALID_PASSWORD", "wrong-password", "ERROR_WRONG_PASSWORD",
        ) -> AuthError.InvalidCredentials()

        errorMessage.containsAny(
            "USER_NOT_FOUND", "user-not-found", "ERROR_USER_NOT_FOUND",
        ) -> AuthError.UserNotFound()

        errorMessage.containsAny(
            "EMAIL_EXISTS", "email-already-in-use", "ERROR_EMAIL_ALREADY_IN_USE",
        ) -> AuthError.EmailAlreadyInUse()

        errorMessage.containsAny(
            "WEAK_PASSWORD", "weak-password", "ERROR_WEAK_PASSWORD",
        ) -> AuthError.WeakPassword()

        errorMessage.containsAny(
            "INVALID_EMAIL", "invalid-email", "ERROR_INVALID_EMAIL",
        ) -> AuthError.InvalidEmail()

        errorMessage.containsAny(
            "EXPIRED_OOB_CODE", "expired-action-code",
            "INVALID_OOB_CODE", "invalid-action-code",
        ) -> AuthError.InvalidResetCode()

        errorMessage.containsAny(
            "TOO_MANY_ATTEMPTS", "too-many-requests", "ERROR_TOO_MANY_REQUESTS",
        ) -> AuthError.TooManyRequests()

        errorMessage.containsAny(
            "USER_DISABLED", "user-disabled", "ERROR_USER_DISABLED",
        ) -> AuthError.UserDisabled()

        errorMessage.containsAny(
            "OPERATION_NOT_ALLOWED", "operation-not-allowed", "ERROR_OPERATION_NOT_ALLOWED",
        ) -> AuthError.OperationNotAllowed()

        errorMessage.containsAny(
            "NETWORK", "network-request-failed", "ERROR_NETWORK_REQUEST_FAILED",
        ) -> AuthError.NetworkError(errorMessage)

        else -> AuthError.Unknown(errorMessage)
    }
}

private fun String.containsAny(vararg values: String): Boolean =
    values.any { this.contains(it, ignoreCase = true) }

/**
 * Maps a [FirebaseAuthException] to a domain [AuthError] object.
 * Falls back to [cause] message if the primary message is missing.
 */
internal fun FirebaseAuthException.toAuthError(): AuthError {
    val message = (message?.trim()?.ifBlank { null } ?: cause?.message?.trim())
        ?: "Authentication error"
    return mapFirebaseErrorMessage(message)
}
