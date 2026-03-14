package com.apptolast.customlogin.domain.model

/**
 * Represents the result of a phone number verification flow.
 */
sealed interface PhoneAuthResult {
    /** Indicates that the verification code has been sent successfully. */
    data class CodeSent(val verificationId: String) : PhoneAuthResult

    /** Indicates that the phone number has been verified and sign-in is complete. */
    data class VerificationCompleted(val session: UserSession) : PhoneAuthResult

    /** Indicates that the verification flow failed. */
    data class Failure(val error: AuthError) : PhoneAuthResult
}
