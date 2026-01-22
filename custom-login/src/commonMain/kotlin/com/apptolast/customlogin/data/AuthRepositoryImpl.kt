package com.apptolast.customlogin.data

import com.apptolast.customlogin.domain.AuthProvider
import com.apptolast.customlogin.domain.AuthRepository
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.PasswordResetData
import com.apptolast.customlogin.domain.model.SignUpData
import com.apptolast.customlogin.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of AuthRepository that delegates to an AuthProvider.
 * This allows swapping between Firebase, Supabase, or custom backends.
 */
class AuthRepositoryImpl(
    private val authProvider: AuthProvider
) : AuthRepository {

    override val currentProviderId: String
        get() = authProvider.id

    override fun observeAuthState(): Flow<AuthState> {
        return authProvider.observeAuthState()
    }

    override suspend fun getCurrentSession(): UserSession? {
        return when (val result = authProvider.refreshSession()) {
            is AuthResult.Success -> result.session
            else -> null
        }
    }

    override suspend fun signIn(credentials: Credentials): AuthResult {
        return authProvider.signIn(credentials)
    }

    override suspend fun signUp(data: SignUpData): AuthResult {
        return authProvider.signUp(data)
    }

    override suspend fun signOut(): Result<Unit> {
        return authProvider.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return authProvider.sendPasswordResetEmail(email)
    }

    override suspend fun confirmPasswordReset(data: PasswordResetData): AuthResult {
        return authProvider.confirmPasswordReset(data.code, data.newPassword)
    }

    override suspend fun refreshSession(): AuthResult {
        return authProvider.refreshSession()
    }

    override suspend fun isSignedIn(): Boolean {
        return authProvider.isSignedIn()
    }

    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        return authProvider.getIdToken(forceRefresh)
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return authProvider.deleteAccount()
    }

    override suspend fun updateDisplayName(displayName: String): Result<Unit> {
        return authProvider.updateDisplayName(displayName)
    }

    override suspend fun updateEmail(newEmail: String): Result<Unit> {
        return authProvider.updateEmail(newEmail)
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        return authProvider.updatePassword(newPassword)
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return authProvider.sendEmailVerification()
    }
}