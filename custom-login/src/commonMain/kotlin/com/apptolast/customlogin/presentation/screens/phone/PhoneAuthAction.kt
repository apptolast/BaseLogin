package com.apptolast.customlogin.presentation.screens.phone

/**
 * Defines the user actions for the PhoneAuth screen.
 */
sealed interface PhoneAuthAction {
    data class PhoneNumberChanged(val phoneNumber: String) : PhoneAuthAction
    data class VerificationCodeChanged(val code: String) : PhoneAuthAction
    data object SendVerificationCode : PhoneAuthAction
    data object SignInWithCode : PhoneAuthAction
}
