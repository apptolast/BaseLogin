package com.apptolast.customlogin.presentation.screens.register

/**
 * Defines the contract for UI events and one-time side effects in the Register screen.
 */

/**
 * User actions from the UI.
 */
sealed interface RegisterAction {
    data class FullNameChanged(val name: String) : RegisterAction
    data class EmailChanged(val email: String) : RegisterAction
    data class PasswordChanged(val password: String) : RegisterAction
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterAction
    data class TermsAcceptedChanged(val accepted: Boolean) : RegisterAction
    data object SignUpClicked : RegisterAction
    data object ErrorMessageDismissed : RegisterAction
}
