package com.apptolast.customlogin.domain

import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.PasswordResetData
import com.apptolast.customlogin.domain.model.SignUpData
import com.apptolast.customlogin.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 * This abstraction allows the library to work with any authentication provider.
 */
interface AuthRepository {
    /**
     * Current provider ID being used.
     */
    val currentProviderId: String

    /**
     * Observe authentication state changes.
     */
    fun observeAuthState(): Flow<AuthState>

    /**
     * Sign in with any supported credentials.
     */
    suspend fun signIn(credentials: Credentials): AuthResult

    /**
     * Create a new user account.
     */
    suspend fun signUp(data: SignUpData): AuthResult

    /**
     * Sign out the current user.
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Send password reset email.
     */
    suspend fun sendPasswordResetEmail(email: String): AuthResult

    /**
     * Confirm password reset with verification code.
     */
    suspend fun confirmPasswordReset(data: PasswordResetData): AuthResult

    /**
     * Refresh the current session.
     */
    suspend fun refreshSession(): AuthResult

    /**
     * Check if user is currently signed in.
     */
    suspend fun isSignedIn(): Boolean

    /**
     * Get the current user's ID token for backend verification.
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): String?

    /**
     * Delete the current user's account.
     */
    suspend fun deleteAccount(): Result<Unit>

    /**
     * Update the user's display name.
     */
    suspend fun updateDisplayName(displayName: String): Result<Unit>

    /**
     * Update the user's email.
     */
    suspend fun updateEmail(newEmail: String): Result<Unit>

    /**
     * Update the user's password.
     */
    suspend fun updatePassword(newPassword: String): Result<Unit>

    /**
     * Send email verification to the current user.
     */
    suspend fun sendEmailVerification(): Result<Unit>
}