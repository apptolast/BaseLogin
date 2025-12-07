package com.apptolast.customlogin.domain.provider

import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.SignUpData
import kotlinx.coroutines.flow.Flow

/**
 * Interface for authentication providers.
 * Implement this interface to add support for different auth backends
 * (Firebase, Supabase, custom backend, etc.)
 */
interface AuthProvider {
    /**
     * Unique identifier for this provider.
     */
    val id: String

    /**
     * Display name for UI purposes.
     */
    val displayName: String

    /**
     * Sign in with the given credentials.
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
     * Confirm password reset with code and new password.
     */
    suspend fun confirmPasswordReset(code: String, newPassword: String): AuthResult

    /**
     * Observe authentication state changes.
     */
    fun observeAuthState(): Flow<AuthState>

    /**
     * Refresh the current session.
     */
    suspend fun refreshSession(): AuthResult

    /**
     * Check if a user is currently signed in.
     */
    suspend fun isSignedIn(): Boolean

    /**
     * Get the current user's ID token (for backend verification).
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): String?

    /**
     * Delete the current user account.
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
     * Send email verification.
     */
    suspend fun sendEmailVerification(): Result<Unit>

    /**
     * Re-authenticate the user (required before sensitive operations).
     */
    suspend fun reauthenticate(credentials: Credentials): AuthResult
}
