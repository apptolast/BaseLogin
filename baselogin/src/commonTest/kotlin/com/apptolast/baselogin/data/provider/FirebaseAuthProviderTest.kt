package com.apptolast.baselogin.data.provider

import com.apptolast.baselogin.data.FirebaseAuthProvider
import com.apptolast.baselogin.data.firebase.FirebaseAuthCredential
import com.apptolast.baselogin.data.firebase.FirebaseAuthFailure
import com.apptolast.baselogin.data.firebase.FirebaseAuthUser
import com.apptolast.baselogin.domain.model.AuthError
import com.apptolast.baselogin.domain.model.AuthResult
import com.apptolast.baselogin.domain.model.AuthState
import com.apptolast.baselogin.domain.model.Credentials
import com.apptolast.baselogin.domain.model.IdentityProvider
import com.apptolast.baselogin.domain.model.SignUpData
import com.apptolast.baselogin.domain.model.UserSession
import com.apptolast.baselogin.test.FakeFirebaseAuthGateway
import com.apptolast.baselogin.test.FakePhoneAuthPort
import com.apptolast.baselogin.test.FakeSocialSignInStateCleaner
import com.apptolast.baselogin.test.FakeSocialTokenProvider
import com.apptolast.baselogin.test.FakeSocialTokenRevoker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [FirebaseAuthProvider] against the [FakeFirebaseAuthGateway] port.
 *
 * These replace the previous file of the same name, which exercised `FakeAuthRepository` — a fake
 * asserting against another fake — and proved nothing about the provider. Direct testing became
 * possible only once `FirebaseAuth`, a platform `expect` type, stopped being a constructor argument.
 */
class FirebaseAuthProviderTest {

    private val gateway = FakeFirebaseAuthGateway()
    private val socialTokens = FakeSocialTokenProvider()
    private val socialCleaner = FakeSocialSignInStateCleaner()
    private val revoker = FakeSocialTokenRevoker()
    private val phoneAuth = FakePhoneAuthPort()

    /**
     * Three arguments on purpose: this is also the proof that spec 005 did not break the constructor
     * every consumer already builds by hand.
     */
    private fun provider() = FirebaseAuthProvider(gateway, socialTokens, socialCleaner)

    private fun providerWithRevoker() = FirebaseAuthProvider(gateway, socialTokens, socialCleaner, revoker)

    private fun providerWithPhone() = FirebaseAuthProvider(gateway, socialTokens, socialCleaner, revoker, phoneAuth)

    // ── AC-03: email/password ────────────────────────────────────────────────

    @Test
    fun `FLE-90 email password sign in delegates to the gateway`() = runTest {
        // Given
        gateway.user = FirebaseAuthUser(uid = "u-1", email = "user@test.com")

        // When
        val result = provider().signIn(Credentials.EmailPassword("user@test.com", "secret"))

        // Then
        assertIs<AuthResult.Success>(result)
        assertEquals("u-1", result.session.userId)
        val credential = gateway.credentials.single()
        assertIs<FirebaseAuthCredential.EmailPassword>(credential)
        assertEquals("user@test.com", credential.email)
    }

    // ── AC-04: registro ──────────────────────────────────────────────────────

    @Test
    fun `FLE-90 sign up propagates the display name to the profile`() = runTest {
        // Given
        gateway.user = FirebaseAuthUser(uid = "u-2", email = "new@test.com")

        // When
        val result = provider().signUp(SignUpData("new@test.com", "secret", displayName = "Ana"))

        // Then
        assertIs<AuthResult.Success>(result)
        assertEquals(1, gateway.signUpCalls)
        assertEquals(listOf("Ana"), gateway.updatedDisplayNames)
    }

    @Test
    fun `FLE-90 sign up without display name does not touch the profile`() = runTest {
        gateway.user = FirebaseAuthUser(uid = "u-2", email = "new@test.com")

        provider().signUp(SignUpData("new@test.com", "secret"))

        assertTrue(gateway.updatedDisplayNames.isEmpty())
    }

