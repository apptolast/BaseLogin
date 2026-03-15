package com.apptolast.customlogin.test

import com.apptolast.customlogin.domain.AuthRepository
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.domain.model.PasswordResetData
import com.apptolast.customlogin.domain.model.PhoneAuthResult
import com.apptolast.customlogin.domain.model.SignUpData
import com.apptolast.customlogin.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Test double for [AuthRepository]. All return values are configurable before each test.
 *
 * Usage:
 * ```kotlin
 * val repo = FakeAuthRepository()
 * repo.signInResult = AuthResult.Failure(AuthError.InvalidCredentials())
 * val viewModel = LoginViewModel(repo)
 * viewModel.onAction(LoginAction.SignInClicked)
 * ```
 */
class FakeAuthRepository : AuthRepository {

    // ── Configurable results ───────────────────────────────────────────────

    var signInResult: AuthResult = AuthResult.Success(fakeSession())
    var signUpResult: AuthResult = AuthResult.Success(fakeSession())
    var signOutResult: Result<Unit> = Result.success(Unit)
    var sendPasswordResetEmailResult: AuthResult = AuthResult.PasswordResetSent
    var confirmPasswordResetResult: AuthResult = AuthResult.PasswordResetSuccess
    var refreshSessionResult: AuthResult = AuthResult.Success(fakeSession())
    var signedIn: Boolean = false
    var idToken: String? = "fake-id-token"
    var deleteAccountResult: Result<Unit> = Result.success(Unit)
    var updateDisplayNameResult: Result<Unit> = Result.success(Unit)
    var updateEmailResult: Result<Unit> = Result.success(Unit)
    var updatePasswordResult: Result<Unit> = Result.success(Unit)
    var sendEmailVerificationResult: Result<Unit> = Result.success(Unit)
    var reauthenticateResult: AuthResult = AuthResult.Success(fakeSession())
    var availableProviders: List<IdentityProvider> = emptyList()
    var sendPhoneOtpResult: PhoneAuthResult = PhoneAuthResult.CodeSent("fake-verification-id")
    var verifyPhoneOtpResult: AuthResult = AuthResult.Success(fakeSession())
    var sendMagicLinkResult: AuthResult = AuthResult.MagicLinkSent
    var signInWithMagicLinkResult: AuthResult = AuthResult.Success(fakeSession())

    // ── Auth state ─────────────────────────────────────────────────────────

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)

    /** Push a new [AuthState] to observers in tests. */
    fun emitAuthState(state: AuthState) {
        _authState.value = state
    }

    // ── AuthRepository ─────────────────────────────────────────────────────

    override val currentProviderId: String = "fake"

    override fun observeAuthState(): Flow<AuthState> = _authState

    override suspend fun getCurrentSession(): UserSession? =
        (_authState.value as? AuthState.Authenticated)?.session

    override suspend fun signIn(credentials: Credentials): AuthResult = signInResult
    override suspend fun signUp(data: SignUpData): AuthResult = signUpResult
    override suspend fun signOut(): Result<Unit> = signOutResult
    override suspend fun sendPasswordResetEmail(email: String): AuthResult = sendPasswordResetEmailResult
    override suspend fun confirmPasswordReset(data: PasswordResetData): AuthResult = confirmPasswordResetResult
    override suspend fun refreshSession(): AuthResult = refreshSessionResult
    override suspend fun isSignedIn(): Boolean = signedIn
    override suspend fun getIdToken(forceRefresh: Boolean): String? = idToken
    override suspend fun deleteAccount(): Result<Unit> = deleteAccountResult
    override suspend fun updateDisplayName(displayName: String): Result<Unit> = updateDisplayNameResult
    override suspend fun updateEmail(newEmail: String): Result<Unit> = updateEmailResult
    override suspend fun updatePassword(newPassword: String): Result<Unit> = updatePasswordResult
    override suspend fun sendEmailVerification(): Result<Unit> = sendEmailVerificationResult
    override suspend fun reauthenticate(credentials: Credentials): AuthResult = reauthenticateResult
    override fun getAvailableProviders(): List<IdentityProvider> = availableProviders
    override suspend fun sendPhoneOtp(phoneNumber: String): PhoneAuthResult = sendPhoneOtpResult
    override suspend fun verifyPhoneOtp(verificationId: String, otpCode: String): AuthResult = verifyPhoneOtpResult
    override suspend fun sendMagicLink(email: String): AuthResult = sendMagicLinkResult
    override suspend fun signInWithMagicLink(email: String, link: String): AuthResult = signInWithMagicLinkResult

    companion object {
        fun fakeSession(
            userId: String = "fake-user-id",
            email: String = "test@example.com",
        ) = UserSession(
            userId = userId,
            email = email,
            displayName = "Test User",
            isEmailVerified = true,
            providerId = "fake",
            accessToken = "fake-access-token",
        )
    }
}
