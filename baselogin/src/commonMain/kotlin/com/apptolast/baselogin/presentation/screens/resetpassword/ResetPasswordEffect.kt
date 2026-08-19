package com.apptolast.baselogin.presentation.screens.resetpassword

import com.apptolast.baselogin.domain.model.AuthError

/**
 * One-time side effects to be handled by the UI.
 */
sealed interface ResetPasswordEffect {
    data object NavigateToLogin : ResetPasswordEffect
    data class ShowError(val error: AuthError) : ResetPasswordEffect
}
