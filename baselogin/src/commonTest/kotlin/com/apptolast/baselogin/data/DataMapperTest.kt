package com.apptolast.baselogin.data

import com.apptolast.baselogin.domain.model.AuthError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
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
            mapFirebaseErrorMessage("The password is invalid. [wrong-password]") is AuthError.InvalidCredentials,
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

    // ── PhoneNumberInvalid ────────────────────────────────────────────────

    @Test
    fun `INVALID_PHONE_NUMBER maps to PhoneNumberInvalid`() {
        assertTrue(mapFirebaseErrorMessage("INVALID_PHONE_NUMBER") is AuthError.PhoneNumberInvalid)
    }

    @Test
    fun `invalid-phone-number maps to PhoneNumberInvalid`() {
        assertTrue(mapFirebaseErrorMessage("invalid-phone-number") is AuthError.PhoneNumberInvalid)
    }

    @Test
    fun `ERROR_INVALID_PHONE_NUMBER maps to PhoneNumberInvalid`() {
        assertTrue(mapFirebaseErrorMessage("ERROR_INVALID_PHONE_NUMBER") is AuthError.PhoneNumberInvalid)
    }

    // ── InvalidVerificationCode ───────────────────────────────────────────

    @Test
    fun `INVALID_VERIFICATION_CODE maps to InvalidVerificationCode`() {
        assertTrue(mapFirebaseErrorMessage("INVALID_VERIFICATION_CODE") is AuthError.InvalidVerificationCode)
    }

    @Test
    fun `invalid-verification-code maps to InvalidVerificationCode`() {
        assertTrue(mapFirebaseErrorMessage("invalid-verification-code") is AuthError.InvalidVerificationCode)
    }

    @Test
    fun `ERROR_INVALID_VERIFICATION_CODE maps to InvalidVerificationCode`() {
        assertTrue(mapFirebaseErrorMessage("ERROR_INVALID_VERIFICATION_CODE") is AuthError.InvalidVerificationCode)
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

    // ── Native SDK ERROR_* variants not yet covered ───────────────────────

    @Test
    fun `invalid-action-code maps to InvalidResetCode`() {
        assertTrue(mapFirebaseErrorMessage("invalid-action-code") is AuthError.InvalidResetCode)
    }

    @Test
    fun `ERROR_USER_NOT_FOUND maps to UserNotFound`() {
        assertTrue(mapFirebaseErrorMessage("ERROR_USER_NOT_FOUND") is AuthError.UserNotFound)
    }

    @Test
    fun `ERROR_EMAIL_ALREADY_IN_USE maps to EmailAlreadyInUse`() {
        assertTrue(mapFirebaseErrorMessage("ERROR_EMAIL_ALREADY_IN_USE") is AuthError.EmailAlreadyInUse)
    }

    @Test
    fun `weak-password maps to WeakPassword`() {
        assertTrue(mapFirebaseErrorMessage("weak-password") is AuthError.WeakPassword)
    }

    @Test
    fun `ERROR_WEAK_PASSWORD maps to WeakPassword`() {
        assertTrue(mapFirebaseErrorMessage("ERROR_WEAK_PASSWORD") is AuthError.WeakPassword)
    }

    @Test
    fun `ERROR_INVALID_EMAIL maps to InvalidEmail`() {
        assertTrue(mapFirebaseErrorMessage("ERROR_INVALID_EMAIL") is AuthError.InvalidEmail)
    }

    @Test
    fun `ERROR_TOO_MANY_REQUESTS maps to TooManyRequests`() {
        assertTrue(mapFirebaseErrorMessage("ERROR_TOO_MANY_REQUESTS") is AuthError.TooManyRequests)
    }

    @Test
    fun `ERROR_USER_DISABLED maps to UserDisabled`() {
        assertTrue(mapFirebaseErrorMessage("ERROR_USER_DISABLED") is AuthError.UserDisabled)
    }

    @Test
    fun `ERROR_OPERATION_NOT_ALLOWED maps to OperationNotAllowed`() {
        assertTrue(mapFirebaseErrorMessage("ERROR_OPERATION_NOT_ALLOWED") is AuthError.OperationNotAllowed)
    }

    @Test
    fun `ERROR_NETWORK_REQUEST_FAILED maps to NetworkError`() {
        assertTrue(mapFirebaseErrorMessage("ERROR_NETWORK_REQUEST_FAILED") is AuthError.NetworkError)
    }

    // ── NetworkError preserves original message ────────────────────────────

    @Test
    fun `NetworkError preserves the original error message`() {
        val msg = "NETWORK_ERROR: connection timed out"
        val error = mapFirebaseErrorMessage(msg)
        assertTrue(error is AuthError.NetworkError)
        assertTrue(error.message.contains(msg))
    }

    // ── Error code embedded in longer message ──────────────────────────────

    @Test
    fun `error code embedded in longer message still maps correctly`() {
        assertTrue(
            mapFirebaseErrorMessage("Firebase: There is no user record [user-not-found]") is AuthError.UserNotFound,
        )
        assertTrue(
            mapFirebaseErrorMessage(
                "The email address is already in use [email-already-in-use].",
            ) is AuthError.EmailAlreadyInUse,
        )
    }

    // ══ Spec 013: mapFirebaseError (by error code) ═════════════════════════

    private fun assertMapsTo(expected: String, code: String?, message: String = "") {
        val error = mapFirebaseError(code, message)
        assertEquals(
            expected,
            error::class.simpleName,
            "code=$code message=\"$message\" mapped to ${error::class.simpleName}",
        )
    }

    // ── AC-01 ──────────────────────────────────────────────────────────────

    @Test
    fun `013 android codes are classified even when the message lacks them`() {
        // Given: native Android codes paired with the human message the Android SDK really sends
        val rows = listOf(
            Triple(
                "ERROR_EMAIL_ALREADY_IN_USE",
                "The email address is already in use by another account.",
                "EmailAlreadyInUse",
            ),
            Triple("ERROR_INVALID_EMAIL", "The email address is badly formatted.", "InvalidEmail"),
            Triple(
                "ERROR_WEAK_PASSWORD",
                "The given password is invalid. [ Password should be at least 6 characters ]",
                "WeakPassword",
            ),
            Triple(
                "ERROR_USER_NOT_FOUND",
                "There is no user record corresponding to this identifier. The user may have been deleted.",
                "UserNotFound",
            ),
            Triple(
                "ERROR_USER_DISABLED",
                "The user account has been disabled by an administrator.",
                "UserDisabled",
            ),
            Triple(
                "ERROR_INVALID_CREDENTIAL",
                "The supplied auth credential is incorrect, malformed or has expired.",
                "InvalidCredentials",
            ),
            Triple(
                "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL",
                "An account already exists with the same email address but different sign-in credentials.",
                "AccountExistsWithDifferentCredential",
            ),
            Triple(
                "ERROR_CREDENTIAL_ALREADY_IN_USE",
                "This credential is already associated with a different user account.",
                "CredentialAlreadyInUse",
            ),
            Triple(
                "ERROR_REQUIRES_RECENT_LOGIN",
                "This operation is sensitive and requires recent authentication. Log in again before retrying.",
                "RequiresRecentLogin",
            ),
            Triple(
                "ERROR_SESSION_EXPIRED",
                "The sms code has expired. Please re-send the verification code to try again.",
                "VerificationCodeExpired",
            ),
            Triple(
                "ERROR_QUOTA_EXCEEDED",
                "The sms quota for this project has been exceeded.",
                "QuotaExceeded",
            ),
            Triple(
                "ERROR_WEB_CONTEXT_CANCELED",
                "The web operation was canceled by the user.",
                "SignInCancelled",
            ),
        )

        // When / Then
        rows.forEach { (code, message, expected) -> assertMapsTo(expected, code, message) }
    }

    // ── AC-02 ──────────────────────────────────────────────────────────────

    @Test
    fun `013 ios numeric codes are classified`() {
        // Given: GitLive builds the iOS FirebaseAuthException code as FIRAuthErrorCode.toString()
        val rows = listOf(
            "17004" to "InvalidCredentials",
            "17009" to "InvalidCredentials",
            "17011" to "UserNotFound",
            "17007" to "EmailAlreadyInUse",
            "17026" to "WeakPassword",
            "17008" to "InvalidEmail",
            "17010" to "TooManyRequests",
            "17005" to "UserDisabled",
            "17006" to "OperationNotAllowed",
            "17020" to "NetworkError",
            "17042" to "PhoneNumberInvalid",
            "17044" to "InvalidVerificationCode",
            "17029" to "InvalidResetCode",
            "17030" to "InvalidResetCode",
            "17012" to "AccountExistsWithDifferentCredential",
            "17025" to "CredentialAlreadyInUse",
            "17014" to "RequiresRecentLogin",
            "17051" to "VerificationCodeExpired",
            "17052" to "QuotaExceeded",
            "17058" to "SignInCancelled",
        )

        // When / Then: the message carries nothing, so only the code can classify
        rows.forEach { (code, expected) -> assertMapsTo(expected, code, message = "An internal error occurred.") }
    }

    // ── AC-03 ──────────────────────────────────────────────────────────────

    @Test
    fun `013 web rest and android spellings of requires recent login normalise to the same type`() {
        // Given
        val codes = listOf(
            "auth/requires-recent-login",
            "requires-recent-login",
            "CREDENTIAL_TOO_OLD_LOGIN_AGAIN",
            "ERROR_REQUIRES_RECENT_LOGIN",
        )

        // When / Then
        codes.forEach { code -> assertMapsTo("RequiresRecentLogin", code) }
    }

    @Test
    fun `013 web rest and android spellings of email already in use normalise to the same type`() {
        // Given
        val codes = listOf("auth/email-already-in-use", "EMAIL_EXISTS", "ERROR_EMAIL_ALREADY_IN_USE")

        // When / Then
        codes.forEach { code -> assertMapsTo("EmailAlreadyInUse", code) }
    }

    // ── AC-04 ──────────────────────────────────────────────────────────────

    @Test
    fun `013 without a recognisable code the message fallback still applies`() {
        // Given
        val message = "The password is invalid. [wrong-password]"

        // When
        val withNullCode = mapFirebaseError(code = null, message = message)
        val withUnknownCode = mapFirebaseError(code = "ERROR_SOMETHING_NEW", message = message)

        // Then: same result as the message-only mapper
        assertIs<AuthError.InvalidCredentials>(withNullCode)
        assertIs<AuthError.InvalidCredentials>(withUnknownCode)
        assertEquals(mapFirebaseErrorMessage(message)::class, withNullCode::class)
    }

    // ── AC-05 ──────────────────────────────────────────────────────────────

    @Test
    fun `013 the code wins over the message`() {
        // Given: a message the substring fallback would classify as NetworkError
        val code = "ERROR_USER_DISABLED"
        val message = "network unreachable"

        // When
        val error = mapFirebaseError(code, message)

        // Then
        assertIs<AuthError.UserDisabled>(error)
    }

    // ── AC-06 ──────────────────────────────────────────────────────────────

    @Test
    fun `013 unknown keeps the original message and cause`() {
        // Given
        val cause = IllegalStateException("root")

        // When
        val error = mapFirebaseError(code = null, message = "boom", cause = cause)

        // Then
        assertIs<AuthError.Unknown>(error)
        assertEquals("boom", error.message)
        assertSame(cause, error.cause)
    }

    // ── AC-07 ──────────────────────────────────────────────────────────────

    @Test
    fun `013 message fallback recognises credential already in use in an ios message`() {
        // Given: the shape of NSError.toString() GitLive uses as the iOS message
        val message = "Error Domain=FIRAuthErrorDomain Code=17025 " +
            "\"This credential is already associated with a different user account.\" " +
            "UserInfo={NSLocalizedDescription=This credential is already associated with a different user account., " +
            "FIRAuthErrorUserInfoNameKey=ERROR_CREDENTIAL_ALREADY_IN_USE}"

        // When
        val error = mapFirebaseErrorMessage(message)

        // Then
        assertIs<AuthError.CredentialAlreadyInUse>(error)
    }

    @Test
    fun `013 message fallback recognises the other new error names`() {
        // Given: iOS messages carrying the FIRAuthErrorUserInfoNameKey of each new variant
        val rows = listOf(
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" to "AccountExistsWithDifferentCredential",
            "ERROR_REQUIRES_RECENT_LOGIN" to "RequiresRecentLogin",
            "ERROR_SESSION_EXPIRED" to "VerificationCodeExpired",
            "ERROR_QUOTA_EXCEEDED" to "QuotaExceeded",
            "ERROR_WEB_CONTEXT_CANCELLED" to "SignInCancelled",
        )

        rows.forEach { (name, expected) ->
            // When
            val error = mapFirebaseErrorMessage(
                "Error Domain=FIRAuthErrorDomain UserInfo={FIRAuthErrorUserInfoNameKey=$name}",
            )

            // Then
            assertEquals(expected, error::class.simpleName, "$name mapped to ${error::class.simpleName}")
        }
    }

    // ── AC-24 ──────────────────────────────────────────────────────────────

    @Test
    fun `013 developer configuration codes map to ProviderNotConfigured`() {
        // Given
        val codes = listOf(
            "ERROR_APP_NOT_AUTHORIZED",
            "17028",
            "ERROR_INVALID_API_KEY",
            "17023",
            "CONFIGURATION_NOT_FOUND",
        )

        // When / Then
        codes.forEach { code ->
            assertMapsTo("ProviderNotConfigured", code, message = "An internal error has occurred.")
        }
    }

    @Test
    fun `013 an internal error caused by a restricted api key maps to ProviderNotConfigured`() {
        // Given: the message Firebase returns when the Android API key restrictions reject the app
        val blocked = "An internal error has occurred. [ Requests from this Android client application " +
            "<empty> are blocked. ]"
        val invalidKey = "An internal error has occurred. [ API key not valid. Please pass a valid API key. ]"

        // When / Then
        assertMapsTo("ProviderNotConfigured", "ERROR_INTERNAL_ERROR", blocked)
        assertMapsTo("ProviderNotConfigured", "ERROR_INTERNAL_ERROR", invalidKey)
    }

    @Test
    fun `013 any other internal error stays Unknown`() {
        // Given
        val message = "An internal error has occurred. [ boom ]"

        // When
        val error = mapFirebaseError("ERROR_INTERNAL_ERROR", message)

        // Then
        assertIs<AuthError.Unknown>(error)
    }
}
