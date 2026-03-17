package com.apptolast.customlogin.domain

import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.UserSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

class AuthResultTest {

    private fun session(userId: String = "uid-1") = UserSession(
        userId = userId,
        email = "user@example.com",
        isEmailVerified = true,
        providerId = "firebase",
    )

    // ── Success ────────────────────────────────────────────────────────────

    @Test
    fun `Success holds the provided session`() {
        val s = session()
        val result = AuthResult.Success(s)
        assertIs<AuthResult.Success>(result)
        assertEquals(s, result.session)
    }

    @Test
    fun `Success instances with the same session are equal`() {
        val s = session()
        assertEquals(AuthResult.Success(s), AuthResult.Success(s))
    }

    @Test
    fun `Success instances with different sessions are not equal`() {
        assertNotEquals(AuthResult.Success(session("uid-1")), AuthResult.Success(session("uid-2")))
    }

    // ── Failure ────────────────────────────────────────────────────────────

    @Test
    fun `Failure holds the provided error`() {
        val error = AuthError.InvalidCredentials()
        val result = AuthResult.Failure(error)
        assertIs<AuthResult.Failure>(result)
        assertEquals(error, result.error)
    }

    @Test
    fun `Failure wrapping different error types are not equal`() {
        assertNotEquals(
            AuthResult.Failure(AuthError.InvalidCredentials()),
            AuthResult.Failure(AuthError.UserNotFound()),
        )
    }

    // ── Terminal object results ────────────────────────────────────────────

    @Test
    fun `RequiresEmailVerification is a distinct result`() {
        assertIs<AuthResult>(AuthResult.RequiresEmailVerification)
        assertNotEquals<AuthResult>(AuthResult.RequiresEmailVerification, AuthResult.PasswordResetSent)
    }

    @Test
    fun `PasswordResetSent is a distinct result`() {
        assertIs<AuthResult>(AuthResult.PasswordResetSent)
        assertNotEquals<AuthResult>(AuthResult.PasswordResetSent, AuthResult.PasswordResetSuccess)
    }

    @Test
    fun `PasswordResetSuccess is a distinct result`() {
        assertIs<AuthResult>(AuthResult.PasswordResetSuccess)
        assertNotEquals<AuthResult>(AuthResult.PasswordResetSuccess, AuthResult.MagicLinkSent)
    }

    @Test
    fun `MagicLinkSent is a distinct result`() {
        assertIs<AuthResult>(AuthResult.MagicLinkSent)
        assertNotEquals<AuthResult>(AuthResult.MagicLinkSent, AuthResult.RequiresEmailVerification)
    }
}