    // ── AC-05: Google ────────────────────────────────────────────────────────

    @Test
    fun `FLE-90 google sign in forwards id token and access token`() = runTest {
        // Given
        gateway.user = FirebaseAuthUser(uid = "u-3")
        socialTokens.returnsToken(IdentityProvider.Google, "g-token|||accessToken|||at-1")

        // When
        val result = provider().signIn(Credentials.OAuthToken(IdentityProvider.Google))

        // Then
        assertIs<AuthResult.Success>(result)
        val credential = gateway.credentials.single()
        assertIs<FirebaseAuthCredential.Google>(credential)
        assertEquals("g-token", credential.idToken)
        assertEquals("at-1", credential.accessToken)
    }

    // ── AC-06 / AC-07 / AC-08: Apple ─────────────────────────────────────────

    @Test
    fun `FLE-90 apple sign in forwards id token and raw nonce`() = runTest {
        gateway.user = FirebaseAuthUser(uid = "u-4")
        socialTokens.returnsToken(IdentityProvider.Apple, "a-token|||rawNonce|||n-1")

        val result = provider().signIn(Credentials.OAuthToken(IdentityProvider.Apple))

        assertIs<AuthResult.Success>(result)
        val credential = gateway.credentials.single()
        assertIs<FirebaseAuthCredential.OAuth>(credential)
        assertEquals("apple.com", credential.providerId)
        assertEquals("a-token", credential.idToken)
        assertEquals("n-1", credential.rawNonce)
    }

    @Test
    fun `FLE-90 apple sign in propagates the display name`() = runTest {
        // Given: Apple only sends the full name on the very first authorisation ever,
        // and the Firebase user comes back without one.
        gateway.user = FirebaseAuthUser(uid = "u-5", displayName = null)
        socialTokens.returnsToken(
            IdentityProvider.Apple,
            "a-token|||rawNonce|||n-1|||displayName|||Ana Perez",
        )

        // When
        val result = provider().signIn(Credentials.OAuthToken(IdentityProvider.Apple))

        // Then
        assertIs<AuthResult.Success>(result)
        assertEquals(listOf("Ana Perez"), gateway.updatedDisplayNames)
        assertEquals("Ana Perez", result.session.displayName)

        // and the nonce still travels
        val credential = gateway.credentials.single()
        assertIs<FirebaseAuthCredential.OAuth>(credential)
        assertEquals("n-1", credential.rawNonce)
    }

    @Test
    fun `FLE-90 apple sign in keeps an existing display name`() = runTest {
        gateway.user = FirebaseAuthUser(uid = "u-6", displayName = "Ya tenia nombre")
        socialTokens.returnsToken(
            IdentityProvider.Apple,
            "a-token|||rawNonce|||n-1|||displayName|||Nombre nuevo",
        )

        provider().signIn(Credentials.OAuthToken(IdentityProvider.Apple))

        assertTrue(gateway.updatedDisplayNames.isEmpty())
    }

    @Test
    fun `FLE-90 legacy apple token format still signs in`() = runTest {
        // Given: the two-segment format that every already-integrated Swift host produces
        gateway.user = FirebaseAuthUser(uid = "u-7")
        socialTokens.returnsToken(IdentityProvider.Apple, "a-token|||rawNonce|||n-1")

        val result = provider().signIn(Credentials.OAuthToken(IdentityProvider.Apple))

        assertIs<AuthResult.Success>(result)
        assertTrue(gateway.updatedDisplayNames.isEmpty())
    }

