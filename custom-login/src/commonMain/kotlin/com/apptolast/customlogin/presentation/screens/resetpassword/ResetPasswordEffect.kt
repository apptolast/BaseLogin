package com.apptolast.customlogin.presentation.screens.resetpassword

/**
 * One-time side effects to be handled by the UI.
 */
sealed interface ResetPasswordEffect {
    data object NavigateToLogin : ResetPasswordEffect
    data class ShowError(val message: String) : ResetPasswordEffect
}
