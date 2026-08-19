package com.apptolast.baselogin.presentation.screens.forgotpassword

import com.apptolast.baselogin.domain.model.AuthError

/**
 * One-time side effects to be handled by the UI.
 */
sealed interface ForgotPasswordEffect {
    data class ShowError(val error: AuthError) : ForgotPasswordEffect
    data object NavigateBack : ForgotPasswordEffect
}
