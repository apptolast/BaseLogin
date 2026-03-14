package com.apptolast.customlogin.data

import com.apptolast.customlogin.domain.AuthRepository
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthRequest
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AuthRepositoryImpl(
    private val firebaseProvider: FirebaseAuthProvider,
    private val googleProvider: GoogleAuthProvider,
    private val appleProvider: AppleAuthProvider,
    private val phoneProvider: PhoneAuthProvider,
) : AuthRepository {

    // Holds the last successful provider for session management
    private var activeProvider: com.apptolast.customlogin.domain.AuthProvider? = null

    override suspend fun signIn(request: AuthRequest): AuthResult {
        val provider = providerFor(request.provider)
            ?: return AuthResult.Failure(AuthError.OperationNotAllowed("Provider not supported: ${request.provider}"))

        return provider.signIn(request).also { result ->
            if (result is AuthResult.Success) {
                activeProvider = provider
            }
        }
    }

    override suspend fun signUp(request: AuthRequest): AuthResult {
        // Sign up is currently only supported via Email/Password
        return firebaseProvider.signUp(request)
    }

    override suspend fun signOut(): AuthResult {
        return activeProvider?.signOut() ?: AuthResult.SignOutSuccess
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return firebaseProvider.sendPasswordResetEmail(email)
    }

    override suspend fun confirmPasswordReset(code: String, newPassword: String): AuthResult {
        return firebaseProvider.confirmPasswordReset(code, newPassword)
    }

    override fun getCurrentSession(): UserSession? {
        // This is a simplification. A real implementation might need to check
        // the session status from the active provider.
        return null
    }

    override fun observeAuthState(): Flow<AuthState> {
        // A real implementation would need to merge flows from all providers.
        // For now, we only observe Firebase auth state.
        return flowOf(AuthState.Unauthenticated)
    }

    private fun providerFor(identityProvider: IdentityProvider): com.apptolast.customlogin.domain.AuthProvider? {
        return when (identityProvider) {
            is IdentityProvider.Email -> firebaseProvider
            is IdentityProvider.Google -> googleProvider
            is IdentityProvider.Apple -> appleProvider
            is IdentityProvider.Phone -> phoneProvider
            else -> null
        }
    }
}
