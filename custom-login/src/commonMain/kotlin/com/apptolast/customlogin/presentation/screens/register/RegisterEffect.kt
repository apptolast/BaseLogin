package com.apptolast.customlogin.presentation.screens.register
/**
 * One-time side effects to be handled by the UI.
 */
sealed interface RegisterEffect {
    data object NavigateToHome : RegisterEffect
    data class ShowError(val message: String) : RegisterEffect
}