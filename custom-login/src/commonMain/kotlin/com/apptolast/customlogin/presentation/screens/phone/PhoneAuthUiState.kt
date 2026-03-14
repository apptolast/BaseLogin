package com.apptolast.customlogin.presentation.screens.phone

/**
 * Represents the state of the PhoneAuth screen.
 */
data class PhoneAuthUiState(
    val phoneNumber: String = "",
    val verificationCode: String = "",
    val error: String? = null,
    val isLoading: Boolean = false,
    val isCodeSent: Boolean = false
)
