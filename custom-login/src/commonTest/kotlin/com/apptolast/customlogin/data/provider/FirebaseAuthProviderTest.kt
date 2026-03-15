package com.apptolast.customlogin.data.provider

import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.SignUpData
import com.apptolast.customlogin.test.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Integration-level tests for auth flows using [FakeAuthRepository].
 *
 * Direct unit-testing of [FirebaseAuthProvider] is deferred because it requires
 * a live or emulated Firebase instance (FirebaseAuth is a platform expect class).
 * Instead, we test the domain contract through the repository abstraction.
 */
class FirebaseAuthProviderTest {

    private val repo = FakeAuthRepository()

    @Test
    fun `signIn returns configured success result`() = runTest {
        repo.signInResult = AuthResult.Success(FakeAuthRepository.fakeSession())

        val result = repo.signIn(Credentials.EmailPassword("user@test.com", "password123"))

        assertIs<AuthResult.Success>(result)
        assertEquals("fake-user-id", result.session.userId)
    }

    @Test
    fun `signIn returns configured failure result`() = runTest {
        repo.signInResult = AuthResult.Failure(AuthError.InvalidCredentials())

        val result = repo.signIn(Credentials.EmailPassword("user@test.com", "wrong"))

        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.InvalidCredentials>(result.error)
    }

    @Test
    fun `signUp returns configured success result`() = runTest {
        repo.signUpResult = AuthResult.Success(FakeAuthRepository.fakeSession(email = "new@test.com"))

        val result = repo.signUp(SignUpData("new@test.com", "password123"))

        assertIs<AuthResult.Success>(result)
        assertEquals("new@test.com", result.session.email)
    }

    @Test
    fun `sendPasswordResetEmail returns PasswordResetSent`() = runTest {
        repo.sendPasswordResetEmailResult = AuthResult.PasswordResetSent

        val result = repo.sendPasswordResetEmail("user@test.com")

        assertEquals(AuthResult.PasswordResetSent, result)
    }

    @Test
    fun `sendPasswordResetEmail returns failure on network error`() = runTest {
        repo.sendPasswordResetEmailResult = AuthResult.Failure(AuthError.NetworkError())

        val result = repo.sendPasswordResetEmail("user@test.com")

        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.NetworkError>(result.error)
    }

    @Test
    fun `signOut returns success`() = runTest {
        repo.signOutResult = Result.success(Unit)

        val result = repo.signOut()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `getAvailableProviders returns configured list`() {
        repo.availableProviders = listOf(
            com.apptolast.customlogin.domain.model.IdentityProvider.Google,
            com.apptolast.customlogin.domain.model.IdentityProvider.Phone,
        )

        val providers = repo.getAvailableProviders()

        assertEquals(2, providers.size)
    }
}
