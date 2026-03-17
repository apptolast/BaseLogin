package com.apptolast.customlogin.domain

import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.PhoneAuthResult
import com.apptolast.customlogin.domain.model.UserSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

class PhoneAuthResultTest {

    private fun session() = UserSession(
        userId = "uid-1",
        email = "user@example.com",
        providerId = "phone",
    )

    @Test
    fun `CodeSent stores verificationId`() {
        val result = PhoneAuthResult.CodeSent("verification-abc")
        assertIs<PhoneAuthResult.CodeSent>(result)
        assertEquals("verification-abc", result.verificationId)
    }

    @Test
    fun `CodeSent instances with same id are equal`() {
        assertEquals(
            PhoneAuthResult.CodeSent("id-1"),
            PhoneAuthResult.CodeSent("id-1"),
        )
    }

    @Test
    fun `CodeSent instances with different ids are not equal`() {
        assertNotEquals(
            PhoneAuthResult.CodeSent("id-1"),
            PhoneAuthResult.CodeSent("id-2"),
        )
    }

    @Test
    fun `AutoSignedIn stores the user session`() {
        val s = session()
        val result = PhoneAuthResult.AutoSignedIn(s)
        assertIs<PhoneAuthResult.AutoSignedIn>(result)
        assertEquals(s, result.session)
    }

    @Test
    fun `Failure stores the auth error`() {
        val error = AuthError.PhoneNumberInvalid()
        val result = PhoneAuthResult.Failure(error)
        assertIs<PhoneAuthResult.Failure>(result)
        assertIs<AuthError.PhoneNumberInvalid>(result.error)
    }

    @Test
    fun `Failure with different errors are not equal`() {
        assertNotEquals(
            PhoneAuthResult.Failure(AuthError.PhoneNumberInvalid()),
            PhoneAuthResult.Failure(AuthError.InvalidVerificationCode()),
        )
    }
}
