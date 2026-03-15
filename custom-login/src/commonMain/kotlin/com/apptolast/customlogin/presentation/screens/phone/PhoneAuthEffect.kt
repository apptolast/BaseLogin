package com.apptolast.customlogin.presentation.screens.phone

sealed interface PhoneAuthEffect {
    data object NavigateToHome : PhoneAuthEffect
    data class ShowError(val message: String) : PhoneAuthEffect
}
