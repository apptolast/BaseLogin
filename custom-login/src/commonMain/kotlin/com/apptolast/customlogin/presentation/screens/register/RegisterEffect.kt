package com.apptolast.customlogin.presentation.screens.register

import com.apptolast.customlogin.domain.model.AuthError

/**
 * One-time side effects to be handled by the UI.
 */
sealed interface RegisterEffect {
    data object NavigateToHome : RegisterEffect
    data object NavigateToPhoneAuth : RegisterEffect
    data object NavigateToMagicLink : RegisterEffect
    data class ShowError(val error: AuthError) : RegisterEffect
}
