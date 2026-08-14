package com.apptolast.customlogin.data

import com.apptolast.customlogin.SocialTokenResult
import com.apptolast.customlogin.data.firebase.FirebaseAuthCredential
import com.apptolast.customlogin.data.firebase.FirebaseAuthGateway
import com.apptolast.customlogin.data.firebase.FirebaseAuthUser
import com.apptolast.customlogin.data.firebase.PlatformSocialSignInStateCleaner
import com.apptolast.customlogin.data.firebase.PlatformSocialTokenProvider
import com.apptolast.customlogin.data.firebase.PlatformSocialTokenRevoker
import com.apptolast.customlogin.data.firebase.SocialSignInStateCleaner
import com.apptolast.customlogin.data.firebase.SocialTokenProvider
import com.apptolast.customlogin.data.firebase.SocialTokenRevoker
import com.apptolast.customlogin.di.DEFAULT_PHONE_AUTH_TIMEOUT_SECONDS
import com.apptolast.customlogin.domain.AuthProvider
import com.apptolast.customlogin.domain.PhoneAuthTimeoutProvider
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.domain.model.PhoneAuthResult
import com.apptolast.customlogin.domain.model.SignUpData
import com.apptolast.customlogin.domain.model.UserSession
import com.apptolast.customlogin.sendPhoneVerificationCode
import com.apptolast.customlogin.util.Logger
import com.apptolast.customlogin.verifyPhoneCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Firebase Authentication provider.
 *
 * Talks to Firebase through [FirebaseAuthGateway] rather than holding a `FirebaseAuth` directly:
 * that type is a platform `expect` class which cannot be faked from `commonTest`, so taking it as a
 * constructor argument made this class untestable by construction.
 *
 * The two social ports have production defaults, so consumers building this by hand only need to
 * supply the gateway.
 *
 * Phone authentication deliberately bypasses the gateway and calls the `expect` functions directly:
 * it needs an Activity on Android and APNS registration on iOS.
 */
