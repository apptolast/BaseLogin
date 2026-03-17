package com.apptolast.customlogin.domain

import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.domain.model.PasswordResetData
import com.apptolast.customlogin.domain.model.SignUpData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CredentialsTest {

    // ── EmailPassword ──────────────────────────────────────────────────────

    @Test
    fun `EmailPassword stores email and password`() {
        val creds = Credentials.EmailPassword("user@example.com", "secret123")
        assertEquals("user@example.com", creds.email)
        assertEquals("secret123", creds.password)
    }

    @Test
    fun `two EmailPassword with same values are equal`() {
        assertEquals(
            Credentials.EmailPassword("a@b.com", "pass"),
            Credentials.EmailPassword("a@b.com", "pass"),
        )
    }

    // ── OAuthToken ─────────────────────────────────────────────────────────

    @Test
    fun `OAuthToken stores the provider`() {
        val creds = Credentials.OAuthToken(IdentityProvider.Google)
        assertEquals(IdentityProvider.Google, creds.provider)
    }

    @Test
    fun `OAuthToken with different providers are not equal`() {
        val google = Credentials.OAuthToken(IdentityProvider.Google)
        val github = Credentials.OAuthToken(IdentityProvider.GitHub)
        assertTrue(google != github)
    }

    // ── RefreshToken ───────────────────────────────────────────────────────

    @Test
    fun `RefreshToken stores the token string`() {
        val creds = Credentials.RefreshToken("my-refresh-token")
        assertEquals("my-refresh-token", creds.token)
    }

    // ── SignUpData ─────────────────────────────────────────────────────────

    @Test
    fun `SignUpData displayName defaults to null`() {
        val data = SignUpData(email = "a@b.com", password = "pass123")
        assertNull(data.displayName)
    }

    @Test
    fun `SignUpData photoUrl defaults to null`() {
        val data = SignUpData(email = "a@b.com", password = "pass123")
        assertNull(data.photoUrl)
    }

    @Test
    fun `SignUpData metadata defaults to empty map`() {
        val data = SignUpData(email = "a@b.com", password = "pass123")
        assertTrue(data.metadata.isEmpty())
    }

    @Test
    fun `SignUpData stores custom displayName`() {
        val data = SignUpData(email = "a@b.com", password = "pass123", displayName = "Alice")
        assertEquals("Alice", data.displayName)
    }

    // ── PasswordResetData ──────────────────────────────────────────────────

    @Test
    fun `PasswordResetData stores code and newPassword`() {
        val data = PasswordResetData(code = "reset-code-xyz", newPassword = "newPass123")
        assertEquals("reset-code-xyz", data.code)
        assertEquals("newPass123", data.newPassword)
    }
}
