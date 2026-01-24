package com.apptolast.customlogin.presentation.screens.forgotpassword

/**
 * Defines the contract for UI events and one-time side effects in the Forgot Password screen.
 */

/**
 * User actions from the UI.
 */
sealed interface ForgotPasswordAction {
    data class EmailChanged(val email: String) : ForgotPasswordAction
    data object SendResetEmailClicked : ForgotPasswordAction
}
