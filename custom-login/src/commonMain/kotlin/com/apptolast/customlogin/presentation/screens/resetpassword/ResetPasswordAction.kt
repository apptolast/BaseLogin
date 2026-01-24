package com.apptolast.customlogin.presentation.screens.resetpassword

/**
 * Defines the contract for UI events and one-time side effects in the Reset Password screen.
 */

/**
 * User actions from the UI.
 */
sealed interface ResetPasswordAction {
    data class NewPasswordChanged(val password: String) : ResetPasswordAction
    data class ConfirmPasswordChanged(val confirmPassword: String) : ResetPasswordAction
    data object ResetPasswordClicked : ResetPasswordAction
}
