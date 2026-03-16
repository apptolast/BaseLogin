package com.apptolast.customlogin.presentation.screens.reauth

import com.apptolast.customlogin.domain.model.IdentityProvider

sealed interface ReauthAction {
    data class EmailChanged(val email: String) : ReauthAction
    data class PasswordChanged(val password: String) : ReauthAction
    data object SubmitEmailPassword : ReauthAction
    data class SubmitOAuth(val provider: IdentityProvider) : ReauthAction
}
