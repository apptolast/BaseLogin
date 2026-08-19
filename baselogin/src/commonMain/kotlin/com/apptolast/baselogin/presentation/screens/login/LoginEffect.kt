package com.apptolast.baselogin.presentation.screens.login

import com.apptolast.baselogin.domain.model.AuthError

/**
 * One-time side effects to be handled by the UI.
 */
sealed interface LoginEffect {
    data object NavigateToHome : LoginEffect
    data object NavigateToPhoneAuth : LoginEffect
    data object NavigateToMagicLink : LoginEffect
    data class ShowError(val error: AuthError) : LoginEffect
}
