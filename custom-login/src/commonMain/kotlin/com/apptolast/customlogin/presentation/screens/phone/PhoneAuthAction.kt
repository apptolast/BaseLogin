package com.apptolast.customlogin.presentation.screens.phone

sealed interface PhoneAuthAction {
    data class CountryCodeChanged(val dialCode: String) : PhoneAuthAction
    data class PhoneNumberChanged(val phoneNumber: String) : PhoneAuthAction
    data class OtpCodeChanged(val code: String) : PhoneAuthAction
    data object SendCodeClicked : PhoneAuthAction
    data object VerifyCodeClicked : PhoneAuthAction
}
