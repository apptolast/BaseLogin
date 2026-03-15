package com.apptolast.customlogin.domain

import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.domain.model.PasswordResetData
import com.apptolast.customlogin.domain.model.PhoneAuthResult
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
     * Get the current user session, if authenticated.
     */
    suspend fun getCurrentSession(): UserSession?

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

    /**
     * Re-authenticate the user (required before sensitive operations like deleteAccount or updateEmail).
     */
    suspend fun reauthenticate(credentials: Credentials): AuthResult

    /**
     * Returns the list of configured social identity providers.
     * Useful for conditionally showing/hiding social login buttons.
     */
    fun getAvailableProviders(): List<IdentityProvider>

    /**
     * Sends a phone verification OTP to the given phone number.
     * @param phoneNumber E.164 format (e.g. "+34612345678").
     */
    suspend fun sendPhoneOtp(phoneNumber: String): PhoneAuthResult

    /**
     * Verifies the OTP code received via SMS and signs the user in.
     * @param verificationId The ID returned from [sendPhoneOtp].
     * @param otpCode The 6-digit SMS code entered by the user.
     */
    suspend fun verifyPhoneOtp(verificationId: String, otpCode: String): AuthResult

    /**
     * Sends a passwordless sign-in link to [email].
     * Requires [MagicLinkConfig] to be set in [LoginLibraryConfig].
     * @return [AuthResult.MagicLinkSent] on success.
     */
    suspend fun sendMagicLink(email: String): AuthResult

    /**
     * Completes a magic link sign-in. Call this from the host app when it intercepts
     * the deep link URL that Firebase embeds in the magic link email.
     * @param email The same email address used in [sendMagicLink].
     * @param link The full URL received from the deep link intent/universal link.
     */
    suspend fun signInWithMagicLink(email: String, link: String): AuthResult
}