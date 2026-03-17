package com.apptolast.customlogin.data

import com.apptolast.customlogin.di.LoginLibraryConfig
import com.apptolast.customlogin.domain.AuthProvider
import com.apptolast.customlogin.domain.AuthRepository
import com.apptolast.customlogin.domain.model.AuthError
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
 * Implementation of AuthRepository that delegates to an AuthProvider.
 * This allows swapping between Firebase, Supabase, or custom backends.
 */
class AuthRepositoryImpl(
    private val authProvider: AuthProvider,
    private val config: LoginLibraryConfig
) : AuthRepository {

    // ── Provider Identity ─────────────────────────────────────────────────────

    /** Identifies the backing [AuthProvider] (e.g. "firebase"). Useful for debugging. */
    override val currentProviderId: String
        get() = authProvider.id

    // ── Session Observation ───────────────────────────────────────────────────

    /** Continuous stream of auth state changes — used by the host app to react to sign-in/out. */
    override fun observeAuthState(): Flow<AuthState> {
        return authProvider.observeAuthState()
    }

    /** Returns the active [UserSession] by refreshing the token, or null if not signed in. */
    override suspend fun getCurrentSession(): UserSession? {
        return when (val result = authProvider.refreshSession()) {
            is AuthResult.Success -> result.session
            else -> null
        }
    }

    // ── Core Auth Flows ───────────────────────────────────────────────────────
    // Used by LoginViewModel, RegisterViewModel, ReauthViewModel, and the host app.

    override suspend fun signIn(credentials: Credentials): AuthResult {
        return authProvider.signIn(credentials)
    }

    override suspend fun signUp(data: SignUpData): AuthResult {
        return authProvider.signUp(data)
    }

    override suspend fun signOut(): Result<Unit> {
        return authProvider.signOut()
    }

    override suspend fun reauthenticate(credentials: Credentials): AuthResult {
        return authProvider.reauthenticate(credentials)
    }

    // ── Password Reset ────────────────────────────────────────────────────────
    // Used by ForgotPasswordViewModel and ResetPasswordViewModel.

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return authProvider.sendPasswordResetEmail(email)
    }

    override suspend fun confirmPasswordReset(data: PasswordResetData): AuthResult {
        return authProvider.confirmPasswordReset(data.code, data.newPassword)
    }

    // ── Session / Token Management ────────────────────────────────────────────
    // Primarily used by the host app for backend verification and session checks.

    override suspend fun refreshSession(): AuthResult {
        return authProvider.refreshSession()
    }

    override suspend fun isSignedIn(): Boolean {
        return authProvider.isSignedIn()
    }

    /** Returns a short-lived JWT for backend verification. Pass forceRefresh=true to bypass cache. */
    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        return authProvider.getIdToken(forceRefresh)
    }

    // ── Account Management ────────────────────────────────────────────────────
    // Consumer-facing API — not used internally by this library's own screens.

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

    // ── Provider Discovery ────────────────────────────────────────────────────

    /**
     * Returns the list of identity providers enabled in [LoginLibraryConfig].
     * Used by LoginScreen and RegisterScreen to render the social button section.
     */
    override fun getAvailableProviders(): List<IdentityProvider> {
        return buildList {
            if (config.googleSignInConfig != null) add(IdentityProvider.Google)
            if (config.appleSignInConfig != null) add(IdentityProvider.Apple)
            if (config.githubEnabled) add(IdentityProvider.GitHub)
            if (config.microsoftEnabled) add(IdentityProvider.Microsoft)
            if (config.magicLinkConfig != null) add(IdentityProvider.MagicLink)
            if (config.phoneEnabled) add(IdentityProvider.Phone)
            if (config.twitterEnabled) add(IdentityProvider.Twitter)
            if (config.facebookEnabled) add(IdentityProvider.Facebook)
        }
    }

    // ── Phone Auth ────────────────────────────────────────────────────────────
    // Used by PhoneAuthViewModel.

    override suspend fun sendPhoneOtp(phoneNumber: String): PhoneAuthResult {
        return authProvider.sendPhoneOtp(phoneNumber)
    }

    override suspend fun verifyPhoneOtp(verificationId: String, otpCode: String): AuthResult {
        return authProvider.verifyPhoneOtp(verificationId, otpCode)
    }

    // ── Magic Link ────────────────────────────────────────────────────────────
    // Used by MagicLinkViewModel. Requires MagicLinkConfig to be set in LoginLibraryConfig.

    override suspend fun sendMagicLink(email: String): AuthResult {
        val mlConfig = config.magicLinkConfig
            ?: return AuthResult.Failure(AuthError.OperationNotAllowed("Magic Link is not configured. Provide MagicLinkConfig in LoginLibraryConfig."))
        return authProvider.sendMagicLink(email, mlConfig.continueUrl, mlConfig.iosBundleId)
    }

    override suspend fun signInWithMagicLink(email: String, link: String): AuthResult {
        return authProvider.signInWithMagicLink(email, link)
    }
}