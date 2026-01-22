package com.apptolast.customlogin.presentation.screens.forgotpassword
/**
 * One-time side effects to be handled by the UI.
 */
sealed interface ForgotPasswordEffect {
    data object ResetEmailSent : ForgotPasswordEffect
    data class ShowError(val message: String) : ForgotPasswordEffect
}