class FirebaseAuthProvider(
    private val gateway: FirebaseAuthGateway,
    private val socialTokens: SocialTokenProvider = PlatformSocialTokenProvider(),
    private val socialStateCleaner: SocialSignInStateCleaner = PlatformSocialSignInStateCleaner(),
    private val socialRevoker: SocialTokenRevoker = PlatformSocialTokenRevoker(),
) : AuthProvider,
    PhoneAuthTimeoutProvider {

    override val id: String = PROVIDER_ID

    // ── Core Auth ─────────────────────────────────────────────────────────────

    override suspend fun signIn(credentials: Credentials): AuthResult = when (credentials) {
        is Credentials.EmailPassword -> runAuth {
            gateway.signIn(FirebaseAuthCredential.EmailPassword(credentials.email, credentials.password))
                .toSuccess()
        }
        is Credentials.OAuthToken -> signInWithOAuth(credentials.provider)
        is Credentials.RefreshToken -> refreshSession()
    }

    override suspend fun signUp(data: SignUpData): AuthResult = runAuth {
        val user = gateway.signUp(data.email, data.password)
        val displayName = data.displayName?.takeIf { it.isNotBlank() }
        if (displayName != null) {
            gateway.updateDisplayName(displayName)
        }
        (gateway.currentUser ?: user.copy(displayName = displayName ?: user.displayName)).toSuccess()
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        gateway.signOut()
        // Firebase alone is not enough: Credential Manager keeps the chosen Google account, and
        // without clearing it the picker never reappears and the user cannot switch accounts.
        socialStateCleaner.clear()
    }

    // ── Password Reset ────────────────────────────────────────────────────────

    override suspend fun sendPasswordResetEmail(email: String): AuthResult = runAuth {
        gateway.sendPasswordResetEmail(email)
        AuthResult.PasswordResetSent
    }

    override suspend fun confirmPasswordReset(code: String, newPassword: String): AuthResult = runAuth {
        gateway.confirmPasswordReset(code, newPassword)
        AuthResult.PasswordResetSuccess
    }

    // ── Session Management ────────────────────────────────────────────────────

    override fun observeAuthState(): Flow<AuthState> = gateway.authStateChanged
        .map { user -> user?.let { AuthState.Authenticated(it.toUserSession()) } ?: AuthState.Unauthenticated }
        .onStart { emit(AuthState.Loading) }
        .catch { e -> emit(AuthState.Error(e.toAuthError())) }

    /**
     * Reads the cached user **without requesting a token**.
     *
     * [AuthProvider.getCurrentSession] documents that this must not perform network I/O, and
     * `getIdToken` goes to the network whenever the cached token has expired. Callers that need a
     * token use [getIdToken] explicitly.
     */
    override suspend fun getCurrentSession(): UserSession? = gateway.currentUser?.toUserSession()

    override suspend fun refreshSession(): AuthResult = runAuth {
        val token = gateway.getIdToken(forceRefresh = true)
        val user = gateway.currentUser ?: return@runAuth AuthResult.Failure(AuthError.SessionExpired())
        AuthResult.Success(user.toUserSession(accessToken = token))
    }

    override suspend fun isSignedIn(): Boolean = gateway.currentUser != null

    override suspend fun getIdToken(forceRefresh: Boolean): String? = try {
        gateway.getIdToken(forceRefresh)
    } catch (e: Exception) {
        Logger.w("FirebaseAuthProvider", "getIdToken failed: ${e.message}")
        null
    }

    // ── Account Management ────────────────────────────────────────────────────

    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        revokeTokensOf(gateway.currentUser)
        gateway.deleteCurrentUser()
    }

    /**
     * Revokes what the user signed in with, before the account goes away.
     *
     * Apple asks for this in App Review guideline 5.1.1(v): deleting the Firebase user is not
     * enough, the app keeps appearing under *Settings → Apple Account → Sign in with Apple*.
     *
     * **Best effort, and deliberately so.** A revocation that fails — expired authorization code,
     * network down, the user dismissing the sheet — must not leave the account undeletable. The
     * opposite outcome (account alive, token revoked) is worse for the user than the reverse.
     *
     * Which providers to revoke comes from the user, never from the app's configuration: an
     * email-only user in an app that also offers Apple must not be shown an Apple sheet on the way
     * out.
     */
    private suspend fun revokeTokensOf(user: FirebaseAuthUser?) {
        user?.providerIds.orEmpty()
            .mapNotNull { IdentityProvider.fromId(it) }
            .forEach { provider ->
                try {
                    socialRevoker.revoke(provider)
                } catch (e: Exception) {
                    Logger.w("FirebaseAuthProvider", "Could not revoke the ${provider.id} token: ${e.message}")
                }
            }
    }

    override suspend fun updateDisplayName(displayName: String): Result<Unit> =
        runCatching { gateway.updateDisplayName(displayName) }

    override suspend fun updateEmail(newEmail: String): Result<Unit> = runCatching { gateway.updateEmail(newEmail) }

    override suspend fun updatePassword(newPassword: String): Result<Unit> =
        runCatching { gateway.updatePassword(newPassword) }

    override suspend fun sendEmailVerification(): Result<Unit> = runCatching { gateway.sendEmailVerification() }

    // ── Re-authentication ─────────────────────────────────────────────────────

    override suspend fun reauthenticate(credentials: Credentials): AuthResult = runAuth {
        val credential = when (credentials) {
            is Credentials.EmailPassword ->
                FirebaseAuthCredential.EmailPassword(credentials.email, credentials.password)
            is Credentials.OAuthToken -> when (val token = socialTokens.tokenFor(credentials.provider)) {
                null -> return@runAuth AuthResult.Failure(AuthError.OperationNotAllowed(SOCIAL_CANCELLED))
                is SocialTokenResult.PlatformHandled -> return@runAuth refreshSession()
                is SocialTokenResult.Token -> credentials.provider.toCredential(token.value)
                    ?: return@runAuth AuthResult.Failure(unsupported(credentials.provider))
            }
            is Credentials.RefreshToken -> return@runAuth AuthResult.Failure(
                AuthError.OperationNotAllowed("RefreshToken credentials cannot be used for reauthentication"),
            )
        }
        gateway.reauthenticate(credential)
        (gateway.currentUser ?: return@runAuth AuthResult.Failure(AuthError.UserNotFound())).toSuccess()
    }

    // ── Phone Auth (bypasses the gateway on purpose) ──────────────────────────

    override suspend fun sendPhoneOtp(phoneNumber: String): PhoneAuthResult =
        sendPhoneOtp(phoneNumber, DEFAULT_PHONE_AUTH_TIMEOUT_SECONDS)

    override suspend fun sendPhoneOtp(phoneNumber: String, timeoutSeconds: Long): PhoneAuthResult =
        sendPhoneVerificationCode(phoneNumber, timeoutSeconds)

    override suspend fun verifyPhoneOtp(verificationId: String, otpCode: String): AuthResult =
        verifyPhoneCode(verificationId, otpCode)

    // ── Magic Link ────────────────────────────────────────────────────────────

    override suspend fun sendMagicLink(email: String, continueUrl: String, iosBundleId: String?): AuthResult = runAuth {
        gateway.sendSignInLinkToEmail(email, continueUrl, iosBundleId)
        AuthResult.MagicLinkSent
    }

    override suspend fun signInWithMagicLink(email: String, link: String): AuthResult = runAuth {
        gateway.signInWithEmailLink(email, link).toSuccess()
    }

    // ── OAuth ─────────────────────────────────────────────────────────────────

    private suspend fun signInWithOAuth(provider: IdentityProvider): AuthResult = runAuth {
        when (val token = socialTokens.tokenFor(provider)) {
            // Cancelled or failed on the platform side: never reaches the SDK.
            null -> AuthResult.Failure(AuthError.OperationNotAllowed(SOCIAL_CANCELLED))

            // The platform already completed the Firebase sign-in (e.g. Android web OAuth).
            is SocialTokenResult.PlatformHandled -> refreshSession()

            is SocialTokenResult.Token -> {
                val credential = provider.toCredential(token.value)
                    ?: return@runAuth AuthResult.Failure(unsupported(provider))
                val user = gateway.signIn(credential)
                val session = applyPendingDisplayName(user, provider, token.value)
                AuthResult.Success(session)
            }
        }
    }

    /**
     * Persists the name Apple sends **only on the very first authorisation** of each user.
     *
     * If it is not written to the profile here it is lost for good: subsequent sign-ins never carry
     * it again. Never fails the sign-in — the user is already authenticated by this point.
     */
    private suspend fun applyPendingDisplayName(
        user: FirebaseAuthUser,
        provider: IdentityProvider,
        tokenData: String,
    ): UserSession {
        if (provider !is IdentityProvider.Apple) return user.toUserSession()
        val pending = tokenData.substringAfter(APPLE_DISPLAY_NAME_SEPARATOR, "").trim()
        if (pending.isBlank() || !user.displayName.isNullOrBlank()) return user.toUserSession()

        return try {
            gateway.updateDisplayName(pending)
            user.copy(displayName = pending).toUserSession()
        } catch (e: Exception) {
            Logger.w("FirebaseAuthProvider", "Could not persist the Apple display name: ${e.message}")
            user.toUserSession()
        }
    }

    /** Builds a credential from the packed string the platform layer produced. */
    private fun IdentityProvider.toCredential(tokenData: String): FirebaseAuthCredential? = when (this) {
        is IdentityProvider.Google -> FirebaseAuthCredential.Google(
            idToken = tokenData.substringBefore(GOOGLE_ACCESS_TOKEN_SEPARATOR),
            accessToken = tokenData.substringAfter(GOOGLE_ACCESS_TOKEN_SEPARATOR, "").ifBlank { null },
        )

        is IdentityProvider.Apple -> FirebaseAuthCredential.OAuth(
            providerId = id,
            idToken = tokenData.substringBefore(APPLE_NONCE_SEPARATOR),
            rawNonce = tokenData
                .substringAfter(APPLE_NONCE_SEPARATOR, "")
                .substringBefore(APPLE_DISPLAY_NAME_SEPARATOR)
                .ifBlank { null },
        )

        is IdentityProvider.GitHub -> FirebaseAuthCredential.GitHub(tokenData)

        is IdentityProvider.Microsoft, is IdentityProvider.Twitter ->
            FirebaseAuthCredential.OAuth(providerId = id, idToken = tokenData)

        is IdentityProvider.Facebook -> FirebaseAuthCredential.OAuth(providerId = id, accessToken = tokenData)

        // Phone, MagicLink and Custom follow different flows.
        else -> null
    }

    // ── Error handling ────────────────────────────────────────────────────────

    /**
     * Single funnel for every failure.
     *
     * Catching `FirebaseAuthException` alone is not enough: `FirebaseNetworkException` and
     * `FirebaseTooManyRequestsException` extend `FirebaseException` and are its *siblings*, so they
     * used to fall through to a generic catch and be reported as [AuthError.Unknown] — even though
     * [mapFirebaseErrorMessage] already knew how to classify their message.
     */
    private inline fun runAuth(block: () -> AuthResult): AuthResult = try {
        block()
    } catch (e: Exception) {
        AuthResult.Failure(e.toAuthError())
    }

    private fun Throwable.toAuthError(): AuthError =
        mapFirebaseErrorMessage(message?.trim()?.ifBlank { null } ?: cause?.message?.trim() ?: "Authentication error")

    private fun unsupported(provider: IdentityProvider): AuthError =
        AuthError.OperationNotAllowed("Provider not supported: ${provider.id}")

    private fun FirebaseAuthUser.toSuccess(): AuthResult.Success = AuthResult.Success(toUserSession())

    companion object {
        const val PROVIDER_ID = "firebase"

        private const val SOCIAL_CANCELLED = "Social sign-in cancelled or failed."

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
