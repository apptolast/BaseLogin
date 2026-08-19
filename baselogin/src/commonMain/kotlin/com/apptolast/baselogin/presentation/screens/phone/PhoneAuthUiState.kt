package com.apptolast.baselogin.presentation.screens.phone

import com.apptolast.baselogin.util.ValidationError

/**
 * Represents the UI state of the Phone Authentication screen.
 *
 * The screen has two logical steps:
 * - Step 1: [verificationId] is null → user enters phone number.
 * - Step 2: [verificationId] is not null → user enters the OTP code.
 */
data class PhoneAuthUiState(
    val countryCode: String = "+1",
    val phoneNumber: String = "",
    val phoneError: ValidationError? = null,
    val otpCode: String = "",
    val otpError: ValidationError? = null,
    val isLoading: Boolean = false,
    /** Set after [sendPhoneOtp] succeeds. Transitions the screen to the OTP entry step. */
    val verificationId: String? = null,
)
