package com.apptolast.baselogin.presentation.screens.reauth

import com.apptolast.baselogin.domain.model.IdentityProvider

sealed interface ReauthAction {
    data class EmailChanged(val email: String) : ReauthAction
    data class PasswordChanged(val password: String) : ReauthAction
    data object SubmitEmailPassword : ReauthAction
    data class SubmitOAuth(val provider: IdentityProvider) : ReauthAction
}
