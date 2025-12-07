package com.apptolast.customlogin.data.provider

import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.SignUpData
import com.apptolast.customlogin.domain.model.UserSession
import com.apptolast.customlogin.domain.provider.AuthProvider
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Firebase Authentication provider implementation.
 * Uses GitLive Firebase SDK for multiplatform support.
 */
class FirebaseAuthProvider(
    private val firebaseAuth: FirebaseAuth
) : AuthProvider {

    override val id: String = PROVIDER_ID
    override val displayName: String = "Firebase"

    override suspend fun signIn(credentials: Credentials): AuthResult {
        return when (credentials) {
            is Credentials.EmailPassword -> signInWithEmail(credentials)
            is Credentials.OAuthToken -> signInWithOAuth(credentials)
            is Credentials.RefreshToken -> refreshSession()
        }
    }

    private suspend fun signInWithEmail(credentials: Credentials.EmailPassword): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(
                credentials.email,
                credentials.password
            )
            result.user?.toUserSession()?.let { session ->
                AuthResult.Success(session)
            } ?: AuthResult.Failure(AuthError.Unknown("No user returned after sign in"))
        } catch (e: FirebaseAuthException) {
            AuthResult.Failure(e.toAuthError())
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.Unknown(e.message ?: "Sign in failed", e))
        }
    }

    private suspend fun signInWithOAuth(credentials: Credentials.OAuthToken): AuthResult {
        return try {
            // For OAuth tokens (Google, Apple, etc.), we need to create credentials
            // This requires platform-specific handling through expect/actual
            AuthResult.Failure(AuthError.OperationNotAllowed("OAuth sign-in requires platform-specific implementation"))
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.Unknown(e.message ?: "OAuth sign in failed", e))
        }
    }

    override suspend fun signUp(data: SignUpData): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(
                data.email,
                data.password
            )
            result.user?.let { user ->
                // Update display name if provided
                if (!data.displayName.isNullOrBlank()) {
                    user.updateProfile(displayName = data.displayName)
                }
                user.toUserSession()?.let { session ->
                    AuthResult.Success(session)
                } ?: AuthResult.Failure(AuthError.Unknown("No user returned after registration"))
            } ?: AuthResult.Failure(AuthError.Unknown("No user returned after registration"))
        } catch (e: FirebaseAuthException) {
            AuthResult.Failure(e.toAuthError())
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.Unknown(e.message ?: "Registration failed", e))
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            firebaseAuth.sendPasswordResetEmail(email)
            AuthResult.PasswordResetSent
        } catch (e: FirebaseAuthException) {
            AuthResult.Failure(e.toAuthError())
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.Unknown(e.message ?: "Failed to send reset email", e))
        }
    }

    override suspend fun confirmPasswordReset(code: String, newPassword: String): AuthResult {
        return try {
            firebaseAuth.confirmPasswordReset(code, newPassword)
            // After confirming, user needs to sign in again
            AuthResult.PasswordResetSent
        } catch (e: FirebaseAuthException) {
            AuthResult.Failure(e.toAuthError())
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.Unknown(e.message ?: "Failed to reset password", e))
        }
    }

    override fun observeAuthState(): Flow<AuthState> {
        return firebaseAuth.authStateChanged.map { user ->
            if (user != null) {
                user.toUserSession()?.let { session ->
                    AuthState.Authenticated(session)
                } ?: AuthState.Unauthenticated
            } else {
                AuthState.Unauthenticated
            }
        }
    }

    override suspend fun refreshSession(): AuthResult {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                // Force token refresh
                val token = user.getIdToken(true)
                user.toUserSession(accessToken = token)?.let { session ->
                    AuthResult.Success(session)
                } ?: AuthResult.Failure(AuthError.SessionExpired())
            } else {
                AuthResult.Failure(AuthError.SessionExpired())
            }
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.SessionExpired(e.message ?: "Session expired"))
        }
    }

    override suspend fun isSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        return try {
            firebaseAuth.currentUser?.getIdToken(forceRefresh)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDisplayName(displayName: String): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.updateProfile(displayName = displayName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEmail(newEmail: String): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.updateEmail(newEmail)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.updatePassword(newPassword)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.sendEmailVerification()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reauthenticate(credentials: Credentials): AuthResult {
        return when (credentials) {
            is Credentials.EmailPassword -> {
                try {
                    val user = firebaseAuth.currentUser
                        ?: return AuthResult.Failure(AuthError.SessionExpired())
                    // Re-sign in the user
                    firebaseAuth.signInWithEmailAndPassword(credentials.email, credentials.password)
                    user.toUserSession()?.let { session ->
                        AuthResult.Success(session)
                    } ?: AuthResult.Failure(AuthError.Unknown("Reauthentication failed"))
                } catch (e: FirebaseAuthException) {
                    AuthResult.Failure(e.toAuthError())
                } catch (e: Exception) {
                    AuthResult.Failure(AuthError.Unknown(e.message ?: "Reauthentication failed", e))
                }
            }

            else -> AuthResult.Failure(AuthError.OperationNotAllowed("Unsupported credentials for reauthentication"))
        }
    }

    /**
     * Convert FirebaseUser to UserSession.
     */
    private suspend fun FirebaseUser.toUserSession(accessToken: String? = null): UserSession? {
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
                expiresAt = null
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convert FirebaseAuthException to AuthError.
     */
    private fun FirebaseAuthException.toAuthError(): AuthError {
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

    companion object {
        const val PROVIDER_ID = "firebase"
    }
}
