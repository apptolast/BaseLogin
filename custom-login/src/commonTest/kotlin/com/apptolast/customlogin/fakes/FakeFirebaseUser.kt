package com.apptolast.customlogin.fakes

import com.apptolast.customlogin.data.UserWrapper

/**
 * A fake implementation of [UserWrapper] for testing purposes.
 * Provides configurable return values and call tracking.
 */
class FakeUserWrapper(
    override val uid: String = "fake-uid",
    override val email: String? = "test@example.com",
    override val displayName: String? = "Test User",
    override val photoURL: String? = null,
    override val isEmailVerified: Boolean = true
) : UserWrapper {

    // Configurable return values
    var idTokenResult: String? = "fake-id-token"
    var shouldThrowOnGetIdToken: Exception? = null
    var shouldThrowOnDelete: Exception? = null
    var shouldThrowOnUpdateProfile: Exception? = null
    var shouldThrowOnUpdatePassword: Exception? = null
    var shouldThrowOnUpdateEmail: Exception? = null
    var shouldThrowOnSendEmailVerification: Exception? = null

    // Call tracking
    var getIdTokenCallCount = 0
        private set
    var getIdTokenForceRefresh: Boolean? = null
        private set
    var updateProfileCallCount = 0
        private set
    var updateProfileCalledWithDisplayName: String? = null
        private set
    var updateProfileCalledWithPhotoUrl: String? = null
        private set
    var deleteCallCount = 0
        private set
    var updatePasswordCallCount = 0
        private set
    var updatePasswordCalledWith: String? = null
        private set
    var verifyBeforeUpdateEmailCallCount = 0
        private set
    var verifyBeforeUpdateEmailCalledWith: String? = null
        private set
    var sendEmailVerificationCallCount = 0
        private set

    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        getIdTokenCallCount++
        getIdTokenForceRefresh = forceRefresh
        shouldThrowOnGetIdToken?.let { throw it }
        return idTokenResult
    }

    override suspend fun updateProfile(displayName: String?, photoUrl: String?) {
        updateProfileCallCount++
        updateProfileCalledWithDisplayName = displayName
        updateProfileCalledWithPhotoUrl = photoUrl
        shouldThrowOnUpdateProfile?.let { throw it }
    }

    override suspend fun delete() {
        deleteCallCount++
        shouldThrowOnDelete?.let { throw it }
    }

    override suspend fun verifyBeforeUpdateEmail(newEmail: String) {
        verifyBeforeUpdateEmailCallCount++
        verifyBeforeUpdateEmailCalledWith = newEmail
        shouldThrowOnUpdateEmail?.let { throw it }
    }

    override suspend fun updatePassword(newPassword: String) {
        updatePasswordCallCount++
        updatePasswordCalledWith = newPassword
        shouldThrowOnUpdatePassword?.let { throw it }
    }

    override suspend fun sendEmailVerification() {
        sendEmailVerificationCallCount++
        shouldThrowOnSendEmailVerification?.let { throw it }
    }

    /**
     * Resets all call tracking state for test isolation.
     */
    fun reset() {
        getIdTokenCallCount = 0
        getIdTokenForceRefresh = null
        updateProfileCallCount = 0
        updateProfileCalledWithDisplayName = null
        updateProfileCalledWithPhotoUrl = null
        deleteCallCount = 0
        updatePasswordCallCount = 0
        updatePasswordCalledWith = null
        verifyBeforeUpdateEmailCallCount = 0
        verifyBeforeUpdateEmailCalledWith = null
        sendEmailVerificationCallCount = 0

        // Reset exception configs
        shouldThrowOnGetIdToken = null
        shouldThrowOnDelete = null
        shouldThrowOnUpdateProfile = null
        shouldThrowOnUpdatePassword = null
        shouldThrowOnUpdateEmail = null
        shouldThrowOnSendEmailVerification = null
    }
}
