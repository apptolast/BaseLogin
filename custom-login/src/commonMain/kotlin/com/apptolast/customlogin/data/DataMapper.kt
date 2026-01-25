package com.apptolast.customlogin.data

import com.apptolast.customlogin.data.FirebaseAuthProvider.Companion.PROVIDER_ID
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.domain.model.UserSession
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GithubAuthProvider
import dev.gitlive.firebase.auth.GoogleAuthProvider

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
 * Maps a [FirebaseAuthException] to a domain [AuthError] object.
 */
internal fun FirebaseAuthException.toAuthError(): AuthError {
    val errorMessage = message ?: "Authentication error"
    return when {
        errorMessage.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                errorMessage.contains("INVALID_PASSWORD", ignoreCase = true) ||
                errorMessage.contains("wrong-password", ignoreCase = true) ->
            AuthError.InvalidCredentials()

        errorMessage.contains("USER_NOT_FOUND", ignoreCase = true) ||
                errorMessage.contains("user-not-found", ignoreCase = true) ->
            AuthError.UserNotFound()

        errorMessage.contains("EMAIL_EXISTS", ignoreCase = true) ||
                errorMessage.contains("email-already-in-use", ignoreCase = true) ->
            AuthError.EmailAlreadyInUse()

        errorMessage.contains("WEAK_PASSWORD", ignoreCase = true) ||
                errorMessage.contains("weak-password", ignoreCase = true) ->
            AuthError.WeakPassword()

        errorMessage.contains("INVALID_EMAIL", ignoreCase = true) ||
                errorMessage.contains("invalid-email", ignoreCase = true) ->
            AuthError.InvalidEmail()

        errorMessage.contains("TOO_MANY_ATTEMPTS", ignoreCase = true) ||
                errorMessage.contains("too-many-requests", ignoreCase = true) ->
            AuthError.TooManyRequests()

        errorMessage.contains("USER_DISABLED", ignoreCase = true) ||
                errorMessage.contains("user-disabled", ignoreCase = true) ->
            AuthError.UserDisabled()

        errorMessage.contains("NETWORK", ignoreCase = true) ||
                errorMessage.contains("network-request-failed", ignoreCase = true) ->
            AuthError.NetworkError(errorMessage, this)

        else -> AuthError.Unknown(errorMessage, this)
    }
}

/**
 * Maps an [IdentityProvider] and its corresponding token data to a Firebase [AuthCredential].
 */
internal fun IdentityProvider.toCredential(tokenData: String): AuthCredential? {
    return when (this) {
        is IdentityProvider.Google -> {
            // For iOS, the tokenData might be "idToken|||accessToken|||accessTokenValue"
            // For Android, it's just the idToken.
            val parts = tokenData.split("|||accessToken|||")
            val idToken = parts[0]
            val accessToken = parts.getOrNull(1)
            GoogleAuthProvider.credential(idToken, accessToken)
        }

        is IdentityProvider.GitHub -> GithubAuthProvider.credential(tokenData)
        else -> null // Other providers like Apple or Phone have different credential flows.
    }
}
