package com.apptolast.customlogin.util

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Verifies that every [ValidationError] variant maps to a distinct [StringResource].
 * Tests don't need compose resource loading — we only compare object identity/equality
 * of the returned StringResource references.
 */
class ValidationErrorExtTest {

    private val allErrors = listOf(
        ValidationError.EmailEmpty,
        ValidationError.EmailInvalid,
        ValidationError.PasswordEmpty,
        ValidationError.PasswordTooShort,
        ValidationError.NameRequired,
        ValidationError.ConfirmPasswordEmpty,
        ValidationError.PasswordsDoNotMatch,
        ValidationError.PhoneEmpty,
        ValidationError.OtpEmpty,
    )

    @Test
    fun `all ValidationError variants return a non-null StringResource`() {
        allErrors.forEach { error ->
            assertNotNull(error.toStringRes(), "toStringRes() returned null for $error")
        }
    }

    @Test
    fun `all ValidationError variants map to distinct StringResources`() {
        val resources = allErrors.map { it.toStringRes() }
        val unique = resources.toSet()
        assertTrue(
            unique.size == allErrors.size,
            "Expected ${allErrors.size} distinct resources but got ${unique.size}. " +
                "Possible collision in ValidationErrorExt.toStringRes()",
        )
    }

    @Test
    fun `EmailEmpty and EmailInvalid map to different resources`() {
        assertTrue(
            ValidationError.EmailEmpty.toStringRes() != ValidationError.EmailInvalid.toStringRes(),
        )
    }

    @Test
    fun `PasswordEmpty and PasswordTooShort map to different resources`() {
        assertTrue(
            ValidationError.PasswordEmpty.toStringRes() != ValidationError.PasswordTooShort.toStringRes(),
        )
    }

    @Test
    fun `ConfirmPasswordEmpty and PasswordsDoNotMatch map to different resources`() {
        assertTrue(
            ValidationError.ConfirmPasswordEmpty.toStringRes() != ValidationError.PasswordsDoNotMatch.toStringRes(),
        )
    }

    @Test
    fun `PhoneEmpty and OtpEmpty map to different resources`() {
        assertTrue(
            ValidationError.PhoneEmpty.toStringRes() != ValidationError.OtpEmpty.toStringRes(),
        )
    }
}