    @Test
    fun `FLE-90 bare apple token without nonce still signs in`() = runTest {
        gateway.user = FirebaseAuthUser(uid = "u-8")
        socialTokens.returnsToken(IdentityProvider.Apple, "a-token")

        val result = provider().signIn(Credentials.OAuthToken(IdentityProvider.Apple))

        assertIs<AuthResult.Success>(result)
        val credential = gateway.credentials.single()
        assertIs<FirebaseAuthCredential.OAuth>(credential)
        assertEquals("a-token", credential.idToken)
        assertEquals(null, credential.rawNonce)
    }

    @Test
    fun `FLE-90 cancelled social sign in never touches the gateway`() = runTest {
        socialTokens.returnsCancelled(IdentityProvider.Google)

        val result = provider().signIn(Credentials.OAuthToken(IdentityProvider.Google))

        assertIs<AuthResult.Failure>(result)
        assertEquals(0, gateway.totalInteractions)
    }

    // ── AC-09: mapeo de errores ──────────────────────────────────────────────

    @Test
    fun `FLE-90 network failures map to NetworkError and not to Unknown`() = runTest {
        // Given: FirebaseNetworkException is a SIBLING of FirebaseAuthException, not a subclass,
        // so it used to slip through catch(FirebaseAuthException) into the generic catch.
        gateway.failWith = FirebaseAuthFailure("A network error (such as timeout) occurred.")

        val result = provider().signIn(Credentials.EmailPassword("user@test.com", "secret"))

        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.NetworkError>(result.error)
    }

    @Test
    fun `FLE-90 wrong password maps to InvalidCredentials`() = runTest {
        gateway.failWith = FirebaseAuthFailure("ERROR_WRONG_PASSWORD")

        val result = provider().signIn(Credentials.EmailPassword("user@test.com", "bad"))

        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.InvalidCredentials>(result.error)
    }

    @Test
    fun `FLE-90 too many requests maps to TooManyRequests`() = runTest {
        gateway.failWith = FirebaseAuthFailure("We have blocked all requests: too-many-requests")

        val result = provider().signIn(Credentials.EmailPassword("user@test.com", "secret"))

        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.TooManyRequests>(result.error)
    }

    // ── Cuarto defecto: getCurrentSession no debe hacer I/O ──────────────────

    @Test
    fun `FLE-90 get current session reads the cache without requesting a token`() = runTest {
        // Given: AuthProvider documents that getCurrentSession MUST NOT perform network I/O.
        gateway.user = FirebaseAuthUser(uid = "u-9", email = "cached@test.com")

        // When
        val session = provider().getCurrentSession()

        // Then
        assertEquals("u-9", session?.userId)
        assertEquals(0, gateway.getIdTokenCalls)
    }

    @Test
    fun `FLE-90 get current session returns null when signed out`() = runTest {
        gateway.user = null

        assertEquals(null, provider().getCurrentSession())
    }

    // ── AC-10: signOut limpia el estado social ───────────────────────────────

    @Test
    fun `FLE-90 sign out clears the platform social state`() = runTest {
        gateway.user = FirebaseAuthUser(uid = "u-10")

        val result = provider().signOut()

        assertTrue(result.isSuccess)
        assertEquals(1, gateway.signOutCalls)
        assertEquals(1, socialCleaner.clearCalls)
    }

    // ── AC-11: flujo de estado ───────────────────────────────────────────────

    @Test
    fun `FLE-90 auth state stream starts with Loading`() = runTest {
        assertIs<AuthState.Loading>(provider().observeAuthState().first())
    }

    // ── Reset de contrasena ──────────────────────────────────────────────────

    @Test
    fun `FLE-90 password reset delegates the email to the gateway`() = runTest {
        val result = provider().sendPasswordResetEmail("user@test.com")

        assertIs<AuthResult.PasswordResetSent>(result)
        assertEquals(listOf("user@test.com"), gateway.passwordResetEmails)
    }

    // ── 005 AC-01…AC-04: revocacion al borrar la cuenta ──────────────────────

