package com.apptolast.customlogin.fakes

import com.apptolast.customlogin.domain.AuthRepository
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.PasswordResetData
import com.apptolast.customlogin.domain.model.SignUpData
import com.apptolast.customlogin.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A fake implementation of AuthRepository for testing.
 * Uses configurable responses and call tracking.
 */
class FakeAuthRepository : AuthRepository {

    // Configurable responses
    var signInResult: AuthResult = AuthResult.Success(createFakeUserSession())
    var signUpResult: AuthResult = AuthResult.Success(createFakeUserSession())
    var signOutResult: Result<Unit> = Result.success(Unit)
    var sendPasswordResetEmailResult: AuthResult = AuthResult.PasswordResetSent
    var confirmPasswordResetResult: AuthResult = AuthResult.PasswordResetSuccess
    var refreshSessionResult: AuthResult = AuthResult.Success(createFakeUserSession())
    var isSignedInResult: Boolean = false
    var idTokenResult: String? = "fake-id-token"
    var deleteAccountResult: Result<Unit> = Result.success(Unit)
    var updateDisplayNameResult: Result<Unit> = Result.success(Unit)
    var updateEmailResult: Result<Unit> = Result.success(Unit)
    var updatePasswordResult: Result<Unit> = Result.success(Unit)
    var sendEmailVerificationResult: Result<Unit> = Result.success(Unit)

    // Call tracking
    var signInCallCount = 0
        private set
    var signInCalledWith: Credentials? = null
        private set

    var signUpCallCount = 0
        private set
    var signUpCalledWith: SignUpData? = null
        private set

    var signOutCallCount = 0
        private set

    var sendPasswordResetEmailCallCount = 0
        private set
    var sendPasswordResetEmailCalledWith: String? = null
        private set

    var confirmPasswordResetCallCount = 0
        private set
    var confirmPasswordResetCalledWith: PasswordResetData? = null
        private set

    var refreshSessionCallCount = 0
        private set

    var isSignedInCallCount = 0
        private set

    var getIdTokenCallCount = 0
        private set
    var getIdTokenForceRefresh: Boolean? = null
        private set

    var deleteAccountCallCount = 0
        private set

    var updateDisplayNameCallCount = 0
        private set
    var updateDisplayNameCalledWith: String? = null
        private set

    var updateEmailCallCount = 0
        private set
    var updateEmailCalledWith: String? = null
        private set

    var updatePasswordCallCount = 0
        private set
    var updatePasswordCalledWith: String? = null
        private set

    var sendEmailVerificationCallCount = 0
        private set

    private val _authStateFlow = MutableStateFlow<AuthState>(AuthState.Unauthenticated)

    override val currentProviderId: String = "fake-provider"

    override fun observeAuthState(): Flow<AuthState> = _authStateFlow

    override suspend fun signIn(credentials: Credentials): AuthResult {
        signInCallCount++
        signInCalledWith = credentials
        return signInResult
    }

    override suspend fun signUp(data: SignUpData): AuthResult {
        signUpCallCount++
        signUpCalledWith = data
        return signUpResult
    }

    override suspend fun signOut(): Result<Unit> {
        signOutCallCount++
        return signOutResult
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        sendPasswordResetEmailCallCount++
        sendPasswordResetEmailCalledWith = email
        return sendPasswordResetEmailResult
    }

    override suspend fun confirmPasswordReset(data: PasswordResetData): AuthResult {
        confirmPasswordResetCallCount++
        confirmPasswordResetCalledWith = data
        return confirmPasswordResetResult
    }

    override suspend fun refreshSession(): AuthResult {
        refreshSessionCallCount++
        return refreshSessionResult
    }

    override suspend fun isSignedIn(): Boolean {
        isSignedInCallCount++
        return isSignedInResult
    }

    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        getIdTokenCallCount++
        getIdTokenForceRefresh = forceRefresh
        return idTokenResult
    }

    override suspend fun deleteAccount(): Result<Unit> {
        deleteAccountCallCount++
        return deleteAccountResult
    }

    override suspend fun updateDisplayName(displayName: String): Result<Unit> {
        updateDisplayNameCallCount++
        updateDisplayNameCalledWith = displayName
        return updateDisplayNameResult
    }

    override suspend fun updateEmail(newEmail: String): Result<Unit> {
        updateEmailCallCount++
        updateEmailCalledWith = newEmail
        return updateEmailResult
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        updatePasswordCallCount++
        updatePasswordCalledWith = newPassword
        return updatePasswordResult
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        sendEmailVerificationCallCount++
        return sendEmailVerificationResult
    }

    /**
     * Emits a new auth state for testing observers.
     */
    fun emitAuthState(state: AuthState) {
        _authStateFlow.value = state
    }

    /**
     * Resets all call counts and recorded arguments for test isolation.
     */
    fun reset() {
        signInCallCount = 0
        signInCalledWith = null
        signUpCallCount = 0
        signUpCalledWith = null
        signOutCallCount = 0
        sendPasswordResetEmailCallCount = 0
        sendPasswordResetEmailCalledWith = null
        confirmPasswordResetCallCount = 0
        confirmPasswordResetCalledWith = null
        refreshSessionCallCount = 0
        isSignedInCallCount = 0
        getIdTokenCallCount = 0
        getIdTokenForceRefresh = null
        deleteAccountCallCount = 0
        updateDisplayNameCallCount = 0
        updateDisplayNameCalledWith = null
        updateEmailCallCount = 0
        updateEmailCalledWith = null
        updatePasswordCallCount = 0
        updatePasswordCalledWith = null
        sendEmailVerificationCallCount = 0

        // Reset results to defaults
        signInResult = AuthResult.Success(createFakeUserSession())
        signUpResult = AuthResult.Success(createFakeUserSession())
        signOutResult = Result.success(Unit)
        sendPasswordResetEmailResult = AuthResult.PasswordResetSent
        confirmPasswordResetResult = AuthResult.PasswordResetSuccess
        refreshSessionResult = AuthResult.Success(createFakeUserSession())
        isSignedInResult = false
        idTokenResult = "fake-id-token"
        deleteAccountResult = Result.success(Unit)
        updateDisplayNameResult = Result.success(Unit)
        updateEmailResult = Result.success(Unit)
        updatePasswordResult = Result.success(Unit)
        sendEmailVerificationResult = Result.success(Unit)

        _authStateFlow.value = AuthState.Unauthenticated
    }

    companion object {
        /**
         * Creates a fake UserSession for testing.
         */
        fun createFakeUserSession(
            userId: String = "fake-user-id",
            email: String = "test@example.com",
            displayName: String? = "Test User",
            photoUrl: String? = null,
            isEmailVerified: Boolean = true,
            providerId: String = "firebase"
        ): UserSession = UserSession(
            userId = userId,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl,
            isEmailVerified = isEmailVerified,
            providerId = providerId
        )
    }
}
