package com.apptolast.baselogin.data

import com.apptolast.baselogin.domain.model.AuthError
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Spec 013 AC-25 — the log line of a failure carries enough to diagnose it and nothing personal.
 *
 * `Logger` is an `expect object` and cannot be faked, so the line is built by a pure function and
 * that is what gets tested.
 */
class AuthFailureLogTest {

    @Test
    fun `013 the failure log line carries the code and exception class but not the email`() {
        // Given: an iOS collision whose message (and so the AuthError built from it) holds the email
        val email = "ana@example.com"
        val error = AuthError.AccountExistsWithDifferentCredential(
            message = "Error Domain=FIRAuthErrorDomain Code=17012 UserInfo={FIRAuthErrorUserInfoEmailKey=$email}",
        )

        // When
        val line = authFailureLogLine(
            code = "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL",
            exceptionClass = "FirebaseAuthFailure",
            error = error,
        )

        // Then
        assertTrue(line.contains("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL"), line)
        assertTrue(line.contains("FirebaseAuthFailure"), line)
        assertTrue(line.contains("AccountExistsWithDifferentCredential"), line)
        assertFalse(line.contains(email), line)
    }

    @Test
    fun `013 the failure log line never leaks the message of an Unknown error`() {
        // Given: no code, so the resulting Unknown keeps the raw message that may hold PII
        val email = "ana@example.com"
        val error = AuthError.Unknown(message = "FIRAuthErrorUserInfoEmailKey=$email", cause = null)

        // When
        val line = authFailureLogLine(code = null, exceptionClass = "FirebaseAuthFailure", error = error)

        // Then
        assertTrue(line.contains("Unknown"), line)
        assertFalse(line.contains(email), line)
    }

    @Test
    fun `013 a null google social token is logged with the provider and SignInCancelled`() {
        // Given
        val providerId = "google.com"

        // When
        val line = nullSocialTokenLogLine(providerId)

        // Then
        assertTrue(line.contains("google.com"), line)
        assertTrue(line.contains("SignInCancelled"), line)
    }
}
