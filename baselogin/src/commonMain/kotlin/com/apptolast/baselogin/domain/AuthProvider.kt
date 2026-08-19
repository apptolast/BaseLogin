package com.apptolast.baselogin.domain

import com.apptolast.baselogin.di.DEFAULT_PHONE_AUTH_TIMEOUT_SECONDS
import com.apptolast.baselogin.domain.model.AuthResult
import com.apptolast.baselogin.domain.model.AuthState
import com.apptolast.baselogin.domain.model.Credentials
import com.apptolast.baselogin.domain.model.PhoneAuthResult
import com.apptolast.baselogin.domain.model.SignUpData
import com.apptolast.baselogin.domain.model.UserSession
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
     * Returns the current authenticated session from local cache, or null if not signed in.
     *
     * Unlike [refreshSession], this MUST NOT perform network I/O — it reads only from the
     * provider's local persistent state. This makes it safe to call offline and from
     * latency-sensitive contexts (e.g. background detection pipelines that need a userId
     * without blocking on a token refresh).
     */
    suspend fun getCurrentSession(): UserSession?

    /**
     * Refresh the current session, forcing a token refresh against the backend.
     * Requires network connectivity. Use [getCurrentSession] when you only need the
     * cached session and can tolerate a stale token.
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
     * Sends a magic link (passwordless email sign-in link) to [email].
     * @param email The user's email address.
     * @param continueUrl The deep link URL configured in [MagicLinkConfig].
     * @param iosBundleId Optional iOS bundle ID for deep link handling.
     * @return [AuthResult.MagicLinkSent] on success, [AuthResult.Failure] on error.
     */
    suspend fun sendMagicLink(email: String, continueUrl: String, iosBundleId: String?): AuthResult

    /**
     * Completes a magic link sign-in by verifying [email] and [link].
     * Call this from the host app when it receives the deep link URL.
     * @return [AuthResult.Success] on sign-in, [AuthResult.Failure] on error.
     */
    suspend fun signInWithMagicLink(email: String, link: String): AuthResult
}

/**
 * Optional capability for providers that support an SMS auto-retrieval timeout.
 *
 * Custom [AuthProvider] implementations do not need to implement this interface unless they want
 * [com.apptolast.baselogin.di.PhoneAuthConfig.timeoutSeconds] to be forwarded to the provider.
 */
interface PhoneAuthTimeoutProvider {
    suspend fun sendPhoneOtp(
        phoneNumber: String,
        timeoutSeconds: Long = DEFAULT_PHONE_AUTH_TIMEOUT_SECONDS,
    ): PhoneAuthResult
}
