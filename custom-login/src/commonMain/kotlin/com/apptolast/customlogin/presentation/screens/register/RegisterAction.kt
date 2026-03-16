package com.apptolast.customlogin.presentation.screens.register

import com.apptolast.customlogin.domain.model.IdentityProvider

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
    data class SignUpWithOAuth(val provider: IdentityProvider) : RegisterAction
}
