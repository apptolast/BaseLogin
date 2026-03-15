package com.apptolast.customlogin.data

import com.apptolast.customlogin.domain.AuthProvider
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.domain.model.PhoneAuthResult
import com.apptolast.customlogin.domain.model.SignUpData
import com.apptolast.customlogin.SocialTokenResult
import com.apptolast.customlogin.getSocialIdToken
import com.apptolast.customlogin.sendPhoneVerificationCode
import com.apptolast.customlogin.verifyPhoneCode
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.auth.ActionCodeSettings
import dev.gitlive.firebase.auth.GithubAuthProvider
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

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
            is Credentials.OAuthToken -> signInWithOAuth(credentials.provider)
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

    private suspend fun signInWithOAuth(provider: IdentityProvider): AuthResult {
        return try {
            when (val tokenResult = getSocialIdToken(provider)) {
                null -> AuthResult.Failure(AuthError.Unknown("Social sign-in cancelled or failed."))
                // Platform (e.g. Android web OAuth) already completed the Firebase sign-in.
                is SocialTokenResult.PlatformHandled -> refreshSession()
                is SocialTokenResult.Token -> {
                    val credential = provider.toCredential(tokenResult.value)
                        ?: return AuthResult.Failure(
                            AuthError.OperationNotAllowed("Provider not supported: ${provider.id}")
                        )
                    val result = firebaseAuth.signInWithCredential(credential)
                    result.user?.toUserSession()?.let { session ->
                        AuthResult.Success(session)
                    } ?: AuthResult.Failure(AuthError.Unknown("No user returned after social sign in"))
                }
            }
        } catch (e: FirebaseAuthException) {
            AuthResult.Failure(e.toAuthError())
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
            AuthResult.PasswordResetSuccess
        } catch (e: FirebaseAuthException) {
            AuthResult.Failure(e.toAuthError())
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.Unknown(e.message ?: "Failed to reset password", e))
        }
    }

    override fun observeAuthState(): Flow<AuthState> {
        return firebaseAuth.authStateChanged
            .map { user ->
                user?.toUserSession()?.let { AuthState.Authenticated(it) }
                    ?: AuthState.Unauthenticated
            }
            .onStart { emit(AuthState.Loading) }
            .catch { e ->
                val error = (e as? FirebaseAuthException)?.toAuthError() ?: AuthError.Unknown(
                    e.message ?: "An unknown error occurred", e
                )
                emit(AuthState.Error(error))
            }
    }

    override suspend fun refreshSession(): AuthResult {
        return try {
            val user = firebaseAuth.currentUser
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
        return try {
            val user = firebaseAuth.currentUser
                ?: return AuthResult.Failure(AuthError.UserNotFound())

            val firebaseCredential = when (credentials) {
                is Credentials.EmailPassword -> EmailAuthProvider.credential(
                    credentials.email,
                    credentials.password
                )

                is Credentials.OAuthToken -> {
                    val tokenResult = getSocialIdToken(credentials.provider)
                        ?: return AuthResult.Failure(AuthError.Unknown("Social sign-in cancelled."))
                    if (tokenResult is SocialTokenResult.PlatformHandled) {
                        // Platform completed sign-in — re-read the current session
                        return refreshSession()
                    }
                    credentials.provider.toCredential((tokenResult as SocialTokenResult.Token).value)
                        ?: return AuthResult.Failure(
                            AuthError.OperationNotAllowed("Provider not supported for reauthentication: ${credentials.provider.id}")
                        )
                }

                is Credentials.RefreshToken -> return AuthResult.Failure(
                    AuthError.OperationNotAllowed("RefreshToken credentials cannot be used for reauthentication")
                )
            }

            user.reauthenticate(firebaseCredential)
            user.toUserSession()?.let { AuthResult.Success(it) }
                ?: AuthResult.Failure(AuthError.Unknown("No user session after reauthentication"))
        } catch (e: FirebaseAuthException) {
            AuthResult.Failure(e.toAuthError())
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.Unknown(e.message ?: "Reauthentication failed", e))
        }
    }

    override suspend fun sendPhoneOtp(phoneNumber: String): PhoneAuthResult {
        return sendPhoneVerificationCode(phoneNumber)
    }

    override suspend fun verifyPhoneOtp(verificationId: String, otpCode: String): AuthResult {
        return verifyPhoneCode(verificationId, otpCode)
    }

    override suspend fun sendMagicLink(email: String, continueUrl: String, iosBundleId: String?): AuthResult {
        return try {
            val settings = ActionCodeSettings(
                url = continueUrl,
                canHandleCodeInApp = true,
                iOSBundleId = iosBundleId
            )
            firebaseAuth.sendSignInLinkToEmail(email, settings)
            AuthResult.MagicLinkSent
        } catch (e: FirebaseAuthException) {
            AuthResult.Failure(e.toAuthError())
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.Unknown(e.message ?: "Failed to send magic link", e))
        }
    }

    override suspend fun signInWithMagicLink(email: String, link: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailLink(email, link)
            result.user?.toUserSession()?.let { session ->
                AuthResult.Success(session)
            } ?: AuthResult.Failure(AuthError.Unknown("No user returned after magic link sign-in"))
        } catch (e: FirebaseAuthException) {
            AuthResult.Failure(e.toAuthError())
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.Unknown(e.message ?: "Magic link sign-in failed", e))
        }
    }

    private fun IdentityProvider.toCredential(tokenData: String): AuthCredential? {
        return when (this) {
            is IdentityProvider.Google -> {
                // Parse combined tokens: "idToken|||accessToken|||accessTokenValue"
                // or just idToken for Android (which doesn't need accessToken)
                val parts = tokenData.split("|||accessToken|||")
                val idToken = parts[0]
                val accessToken = parts.getOrNull(1)
                GoogleAuthProvider.credential(idToken, accessToken)
            }

            is IdentityProvider.Apple -> {
                // Token format: "idToken|||rawNonce|||rawNonceValue" or just "idToken"
                val parts = tokenData.split("|||rawNonce|||")
                val idToken = parts[0]
                val rawNonce = parts.getOrNull(1)
                OAuthProvider.credential(
                    providerId = "apple.com",
                    accessToken = null,
                    idToken = idToken,
                    rawNonce = rawNonce
                )
            }
            is IdentityProvider.GitHub -> GithubAuthProvider.credential(tokenData)
            is IdentityProvider.Microsoft -> OAuthProvider.credential(
                providerId = "microsoft.com",
                accessToken = null,
                idToken = tokenData,
                rawNonce = null
            )
            else -> null // Facebook, Phone, MagicLink have different flows
        }
    }

    companion object {
        const val PROVIDER_ID = "firebase"
    }
}
