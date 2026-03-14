package com.apptolast.customlogin.presentation.screens.phone

/**
 * Defines the one-time side effects for the PhoneAuth screen.
 */
sealed interface PhoneAuthEffect {
    data object NavigateToHome : PhoneAuthEffect
    data class ShowError(val message: String) : PhoneAuthEffect
}
