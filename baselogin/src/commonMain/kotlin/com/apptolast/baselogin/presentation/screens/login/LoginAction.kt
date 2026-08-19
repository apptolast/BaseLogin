package com.apptolast.baselogin.presentation.screens.login

import com.apptolast.baselogin.domain.model.IdentityProvider

/**
 * Defines the contract for UI events and one-time side effects in the Login screen.
 */

/**
 * User actions from the UI.
 */
sealed interface LoginAction {
    data class EmailChanged(val email: String) : LoginAction
    data class PasswordChanged(val password: String) : LoginAction
    data class SocialSignInClicked(val provider: IdentityProvider) : LoginAction
    data object SignInClicked : LoginAction
}
