package com.apptolast.customlogin.domain.model

/**
 * Represents the result of initiating a phone OTP verification flow.
 * This is separate from [AuthResult] because phone auth is a two-step process.
 */
sealed interface PhoneAuthResult {

    /**
     * The OTP code was sent to the phone number.
     * @property verificationId Must be passed back when calling [AuthRepository.verifyPhoneOtp].
     */
    data class CodeSent(val verificationId: String) : PhoneAuthResult

    /**
     * Android only: The user was automatically signed in without needing to enter a code
     * (e.g., SIM-based instant verification).
     * @property session The authenticated user's session data.
     */
    data class AutoSignedIn(val session: UserSession) : PhoneAuthResult

    /**
     * The verification request failed.
     * @property error The reason for the failure.
     */
    data class Failure(val error: AuthError) : PhoneAuthResult
}
