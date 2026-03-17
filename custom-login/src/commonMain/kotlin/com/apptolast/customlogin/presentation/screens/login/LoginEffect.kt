package com.apptolast.customlogin.presentation.screens.login

import com.apptolast.customlogin.domain.model.AuthError

/**
 * One-time side effects to be handled by the UI.
 */
sealed interface LoginEffect {
    data object NavigateToHome : LoginEffect
    data object NavigateToPhoneAuth : LoginEffect
    data object NavigateToMagicLink : LoginEffect
    data class ShowError(val error: AuthError) : LoginEffect
}