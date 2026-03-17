package com.apptolast.customlogin.domain

import com.apptolast.customlogin.domain.model.IdentityProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Verifies that each [IdentityProvider] carries the exact provider ID expected by Firebase.
 * A wrong ID silently breaks OAuth sign-in, so pinning these values in tests prevents
 * accidental regressions when providers are renamed or restructured.
 */
class IdentityProviderTest {

    @Test
    fun `Google id is google dot com`() {
        assertEquals("google.com", IdentityProvider.Google.id)
    }

    @Test
    fun `Apple id is apple dot com`() {
        assertEquals("apple.com", IdentityProvider.Apple.id)
    }

    @Test
    fun `Facebook id is facebook dot com`() {
        assertEquals("facebook.com", IdentityProvider.Facebook.id)
    }

    @Test
    fun `GitHub id is github dot com`() {
        assertEquals("github.com", IdentityProvider.GitHub.id)
    }

    @Test
    fun `Microsoft id is microsoft dot com`() {
        assertEquals("microsoft.com", IdentityProvider.Microsoft.id)
    }

    @Test
    fun `Twitter id is twitter dot com`() {
        assertEquals("twitter.com", IdentityProvider.Twitter.id)
    }

    @Test
    fun `MagicLink id is magiclink`() {
        assertEquals("magiclink", IdentityProvider.MagicLink.id)
    }

    @Test
    fun `Phone id is phone`() {
        assertEquals("phone", IdentityProvider.Phone.id)
    }

    @Test
    fun `Custom id matches the provided customId`() {
        val provider = IdentityProvider.Custom("my-custom-provider")
        assertEquals("my-custom-provider", provider.id)
        assertEquals("my-custom-provider", provider.customId)
    }

    @Test
    fun `all standard providers have distinct ids`() {
        val ids = listOf(
            IdentityProvider.Google.id,
            IdentityProvider.Apple.id,
            IdentityProvider.Facebook.id,
            IdentityProvider.GitHub.id,
            IdentityProvider.Microsoft.id,
            IdentityProvider.Twitter.id,
            IdentityProvider.MagicLink.id,
            IdentityProvider.Phone.id,
        )
        assertEquals(ids.size, ids.toSet().size, "Duplicate provider IDs detected")
    }

    @Test
    fun `two Custom providers with same customId are equal`() {
        assertEquals(IdentityProvider.Custom("foo"), IdentityProvider.Custom("foo"))
    }

    @Test
    fun `two Custom providers with different customIds are not equal`() {
        assertNotEquals(IdentityProvider.Custom("foo"), IdentityProvider.Custom("bar"))
    }
}
