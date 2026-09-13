package com.apptolast.baselogin.presentation.screens.register

import com.apptolast.baselogin.domain.model.AuthError

/**
 * One-time side effects to be handled by the UI.
 */
sealed interface RegisterEffect {
    data object NavigateToHome : RegisterEffect
    data object NavigateToPhoneAuth : RegisterEffect
    data object NavigateToMagicLink : RegisterEffect
    data class ShowError(val error: AuthError) : RegisterEffect

    /** Sign-up succeeded but the account still has to verify its email. Not an error. */
    data object EmailVerificationRequired : RegisterEffect
}
