package com.apptolast.customlogin.data

import com.apptolast.customlogin.domain.model.AuthError
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests for [mapFirebaseErrorMessage] — pure function, no mocks required.
 * Covers both Firebase REST API codes (ALL_CAPS) and native SDK codes (kebab-case).
 */
class DataMapperTest {

    // ── InvalidCredentials ─────────────────────────────────────────────────

    @Test
    fun `INVALID_CREDENTIAL maps to InvalidCredentials`() {
        assertTrue(mapFirebaseErrorMessage("INVALID_CREDENTIAL") is AuthError.InvalidCredentials)
    }

    @Test
    fun `ERROR_INVALID_CREDENTIAL maps to InvalidCredentials`() {
        assertTrue(mapFirebaseErrorMessage("ERROR_INVALID_CREDENTIAL") is AuthError.InvalidCredentials)
    }

    @Test
    fun `INVALID_LOGIN_CREDENTIALS maps to InvalidCredentials`() {
        assertTrue(mapFirebaseErrorMessage("INVALID_LOGIN_CREDENTIALS") is AuthError.InvalidCredentials)
    }

    @Test
    fun `wrong-password embedded in message maps to InvalidCredentials`() {
        assertTrue(
            mapFirebaseErrorMessage("The password is invalid. [wrong-password]") is AuthError.InvalidCredentials
        )
    }

    // ── UserNotFound ───────────────────────────────────────────────────────

    @Test
    fun `USER_NOT_FOUND maps to UserNotFound`() {
        assertTrue(mapFirebaseErrorMessage("USER_NOT_FOUND") is AuthError.UserNotFound)
    }

    @Test
    fun `user-not-found maps to UserNotFound`() {
        assertTrue(mapFirebaseErrorMessage("user-not-found") is AuthError.UserNotFound)
    }

    // ── EmailAlreadyInUse ─────────────────────────────────────────────────

    @Test
    fun `EMAIL_EXISTS maps to EmailAlreadyInUse`() {
        assertTrue(mapFirebaseErrorMessage("EMAIL_EXISTS") is AuthError.EmailAlreadyInUse)
    }

    @Test
    fun `email-already-in-use maps to EmailAlreadyInUse`() {
        assertTrue(mapFirebaseErrorMessage("email-already-in-use") is AuthError.EmailAlreadyInUse)
    }

    // ── WeakPassword ──────────────────────────────────────────────────────

    @Test
    fun `WEAK_PASSWORD maps to WeakPassword`() {
        assertTrue(mapFirebaseErrorMessage("WEAK_PASSWORD") is AuthError.WeakPassword)
    }

    // ── InvalidEmail ──────────────────────────────────────────────────────

    @Test
    fun `INVALID_EMAIL maps to InvalidEmail`() {
        assertTrue(mapFirebaseErrorMessage("INVALID_EMAIL") is AuthError.InvalidEmail)
    }

    @Test
    fun `invalid-email maps to InvalidEmail`() {
        assertTrue(mapFirebaseErrorMessage("invalid-email") is AuthError.InvalidEmail)
    }

    // ── InvalidResetCode ──────────────────────────────────────────────────

    @Test
    fun `EXPIRED_OOB_CODE maps to InvalidResetCode`() {
        assertTrue(mapFirebaseErrorMessage("EXPIRED_OOB_CODE") is AuthError.InvalidResetCode)
    }

    @Test
    fun `INVALID_OOB_CODE maps to InvalidResetCode`() {
        assertTrue(mapFirebaseErrorMessage("INVALID_OOB_CODE") is AuthError.InvalidResetCode)
    }

    @Test
    fun `expired-action-code maps to InvalidResetCode`() {
        assertTrue(mapFirebaseErrorMessage("expired-action-code") is AuthError.InvalidResetCode)
    }

    // ── TooManyRequests ───────────────────────────────────────────────────

    @Test
    fun `TOO_MANY_ATTEMPTS maps to TooManyRequests`() {
        assertTrue(mapFirebaseErrorMessage("TOO_MANY_ATTEMPTS") is AuthError.TooManyRequests)
    }

    @Test
    fun `too-many-requests maps to TooManyRequests`() {
        assertTrue(mapFirebaseErrorMessage("too-many-requests") is AuthError.TooManyRequests)
    }

    // ── UserDisabled ──────────────────────────────────────────────────────

    @Test
    fun `USER_DISABLED maps to UserDisabled`() {
        assertTrue(mapFirebaseErrorMessage("USER_DISABLED") is AuthError.UserDisabled)
    }

    @Test
    fun `user-disabled maps to UserDisabled`() {
        assertTrue(mapFirebaseErrorMessage("user-disabled") is AuthError.UserDisabled)
    }

    // ── OperationNotAllowed ───────────────────────────────────────────────

    @Test
    fun `OPERATION_NOT_ALLOWED maps to OperationNotAllowed`() {
        assertTrue(mapFirebaseErrorMessage("OPERATION_NOT_ALLOWED") is AuthError.OperationNotAllowed)
    }

    @Test
    fun `operation-not-allowed maps to OperationNotAllowed`() {
        assertTrue(mapFirebaseErrorMessage("operation-not-allowed") is AuthError.OperationNotAllowed)
    }

    // ── NetworkError ──────────────────────────────────────────────────────

    @Test
    fun `NETWORK_ERROR maps to NetworkError`() {
        assertTrue(mapFirebaseErrorMessage("NETWORK_ERROR") is AuthError.NetworkError)
    }

    @Test
    fun `network-request-failed maps to NetworkError`() {
        assertTrue(mapFirebaseErrorMessage("network-request-failed") is AuthError.NetworkError)
    }

    // ── Unknown ───────────────────────────────────────────────────────────

    @Test
    fun `unrecognized message maps to Unknown`() {
        assertTrue(mapFirebaseErrorMessage("some completely unknown firebase error") is AuthError.Unknown)
    }

    @Test
    fun `empty message maps to Unknown`() {
        assertTrue(mapFirebaseErrorMessage("") is AuthError.Unknown)
    }

    // ── Case insensitivity ────────────────────────────────────────────────

    @Test
    fun `matching is case-insensitive`() {
        assertTrue(mapFirebaseErrorMessage("invalid_email") is AuthError.InvalidEmail)
        assertTrue(mapFirebaseErrorMessage("Invalid_Email") is AuthError.InvalidEmail)
        assertTrue(mapFirebaseErrorMessage("INVALID_EMAIL") is AuthError.InvalidEmail)
    }
}
