package com.apptolast.customlogin.test

import com.apptolast.customlogin.domain.AuthProvider
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.PhoneAuthResult
import com.apptolast.customlogin.domain.model.SignUpData
import com.apptolast.customlogin.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Minimal [AuthProvider] test double used in [AuthRepositoryImpl] unit tests.
 * Configure individual results before each test as needed.
 */
class FakeAuthProvider : AuthProvider {

    override val id: String = "fake"
    override val displayName: String = "Fake"

    var signInResult: AuthResult = AuthResult.Success(fakeSession())
    var signUpResult: AuthResult = AuthResult.Success(fakeSession())
    var signOutResult: Result<Unit> = Result.success(Unit)
    var sendPasswordResetEmailResult: AuthResult = AuthResult.PasswordResetSent
    var confirmPasswordResetResult: AuthResult = AuthResult.PasswordResetSuccess
    var refreshSessionResult: AuthResult = AuthResult.Success(fakeSession())
    var isSignedInResult: Boolean = false
    var idTokenResult: String? = "fake-token"
    var deleteAccountResult: Result<Unit> = Result.success(Unit)
    var updateDisplayNameResult: Result<Unit> = Result.success(Unit)
    var updateEmailResult: Result<Unit> = Result.success(Unit)
    var updatePasswordResult: Result<Unit> = Result.success(Unit)
    var sendEmailVerificationResult: Result<Unit> = Result.success(Unit)
    var reauthenticateResult: AuthResult = AuthResult.Success(fakeSession())
    var sendPhoneOtpResult: PhoneAuthResult = PhoneAuthResult.CodeSent("fake-verification-id")
    var verifyPhoneOtpResult: AuthResult = AuthResult.Success(fakeSession())
    var sendMagicLinkResult: AuthResult = AuthResult.MagicLinkSent
    var signInWithMagicLinkResult: AuthResult = AuthResult.Success(fakeSession())

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)

    override fun observeAuthState(): Flow<AuthState> = _authState

    override suspend fun signIn(credentials: Credentials): AuthResult = signInResult
    override suspend fun signUp(data: SignUpData): AuthResult = signUpResult
    override suspend fun signOut(): Result<Unit> = signOutResult
    override suspend fun sendPasswordResetEmail(email: String): AuthResult = sendPasswordResetEmailResult
    override suspend fun confirmPasswordReset(code: String, newPassword: String): AuthResult = confirmPasswordResetResult
    override suspend fun refreshSession(): AuthResult = refreshSessionResult
    override suspend fun isSignedIn(): Boolean = isSignedInResult
    override suspend fun getIdToken(forceRefresh: Boolean): String? = idTokenResult
    override suspend fun deleteAccount(): Result<Unit> = deleteAccountResult
    override suspend fun updateDisplayName(displayName: String): Result<Unit> = updateDisplayNameResult
    override suspend fun updateEmail(newEmail: String): Result<Unit> = updateEmailResult
    override suspend fun updatePassword(newPassword: String): Result<Unit> = updatePasswordResult
    override suspend fun sendEmailVerification(): Result<Unit> = sendEmailVerificationResult
    override suspend fun reauthenticate(credentials: Credentials): AuthResult = reauthenticateResult
    override suspend fun sendPhoneOtp(phoneNumber: String): PhoneAuthResult = sendPhoneOtpResult
    override suspend fun verifyPhoneOtp(verificationId: String, otpCode: String): AuthResult = verifyPhoneOtpResult
    override suspend fun sendMagicLink(email: String, continueUrl: String, iosBundleId: String?): AuthResult = sendMagicLinkResult
    override suspend fun signInWithMagicLink(email: String, link: String): AuthResult = signInWithMagicLinkResult

    companion object {
        fun fakeSession(userId: String = "fake-user-id", email: String = "test@example.com") =
            UserSession(
                userId = userId,
                email = email,
                displayName = "Test User",
                isEmailVerified = true,
                providerId = "fake",
                accessToken = "fake-token",
            )
    }
}
