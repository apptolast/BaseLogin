package com.apptolast.customlogin.data

import com.apptolast.customlogin.config.AppleSignInConfig
import com.apptolast.customlogin.config.GoogleSignInConfig
import com.apptolast.customlogin.config.MagicLinkConfig
import com.apptolast.customlogin.di.LoginLibraryConfig
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.domain.model.PasswordResetData
import com.apptolast.customlogin.domain.model.SignUpData
import com.apptolast.customlogin.test.FakeAuthProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthRepositoryImplTest {

    private fun repo(config: LoginLibraryConfig, provider: FakeAuthProvider = FakeAuthProvider()) =
        AuthRepositoryImpl(provider, config)

    // ── getAvailableProviders ─────────────────────────────────────────────

    @Test
    fun `getAvailableProviders is empty with default config`() {
        val result = repo(LoginLibraryConfig()).getAvailableProviders()

        // Default: only phone is enabled by default
        assertEquals(listOf(IdentityProvider.Phone), result)
    }

    @Test
    fun `getAvailableProviders includes Phone when phoneEnabled is true`() {
        val config = LoginLibraryConfig(phoneEnabled = true)

        assertTrue(repo(config).getAvailableProviders().contains(IdentityProvider.Phone))
    }

    @Test
    fun `getAvailableProviders excludes Phone when phoneEnabled is false`() {
        val config = LoginLibraryConfig(phoneEnabled = false)

        assertFalse(repo(config).getAvailableProviders().contains(IdentityProvider.Phone))
    }

    @Test
    fun `getAvailableProviders includes Google when googleSignInConfig is set`() {
        val config = LoginLibraryConfig(googleSignInConfig = GoogleSignInConfig("client-id"))

        assertTrue(repo(config).getAvailableProviders().contains(IdentityProvider.Google))
    }

    @Test
    fun `getAvailableProviders includes GitHub when githubEnabled is true`() {
        val config = LoginLibraryConfig(githubEnabled = true)

        assertTrue(repo(config).getAvailableProviders().contains(IdentityProvider.GitHub))
    }

    @Test
    fun `getAvailableProviders includes Microsoft when microsoftEnabled is true`() {
        val config = LoginLibraryConfig(microsoftEnabled = true)

        assertTrue(repo(config).getAvailableProviders().contains(IdentityProvider.Microsoft))
    }

    @Test
    fun `getAvailableProviders includes MagicLink when magicLinkConfig is set`() {
        val config = LoginLibraryConfig(magicLinkConfig = MagicLinkConfig("https://example.com/auth"))

        assertTrue(repo(config).getAvailableProviders().contains(IdentityProvider.MagicLink))
    }

    @Test
    fun `getAvailableProviders includes Twitter when twitterEnabled is true`() {
        val config = LoginLibraryConfig(twitterEnabled = true)

        assertTrue(repo(config).getAvailableProviders().contains(IdentityProvider.Twitter))
    }

    @Test
    fun `getAvailableProviders excludes Twitter when twitterEnabled is false`() {
        val config = LoginLibraryConfig(twitterEnabled = false)

        assertFalse(repo(config).getAvailableProviders().contains(IdentityProvider.Twitter))
    }

    @Test
    fun `getAvailableProviders includes Facebook when facebookEnabled is true`() {
        val config = LoginLibraryConfig(facebookEnabled = true)

        assertTrue(repo(config).getAvailableProviders().contains(IdentityProvider.Facebook))
    }

    @Test
    fun `getAvailableProviders excludes Facebook when facebookEnabled is false`() {
        val config = LoginLibraryConfig(facebookEnabled = false)

        assertFalse(repo(config).getAvailableProviders().contains(IdentityProvider.Facebook))
    }

    @Test
    fun `getAvailableProviders returns all enabled providers`() {
        val config = LoginLibraryConfig(
            googleSignInConfig = GoogleSignInConfig("id"),
            githubEnabled = true,
            microsoftEnabled = true,
            phoneEnabled = true,
        )

        val providers = repo(config).getAvailableProviders()

        assertTrue(providers.contains(IdentityProvider.Google))
        assertTrue(providers.contains(IdentityProvider.GitHub))
        assertTrue(providers.contains(IdentityProvider.Microsoft))
        assertTrue(providers.contains(IdentityProvider.Phone))
    }

    // ── delegation ────────────────────────────────────────────────────────

    @Test
    fun `signIn delegates to provider`() = runTest {
        val provider = FakeAuthProvider()
        provider.signInResult = AuthResult.Failure(AuthError.InvalidCredentials())
        val r = repo(LoginLibraryConfig(), provider)

        val result = r.signIn(Credentials.EmailPassword("a@b.com", "pass"))

        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.InvalidCredentials>(result.error)
    }

    @Test
    fun `confirmPasswordReset delegates to provider`() = runTest {
        val provider = FakeAuthProvider()
        provider.confirmPasswordResetResult = AuthResult.PasswordResetSuccess
        val r = repo(LoginLibraryConfig(), provider)

        val result = r.confirmPasswordReset(PasswordResetData("code123", "newPass"))

        assertEquals(AuthResult.PasswordResetSuccess, result)
    }

    @Test
    fun `signUp delegates to provider`() = runTest {
        val provider = FakeAuthProvider()
        provider.signUpResult = AuthResult.Failure(AuthError.EmailAlreadyInUse())
        val r = repo(LoginLibraryConfig(), provider)

        val result = r.signUp(SignUpData(email = "a@b.com", password = "pass123"))

        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.EmailAlreadyInUse>(result.error)
    }

    @Test
    fun `sendPasswordResetEmail delegates to provider`() = runTest {
        val provider = FakeAuthProvider()
        provider.sendPasswordResetEmailResult = AuthResult.PasswordResetSent
        val r = repo(LoginLibraryConfig(), provider)

        val result = r.sendPasswordResetEmail("user@example.com")

        assertEquals(AuthResult.PasswordResetSent, result)
    }

    @Test
    fun `reauthenticate delegates to provider`() = runTest {
        val provider = FakeAuthProvider()
        provider.reauthenticateResult = AuthResult.Failure(AuthError.InvalidCredentials())
        val r = repo(LoginLibraryConfig(), provider)

        val result = r.reauthenticate(Credentials.EmailPassword("a@b.com", "wrong"))

        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.InvalidCredentials>(result.error)
    }

    @Test
    fun `signOut delegates to provider`() = runTest {
        val provider = FakeAuthProvider()
        provider.signOutResult = Result.success(Unit)
        val r = repo(LoginLibraryConfig(), provider)

        val result = r.signOut()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `getAvailableProviders includes Apple when appleSignInConfig is set`() {
        val config = LoginLibraryConfig(appleSignInConfig = AppleSignInConfig())

        assertTrue(repo(config).getAvailableProviders().contains(IdentityProvider.Apple))
    }

    @Test
    fun `getAvailableProviders excludes Apple when appleSignInConfig is null`() {
        val config = LoginLibraryConfig(appleSignInConfig = null)

        assertFalse(repo(config).getAvailableProviders().contains(IdentityProvider.Apple))
    }

    @Test
    fun `sendMagicLink returns OperationNotAllowed when magicLinkConfig is null`() = runTest {
        val r = repo(LoginLibraryConfig(magicLinkConfig = null))

        val result = r.sendMagicLink("user@example.com")

        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.OperationNotAllowed>(result.error)
    }
}
