package com.apptolast.customlogin.fakes

import com.apptolast.customlogin.data.AuthResultWrapper
import com.apptolast.customlogin.data.FirebaseAuthService
import com.apptolast.customlogin.data.UserWrapper
import dev.gitlive.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A fake implementation of [FirebaseAuthService] for testing purposes.
 * Provides configurable return values, exception throwing, and call tracking.
 */
class FakeFirebaseAuthService : FirebaseAuthService {

    // Configurable auth results
    var signInWithEmailResult: AuthResultWrapper = FakeAuthResultWrapper(FakeUserWrapper())
    var signInWithCredentialResult: AuthResultWrapper = FakeAuthResultWrapper(FakeUserWrapper())
    var createUserResult: AuthResultWrapper = FakeAuthResultWrapper(FakeUserWrapper())

    // Current user configuration
    private var _currentUser: UserWrapper? = null
    override val currentUser: UserWrapper?
        get() = _currentUser

    // Auth state flow
    private val _authStateFlow = MutableStateFlow<UserWrapper?>(null)
    override val authStateChanged: Flow<UserWrapper?> = _authStateFlow.asStateFlow()

    // Exception configuration for testing error scenarios
    var signInWithEmailException: Exception? = null
    var signInWithCredentialException: Exception? = null
    var createUserException: Exception? = null
    var signOutException: Exception? = null
    var sendPasswordResetEmailException: Exception? = null
    var confirmPasswordResetException: Exception? = null

    // Call tracking
    var signInWithEmailCallCount = 0
        private set
    var signInWithEmailCalledWith: Pair<String, String>? = null
        private set

    var signInWithCredentialCallCount = 0
        private set
    var signInWithCredentialCalledWith: AuthCredential? = null
        private set

    var createUserCallCount = 0
        private set
    var createUserCalledWith: Pair<String, String>? = null
        private set

    var signOutCallCount = 0
        private set

    var sendPasswordResetEmailCallCount = 0
        private set
    var sendPasswordResetEmailCalledWith: String? = null
        private set

    var confirmPasswordResetCallCount = 0
        private set
    var confirmPasswordResetCalledWith: Pair<String, String>? = null
        private set

    override suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResultWrapper {
        signInWithEmailCallCount++
        signInWithEmailCalledWith = email to password
        signInWithEmailException?.let { throw it }
        return signInWithEmailResult
    }

    override suspend fun signInWithCredential(credential: AuthCredential): AuthResultWrapper {
        signInWithCredentialCallCount++
        signInWithCredentialCalledWith = credential
        signInWithCredentialException?.let { throw it }
        return signInWithCredentialResult
    }

    override suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResultWrapper {
        createUserCallCount++
        createUserCalledWith = email to password
        createUserException?.let { throw it }
        return createUserResult
    }

    override suspend fun signOut() {
        signOutCallCount++
        signOutException?.let { throw it }
        _currentUser = null
        _authStateFlow.value = null
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        sendPasswordResetEmailCallCount++
        sendPasswordResetEmailCalledWith = email
        sendPasswordResetEmailException?.let { throw it }
    }

    override suspend fun confirmPasswordReset(code: String, newPassword: String) {
        confirmPasswordResetCallCount++
        confirmPasswordResetCalledWith = code to newPassword
        confirmPasswordResetException?.let { throw it }
    }

    // Helper methods for test setup

    /**
     * Sets the current user for testing.
     */
    fun setCurrentUser(user: UserWrapper?) {
        _currentUser = user
        _authStateFlow.value = user
    }

    /**
     * Emits a new auth state for testing observers.
     */
    fun emitAuthState(user: UserWrapper?) {
        _authStateFlow.value = user
    }

    /**
     * Resets all call counts, recorded arguments, and configurable values for test isolation.
     */
    fun reset() {
        // Reset call counts
        signInWithEmailCallCount = 0
        signInWithEmailCalledWith = null
        signInWithCredentialCallCount = 0
        signInWithCredentialCalledWith = null
        createUserCallCount = 0
        createUserCalledWith = null
        signOutCallCount = 0
        sendPasswordResetEmailCallCount = 0
        sendPasswordResetEmailCalledWith = null
        confirmPasswordResetCallCount = 0
        confirmPasswordResetCalledWith = null

        // Reset exceptions
        signInWithEmailException = null
        signInWithCredentialException = null
        createUserException = null
        signOutException = null
        sendPasswordResetEmailException = null
        confirmPasswordResetException = null

        // Reset configurable results
        val defaultUser = FakeUserWrapper()
        signInWithEmailResult = FakeAuthResultWrapper(defaultUser)
        signInWithCredentialResult = FakeAuthResultWrapper(defaultUser)
        createUserResult = FakeAuthResultWrapper(defaultUser)

        // Reset state
        _currentUser = null
        _authStateFlow.value = null
    }
}
