package com.apptolast.customlogin.data.provider

import app.cash.turbine.test
import com.apptolast.customlogin.data.FirebaseAuthProvider
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.SignUpData
import com.apptolast.customlogin.fakes.FakeAuthResultWrapper
import com.apptolast.customlogin.fakes.FakeFirebaseAuthService
import com.apptolast.customlogin.fakes.FakeUserWrapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [FirebaseAuthProvider].
 * Uses fake implementations of Firebase SDK interfaces.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAuthProviderTest {

    private lateinit var fakeAuthService: FakeFirebaseAuthService
    private lateinit var authProvider: FirebaseAuthProvider

    @BeforeTest
    fun setUp() {
        fakeAuthService = FakeFirebaseAuthService()
        authProvider = FirebaseAuthProvider(fakeAuthService)
    }

    @AfterTest
    fun tearDown() {
        fakeAuthService.reset()
    }

    // ========== Provider Identity Tests ==========

    @Test
    fun `provider id returns firebase`() {
        assertEquals("firebase", authProvider.id)
    }

    @Test
    fun `provider display name returns Firebase`() {
        assertEquals("Firebase", authProvider.displayName)
    }

    // ========== Sign In Tests ==========

    @Test
    fun `signIn with valid email credentials returns Success`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper(uid = "user-123", email = "test@example.com")
        fakeAuthService.signInWithEmailResult = FakeAuthResultWrapper(fakeUser)
        val credentials = Credentials.EmailPassword("test@example.com", "password123")

        // Act
        val result = authProvider.signIn(credentials)

        // Assert
        assertIs<AuthResult.Success>(result)
        assertEquals("user-123", result.session.userId)
        assertEquals("test@example.com", result.session.email)
        assertEquals(1, fakeAuthService.signInWithEmailCallCount)
        assertEquals("test@example.com" to "password123", fakeAuthService.signInWithEmailCalledWith)
    }

    @Test
    fun `signIn with invalid credentials returns Failure with InvalidCredentials error`() = runTest {
        // Arrange
        fakeAuthService.signInWithEmailException = RuntimeException("INVALID_LOGIN_CREDENTIALS")
        val credentials = Credentials.EmailPassword("test@example.com", "wrongpassword")

        // Act
        val result = authProvider.signIn(credentials)

        // Assert
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.InvalidCredentials>(result.error)
    }

    @Test
    fun `signIn with non-existent user returns Failure with UserNotFound error`() = runTest {
        // Arrange
        fakeAuthService.signInWithEmailException = RuntimeException("USER_NOT_FOUND")
        val credentials = Credentials.EmailPassword("nonexistent@example.com", "password")

        // Act
        val result = authProvider.signIn(credentials)

        // Assert
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.UserNotFound>(result.error)
    }

    @Test
    fun `signIn with network error returns Failure with NetworkError`() = runTest {
        // Arrange
        fakeAuthService.signInWithEmailException = RuntimeException("NETWORK error occurred")
        val credentials = Credentials.EmailPassword("test@example.com", "password")

        // Act
        val result = authProvider.signIn(credentials)

        // Assert
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.NetworkError>(result.error)
    }

    @Test
    fun `signIn with null user returned returns Failure`() = runTest {
        // Arrange
        fakeAuthService.signInWithEmailResult = FakeAuthResultWrapper(user = null)
        val credentials = Credentials.EmailPassword("test@example.com", "password")

        // Act
        val result = authProvider.signIn(credentials)

        // Assert
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.Unknown>(result.error)
    }

    @Test
    fun `signIn with RefreshToken calls refreshSession`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper(uid = "user-123")
        fakeAuthService.setCurrentUser(fakeUser)
        val credentials = Credentials.RefreshToken("refresh-token")

        // Act
        val result = authProvider.signIn(credentials)

        // Assert
        assertIs<AuthResult.Success>(result)
    }

    // ========== Sign Up Tests ==========

    @Test
    fun `signUp with valid data returns Success`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper(uid = "new-user-123", email = "newuser@example.com")
        fakeAuthService.createUserResult = FakeAuthResultWrapper(fakeUser)
        val signUpData = SignUpData(
            email = "newuser@example.com",
            password = "securePassword123"
        )

        // Act
        val result = authProvider.signUp(signUpData)

        // Assert
        assertIs<AuthResult.Success>(result)
        assertEquals("new-user-123", result.session.userId)
        assertEquals(1, fakeAuthService.createUserCallCount)
        assertEquals("newuser@example.com" to "securePassword123", fakeAuthService.createUserCalledWith)
    }

    @Test
    fun `signUp with display name updates profile`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper(uid = "new-user-123")
        fakeAuthService.createUserResult = FakeAuthResultWrapper(fakeUser)
        val signUpData = SignUpData(
            email = "newuser@example.com",
            password = "securePassword123",
            displayName = "John Doe"
        )

        // Act
        val result = authProvider.signUp(signUpData)

        // Assert
        assertIs<AuthResult.Success>(result)
        assertEquals(1, fakeUser.updateProfileCallCount)
        assertEquals("John Doe", fakeUser.updateProfileCalledWithDisplayName)
    }

    @Test
    fun `signUp without display name does not update profile`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper(uid = "new-user-123")
        fakeAuthService.createUserResult = FakeAuthResultWrapper(fakeUser)
        val signUpData = SignUpData(
            email = "newuser@example.com",
            password = "securePassword123",
            displayName = null
        )

        // Act
        authProvider.signUp(signUpData)

        // Assert
        assertEquals(0, fakeUser.updateProfileCallCount)
    }

    @Test
    fun `signUp with existing email returns Failure with EmailAlreadyInUse error`() = runTest {
        // Arrange
        fakeAuthService.createUserException = RuntimeException("EMAIL_EXISTS")
        val signUpData = SignUpData(
            email = "existing@example.com",
            password = "password123"
        )

        // Act
        val result = authProvider.signUp(signUpData)

        // Assert
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.EmailAlreadyInUse>(result.error)
    }

    @Test
    fun `signUp with weak password returns Failure with WeakPassword error`() = runTest {
        // Arrange
        fakeAuthService.createUserException = RuntimeException("WEAK_PASSWORD")
        val signUpData = SignUpData(
            email = "newuser@example.com",
            password = "123"
        )

        // Act
        val result = authProvider.signUp(signUpData)

        // Assert
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.WeakPassword>(result.error)
    }

    @Test
    fun `signUp with invalid email returns Failure with InvalidEmail error`() = runTest {
        // Arrange
        fakeAuthService.createUserException = RuntimeException("INVALID_EMAIL")
        val signUpData = SignUpData(
            email = "invalid-email",
            password = "password123"
        )

        // Act
        val result = authProvider.signUp(signUpData)

        // Assert
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.InvalidEmail>(result.error)
    }

    @Test
    fun `signUp with null user returned returns Failure`() = runTest {
        // Arrange
        fakeAuthService.createUserResult = FakeAuthResultWrapper(user = null)
        val signUpData = SignUpData(
            email = "newuser@example.com",
            password = "password123"
        )

        // Act
        val result = authProvider.signUp(signUpData)

        // Assert
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.Unknown>(result.error)
    }

    // ========== Sign Out Tests ==========

    @Test
    fun `signOut calls authService signOut`() = runTest {
        // Act
        authProvider.signOut()

        // Assert
        assertEquals(1, fakeAuthService.signOutCallCount)
    }

    @Test
    fun `signOut returns success`() = runTest {
        // Act
        val result = authProvider.signOut()

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `signOut with error returns failure`() = runTest {
        // Arrange
        fakeAuthService.signOutException = RuntimeException("Sign out failed")

        // Act
        val result = authProvider.signOut()

        // Assert
        assertTrue(result.isFailure)
    }

    // ========== Password Reset Tests ==========

    @Test
    fun `sendPasswordResetEmail returns PasswordResetSent`() = runTest {
        // Act
        val result = authProvider.sendPasswordResetEmail("test@example.com")

        // Assert
        assertIs<AuthResult.PasswordResetSent>(result)
        assertEquals(1, fakeAuthService.sendPasswordResetEmailCallCount)
        assertEquals("test@example.com", fakeAuthService.sendPasswordResetEmailCalledWith)
    }

    @Test
    fun `sendPasswordResetEmail with invalid email returns Failure`() = runTest {
        // Arrange
        fakeAuthService.sendPasswordResetEmailException = RuntimeException("INVALID_EMAIL")

        // Act
        val result = authProvider.sendPasswordResetEmail("invalid-email")

        // Assert
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.InvalidEmail>(result.error)
    }

    @Test
    fun `sendPasswordResetEmail with non-existent user returns Failure`() = runTest {
        // Arrange
        fakeAuthService.sendPasswordResetEmailException = RuntimeException("USER_NOT_FOUND")

        // Act
        val result = authProvider.sendPasswordResetEmail("nonexistent@example.com")

        // Assert
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.UserNotFound>(result.error)
    }

    @Test
    fun `confirmPasswordReset returns PasswordResetSuccess`() = runTest {
        // Act
        val result = authProvider.confirmPasswordReset("reset-code", "newPassword123")

        // Assert
        assertIs<AuthResult.PasswordResetSuccess>(result)
        assertEquals(1, fakeAuthService.confirmPasswordResetCallCount)
        assertEquals("reset-code" to "newPassword123", fakeAuthService.confirmPasswordResetCalledWith)
    }

    @Test
    fun `confirmPasswordReset with invalid code returns Failure`() = runTest {
        // Arrange
        fakeAuthService.confirmPasswordResetException = RuntimeException("INVALID_OOB_CODE")

        // Act
        val result = authProvider.confirmPasswordReset("invalid-code", "newPassword123")

        // Assert
        assertIs<AuthResult.Failure>(result)
    }

    // ========== Session Tests ==========

    @Test
    fun `refreshSession with current user returns Success`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper(uid = "user-123", email = "test@example.com")
        fakeAuthService.setCurrentUser(fakeUser)

        // Act
        val result = authProvider.refreshSession()

        // Assert
        assertIs<AuthResult.Success>(result)
        assertEquals("user-123", result.session.userId)
        assertEquals(1, fakeUser.getIdTokenCallCount)
        assertTrue(fakeUser.getIdTokenForceRefresh == true)
    }

    @Test
    fun `refreshSession without user returns SessionExpired`() = runTest {
        // Arrange
        fakeAuthService.setCurrentUser(null)

        // Act
        val result = authProvider.refreshSession()

        // Assert
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.SessionExpired>(result.error)
    }

    @Test
    fun `refreshSession with token error returns SessionExpired`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper(uid = "user-123")
        fakeUser.shouldThrowOnGetIdToken = RuntimeException("Token refresh failed")
        fakeAuthService.setCurrentUser(fakeUser)

        // Act
        val result = authProvider.refreshSession()

        // Assert
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.SessionExpired>(result.error)
    }

    @Test
    fun `isSignedIn returns true when user exists`() = runTest {
        // Arrange
        fakeAuthService.setCurrentUser(FakeUserWrapper())

        // Act
        val result = authProvider.isSignedIn()

        // Assert
        assertTrue(result)
    }

    @Test
    fun `isSignedIn returns false when no user`() = runTest {
        // Arrange
        fakeAuthService.setCurrentUser(null)

        // Act
        val result = authProvider.isSignedIn()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `getIdToken returns token from current user`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper()
        fakeUser.idTokenResult = "test-id-token"
        fakeAuthService.setCurrentUser(fakeUser)

        // Act
        val result = authProvider.getIdToken(forceRefresh = false)

        // Assert
        assertEquals("test-id-token", result)
        assertEquals(1, fakeUser.getIdTokenCallCount)
        assertFalse(fakeUser.getIdTokenForceRefresh!!)
    }

    @Test
    fun `getIdToken with forceRefresh true passes flag correctly`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper()
        fakeAuthService.setCurrentUser(fakeUser)

        // Act
        authProvider.getIdToken(forceRefresh = true)

        // Assert
        assertTrue(fakeUser.getIdTokenForceRefresh!!)
    }

    @Test
    fun `getIdToken returns null when no user`() = runTest {
        // Arrange
        fakeAuthService.setCurrentUser(null)

        // Act
        val result = authProvider.getIdToken(forceRefresh = false)

        // Assert
        assertNull(result)
    }

    @Test
    fun `getIdToken returns null on exception`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper()
        fakeUser.shouldThrowOnGetIdToken = RuntimeException("Token error")
        fakeAuthService.setCurrentUser(fakeUser)

        // Act
        val result = authProvider.getIdToken(forceRefresh = false)

        // Assert
        assertNull(result)
    }

    // ========== Account Management Tests ==========

    @Test
    fun `deleteAccount calls delete on current user`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper()
        fakeAuthService.setCurrentUser(fakeUser)

        // Act
        val result = authProvider.deleteAccount()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, fakeUser.deleteCallCount)
    }

    @Test
    fun `deleteAccount returns failure on exception`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper()
        fakeUser.shouldThrowOnDelete = RuntimeException("Delete failed")
        fakeAuthService.setCurrentUser(fakeUser)

        // Act
        val result = authProvider.deleteAccount()

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `deleteAccount with no user returns success`() = runTest {
        // Arrange
        fakeAuthService.setCurrentUser(null)

        // Act
        val result = authProvider.deleteAccount()

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateDisplayName calls updateProfile`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper()
        fakeAuthService.setCurrentUser(fakeUser)

        // Act
        val result = authProvider.updateDisplayName("New Name")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, fakeUser.updateProfileCallCount)
        assertEquals("New Name", fakeUser.updateProfileCalledWithDisplayName)
    }

    @Test
    fun `updateDisplayName returns failure on exception`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper()
        fakeUser.shouldThrowOnUpdateProfile = RuntimeException("Update failed")
        fakeAuthService.setCurrentUser(fakeUser)

        // Act
        val result = authProvider.updateDisplayName("New Name")

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `updateEmail calls verifyBeforeUpdateEmail`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper()
        fakeAuthService.setCurrentUser(fakeUser)

        // Act
        val result = authProvider.updateEmail("newemail@example.com")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, fakeUser.verifyBeforeUpdateEmailCallCount)
        assertEquals("newemail@example.com", fakeUser.verifyBeforeUpdateEmailCalledWith)
    }

    @Test
    fun `updateEmail returns failure on exception`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper()
        fakeUser.shouldThrowOnUpdateEmail = RuntimeException("Update failed")
        fakeAuthService.setCurrentUser(fakeUser)

        // Act
        val result = authProvider.updateEmail("newemail@example.com")

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `updatePassword calls updatePassword on user`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper()
        fakeAuthService.setCurrentUser(fakeUser)

        // Act
        val result = authProvider.updatePassword("newPassword123")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, fakeUser.updatePasswordCallCount)
        assertEquals("newPassword123", fakeUser.updatePasswordCalledWith)
    }

    @Test
    fun `updatePassword returns failure on exception`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper()
        fakeUser.shouldThrowOnUpdatePassword = RuntimeException("Update failed")
        fakeAuthService.setCurrentUser(fakeUser)

        // Act
        val result = authProvider.updatePassword("newPassword123")

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `sendEmailVerification calls sendEmailVerification on user`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper()
        fakeAuthService.setCurrentUser(fakeUser)

        // Act
        val result = authProvider.sendEmailVerification()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, fakeUser.sendEmailVerificationCallCount)
    }

    @Test
    fun `sendEmailVerification returns failure on exception`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper()
        fakeUser.shouldThrowOnSendEmailVerification = RuntimeException("Verification failed")
        fakeAuthService.setCurrentUser(fakeUser)

        // Act
        val result = authProvider.sendEmailVerification()

        // Assert
        assertTrue(result.isFailure)
    }

    // ========== Auth State Tests ==========

    @Test
    fun `observeAuthState emits Loading then Authenticated when user exists`() = runTest {
        // Arrange
        val fakeUser = FakeUserWrapper(uid = "user-123", email = "test@example.com")
        fakeAuthService.emitAuthState(fakeUser)

        // Act & Assert
        authProvider.observeAuthState().test {
            // First emission should be Loading (from onStart)
            val loading = awaitItem()
            assertIs<AuthState.Loading>(loading)

            // Second emission should be Authenticated
            val authenticated = awaitItem()
            assertIs<AuthState.Authenticated>(authenticated)
            assertEquals("user-123", authenticated.session.userId)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeAuthState emits Unauthenticated when no user`() = runTest {
        // Arrange
        fakeAuthService.emitAuthState(null)

        // Act & Assert
        authProvider.observeAuthState().test {
            // First emission should be Loading
            val loading = awaitItem()
            assertIs<AuthState.Loading>(loading)

            // Second emission should be Unauthenticated
            val unauthenticated = awaitItem()
            assertIs<AuthState.Unauthenticated>(unauthenticated)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== Reauthenticate Tests ==========

    @Test
    fun `reauthenticate returns OperationNotAllowed`() = runTest {
        // Act
        val result = authProvider.reauthenticate(
            Credentials.EmailPassword("test@example.com", "password")
        )

        // Assert
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.OperationNotAllowed>(result.error)
    }

    // ========== Error Mapping Tests ==========

    @Test
    fun `signIn with too many requests returns TooManyRequests error`() = runTest {
        // Arrange
        fakeAuthService.signInWithEmailException = RuntimeException("TOO_MANY_ATTEMPTS")
        val credentials = Credentials.EmailPassword("test@example.com", "password")

        // Act
        val result = authProvider.signIn(credentials)

        // Assert
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.TooManyRequests>(result.error)
    }

    @Test
    fun `signIn with disabled user returns UserDisabled error`() = runTest {
        // Arrange
        fakeAuthService.signInWithEmailException = RuntimeException("USER_DISABLED")
        val credentials = Credentials.EmailPassword("disabled@example.com", "password")

        // Act
        val result = authProvider.signIn(credentials)

        // Assert
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.UserDisabled>(result.error)
    }

    @Test
    fun `signIn with unknown exception returns Unknown error`() = runTest {
        // Arrange
        fakeAuthService.signInWithEmailException = RuntimeException("Something unexpected happened")
        val credentials = Credentials.EmailPassword("test@example.com", "password")

        // Act
        val result = authProvider.signIn(credentials)

        // Assert
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.Unknown>(result.error)
    }
}
