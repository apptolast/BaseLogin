package com.apptolast.customlogin.data

import com.apptolast.customlogin.data.firebase.FirebaseAuthGateway
import com.apptolast.customlogin.data.firebase.PlatformSocialSignInStateCleaner
import com.apptolast.customlogin.data.firebase.PlatformSocialTokenProvider
import com.apptolast.customlogin.data.firebase.SocialSignInStateCleaner
import com.apptolast.customlogin.data.firebase.SocialTokenProvider
import com.apptolast.customlogin.di.DEFAULT_PHONE_AUTH_TIMEOUT_SECONDS
import com.apptolast.customlogin.domain.AuthProvider
import com.apptolast.customlogin.domain.PhoneAuthTimeoutProvider
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.PhoneAuthResult
import com.apptolast.customlogin.domain.model.SignUpData
import com.apptolast.customlogin.domain.model.UserSession
import com.apptolast.customlogin.sendPhoneVerificationCode
import com.apptolast.customlogin.verifyPhoneCode
import kotlinx.coroutines.flow.Flow

/**
 * Firebase Authentication provider.
 *
 * Talks to Firebase through [FirebaseAuthGateway] rather than holding a `FirebaseAuth` directly:
 * that type is a platform `expect` class which cannot be faked from `commonTest`, so taking it as a
 * constructor argument made this class untestable by construction.
 *
 * Phone authentication deliberately bypasses the gateway and calls the `expect` functions directly:
 * it needs an Activity on Android and APNS registration on iOS.
 */
class FirebaseAuthProvider(
    private val gateway: FirebaseAuthGateway,
    private val socialTokens: SocialTokenProvider = PlatformSocialTokenProvider(),
    private val socialStateCleaner: SocialSignInStateCleaner = PlatformSocialSignInStateCleaner(),
) : AuthProvider,
    PhoneAuthTimeoutProvider {

    override val id: String = PROVIDER_ID

    // ── Core Auth ─────────────────────────────────────────────────────────────

    override suspend fun signIn(credentials: Credentials): AuthResult = TODO("FLE-90")

    override suspend fun signUp(data: SignUpData): AuthResult = TODO("FLE-90")

    override suspend fun signOut(): Result<Unit> = TODO("FLE-90")

    // ── Password Reset ────────────────────────────────────────────────────────

    override suspend fun sendPasswordResetEmail(email: String): AuthResult = TODO("FLE-90")

    override suspend fun confirmPasswordReset(code: String, newPassword: String): AuthResult = TODO("FLE-90")

    // ── Session Management ────────────────────────────────────────────────────

    override fun observeAuthState(): Flow<AuthState> = TODO("FLE-90")

    override suspend fun getCurrentSession(): UserSession? = TODO("FLE-90")

    override suspend fun refreshSession(): AuthResult = TODO("FLE-90")

    override suspend fun isSignedIn(): Boolean = TODO("FLE-90")

    override suspend fun getIdToken(forceRefresh: Boolean): String? = TODO("FLE-90")

    // ── Account Management ────────────────────────────────────────────────────

    override suspend fun deleteAccount(): Result<Unit> = TODO("FLE-90")

    override suspend fun updateDisplayName(displayName: String): Result<Unit> = TODO("FLE-90")

    override suspend fun updateEmail(newEmail: String): Result<Unit> = TODO("FLE-90")

    override suspend fun updatePassword(newPassword: String): Result<Unit> = TODO("FLE-90")

    override suspend fun sendEmailVerification(): Result<Unit> = TODO("FLE-90")

    // ── Re-authentication ─────────────────────────────────────────────────────

    override suspend fun reauthenticate(credentials: Credentials): AuthResult = TODO("FLE-90")

    // ── Phone Auth (bypasses the gateway on purpose) ──────────────────────────

    override suspend fun sendPhoneOtp(phoneNumber: String): PhoneAuthResult =
        sendPhoneOtp(phoneNumber, DEFAULT_PHONE_AUTH_TIMEOUT_SECONDS)

    override suspend fun sendPhoneOtp(phoneNumber: String, timeoutSeconds: Long): PhoneAuthResult =
        sendPhoneVerificationCode(phoneNumber, timeoutSeconds)

    override suspend fun verifyPhoneOtp(verificationId: String, otpCode: String): AuthResult =
        verifyPhoneCode(verificationId, otpCode)

    // ── Magic Link ────────────────────────────────────────────────────────────

    override suspend fun sendMagicLink(email: String, continueUrl: String, iosBundleId: String?): AuthResult =
        TODO("FLE-90")

    override suspend fun signInWithMagicLink(email: String, link: String): AuthResult = TODO("FLE-90")

    companion object {
        const val PROVIDER_ID = "firebase"

        /** Separator in the Google token string: `"idToken$GOOGLE_ACCESS_TOKEN_SEPARATOR$accessToken"`. */
        const val GOOGLE_ACCESS_TOKEN_SEPARATOR = "|||accessToken|||"

        /** Separator in the Apple token string: `"idToken$APPLE_NONCE_SEPARATOR$rawNonce"`. */
        const val APPLE_NONCE_SEPARATOR = "|||rawNonce|||"

        /**
         * Optional trailing segment carrying the name Apple returns on the first authorisation only:
         * `"idToken$APPLE_NONCE_SEPARATOR$rawNonce$APPLE_DISPLAY_NAME_SEPARATOR$displayName"`.
         *
         * Appended rather than replacing the format, so Swift hosts already wired against the
         * two-segment string keep working untouched.
         */
        const val APPLE_DISPLAY_NAME_SEPARATOR = "|||displayName|||"
    }
}