    @Test
    fun `005 deleteAccount revokes the apple token before deleting the user`() = runTest {
        // Given: Apple requires the token to be revoked, not just the account removed (5.1.1(v)).
        gateway.user = FirebaseAuthUser(uid = "u-12", providerIds = listOf("apple.com"))
        var userStillAliveWhenRevoked: Boolean? = null
        revoker.onRevoke = { userStillAliveWhenRevoked = gateway.user != null }

        // When
        val result = providerWithRevoker().deleteAccount()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(IdentityProvider.Apple, revoker.revoked.single())
        assertEquals(true, userStillAliveWhenRevoked, "revocation must happen before the deletion")
        assertNull(gateway.user)
    }

    @Test
    fun `005 deleteAccount does not revoke for an email only user`() = runTest {
        // Given: an email user in an app that also offers Apple must not be shown an Apple sheet.
        gateway.user = FirebaseAuthUser(uid = "u-13", providerIds = listOf("password"))

        // When
        val result = providerWithRevoker().deleteAccount()

        // Then
        assertTrue(result.isSuccess)
        assertTrue(revoker.revoked.isEmpty())
        assertNull(gateway.user)
    }

    @Test
    fun `005 a failing revocation still deletes the account`() = runTest {
        // Given: a user who asked to delete their account cannot be stuck behind Apple's servers.
        gateway.user = FirebaseAuthUser(uid = "u-14", providerIds = listOf("apple.com"))
        revoker.failWith = IllegalStateException("apple is down")

        // When
        val result = providerWithRevoker().deleteAccount()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(IdentityProvider.Apple, revoker.revoked.single())
        assertNull(gateway.user)
    }

    @Test
    fun `005 a failing deletion is reported as a failure`() = runTest {
        gateway.user = FirebaseAuthUser(uid = "u-15", providerIds = listOf("apple.com"))
        gateway.failWith = FirebaseAuthFailure("requires-recent-login")

        val result = providerWithRevoker().deleteAccount()

        assertTrue(result.isFailure)
    }

    // ── 009: la sesion de telefono, igual que las demas ───────────────────────

    @Test
    fun `009 verifyPhoneOtp returns the full user, not the platform stub`() = runTest {
        // Given: iOS only knows the uid its Swift handler sent back, while Android fills the session
        // from the Firebase user. The gateway is the one that knows the whole user.
        phoneAuth.verifyResult = AuthResult.Success(UserSession(userId = "p-1", email = null))
        gateway.user = FirebaseAuthUser(
            uid = "p-1",
            email = "phone@test.com",
            displayName = "Ana",
            isEmailVerified = true,
        )

        // When
        val result = providerWithPhone().verifyPhoneOtp("v-1", "123456")

        // Then
        assertIs<AuthResult.Success>(result)
        assertEquals("p-1", result.session.userId)
        assertEquals("phone@test.com", result.session.email)
        assertEquals("Ana", result.session.displayName)
        assertTrue(result.session.isEmailVerified)
    }

    @Test
    fun `009 verifyPhoneOtp forwards the code to the platform`() = runTest {
        gateway.user = FirebaseAuthUser(uid = "p-2")

        providerWithPhone().verifyPhoneOtp("v-2", "654321")

        assertEquals("v-2" to "654321", phoneAuth.verifiedCodes.single())
    }

    @Test
    fun `009 a rejected otp is propagated untouched`() = runTest {
        // Given: no session to read back, so the platform's failure has to survive
        phoneAuth.verifyResult = AuthResult.Failure(AuthError.InvalidCredentials())

        val result = providerWithPhone().verifyPhoneOtp("v-3", "000000")

        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.InvalidCredentials>(result.error)
    }

    @Test
    fun `009 sendPhoneOtp forwards the timeout to the platform`() = runTest {
        providerWithPhone().sendPhoneOtp("+34600000000", timeoutSeconds = 90)

        assertEquals("+34600000000" to 90L, phoneAuth.sentCodes.single())
    }
}
