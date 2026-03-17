package com.apptolast.customlogin.presentation.screens.reauth

import com.apptolast.customlogin.domain.model.AuthError

sealed class ReauthEffect {
    data object Success : ReauthEffect()
    data class ShowError(val error: AuthError) : ReauthEffect()
}
