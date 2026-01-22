package com.apptolast.customlogin.presentation.screens.login
/**
 * One-time side effects to be handled by the UI.
 */
sealed interface LoginEffect {
    data object NavigateToHome : LoginEffect
    data class ShowError(val message: String) : LoginEffect
}