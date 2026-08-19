package com.apptolast.baselogin.presentation.screens.reauth

import com.apptolast.baselogin.domain.model.AuthError

sealed class ReauthEffect {
    data object Success : ReauthEffect()
    data class ShowError(val error: AuthError) : ReauthEffect()
}
