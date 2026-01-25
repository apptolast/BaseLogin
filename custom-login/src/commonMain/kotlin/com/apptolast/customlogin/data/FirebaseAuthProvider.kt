package com.apptolast.customlogin.data

import com.apptolast.customlogin.domain.AuthProvider
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.domain.model.SignUpData
import com.apptolast.customlogin.getSocialIdToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Firebase Authentication provider implementation.
 * Uses GitLive Firebase SDK for multiplatform support.
 */
class FirebaseAuthProvider(
    private val authService: FirebaseAuthService
) : AuthProvider {

    override val id: String = PROVIDER_ID
    override val displayName: String = "Firebase"

    override suspend fun signIn(credentials: Credentials): AuthResult {
        return when (credentials) {
            is Credentials.EmailPassword -> signInWithEmail(credentials)
            is Credentials.OAuthToken -> signInWithOAuth(credentials.provider)
            is Credentials.RefreshToken -> refreshSession()
        }
    }

    private suspend fun signInWithEmail(credentials: Credentials.EmailPassword): AuthResult {
        return try {
            val result = authService.signInWithEmailAndPassword(
                credentials.email,
                credentials.password
            )
            result.user?.toUserSession()?.let { session ->
                AuthResult.Success(session)
            } ?: AuthResult.Failure(AuthError.Unknown("No user returned after sign in"))
        } catch (e: Exception) {
            AuthResult.Failure(e.toAuthError())
        }
    }

    private suspend fun signInWithOAuth(provider: IdentityProvider): AuthResult {
        return try {
            val idToken = getSocialIdToken(provider)
                ?: return AuthResult.Failure(AuthError.Unknown("Social sign-in cancelled or failed."))

            val credential = provider.toCredential(idToken)
                ?: return AuthResult.Failure(AuthError.OperationNotAllowed("Provider not supported: ${provider.id}"))

            val result = authService.signInWithCredential(credential)
            result.user?.toUserSession()?.let { session ->
                AuthResult.Success(session)
            } ?: AuthResult.Failure(AuthError.Unknown("No user returned after social sign in"))
        } catch (e: Exception) {
            AuthResult.Failure(e.toAuthError())
        }
    }

    override suspend fun signUp(data: SignUpData): AuthResult {
        return try {
            val result = authService.createUserWithEmailAndPassword(
                data.email,
                data.password
            )
            result.user?.let { user ->
                if (!data.displayName.isNullOrBlank()) {
                    user.updateProfile(displayName = data.displayName)
                }
                user.toUserSession()?.let { session ->
                    AuthResult.Success(session)
                } ?: AuthResult.Failure(AuthError.Unknown("No user returned after registration"))
            } ?: AuthResult.Failure(AuthError.Unknown("No user returned after registration"))
        } catch (e: Exception) {
            AuthResult.Failure(e.toAuthError())
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            authService.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            authService.sendPasswordResetEmail(email)
            AuthResult.PasswordResetSent
        } catch (e: Exception) {
            AuthResult.Failure(e.toAuthError())
        }
    }

    override suspend fun confirmPasswordReset(code: String, newPassword: String): AuthResult {
        return try {
            authService.confirmPasswordReset(code, newPassword)
            AuthResult.PasswordResetSuccess
        } catch (e: Exception) {
            AuthResult.Failure(e.toAuthError())
        }
    }

    override fun observeAuthState(): Flow<AuthState> {
        return authService.authStateChanged
            .map { user ->
                user?.toUserSession()?.let { AuthState.Authenticated(it) }
                    ?: AuthState.Unauthenticated
            }
            .onStart { emit(AuthState.Loading) }
            .catch { e ->
                emit(AuthState.Error(e.toAuthError()))
            }
    }

    override suspend fun refreshSession(): AuthResult {
        return try {
            val user = authService.currentUser
            if (user != null) {
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
        return authService.currentUser != null
    }

    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        return try {
            authService.currentUser?.getIdToken(forceRefresh)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            authService.currentUser?.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDisplayName(displayName: String): Result<Unit> {
        return try {
            authService.currentUser?.updateProfile(displayName = displayName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEmail(newEmail: String): Result<Unit> {
        return try {
            authService.currentUser?.verifyBeforeUpdateEmail(newEmail)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            authService.currentUser?.updatePassword(newPassword)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            authService.currentUser?.sendEmailVerification()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reauthenticate(credentials: Credentials): AuthResult {
        return AuthResult.Failure(AuthError.OperationNotAllowed("Reauthentication not implemented in this provider"))
    }

    companion object {
        const val PROVIDER_ID = "firebase"
    }
}
