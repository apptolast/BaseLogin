package com.apptolast.customlogin.presentation.screens.login

import com.apptolast.customlogin.domain.model.IdentityProvider

/**
 * Defines all possible user actions that can be triggered from the Login screen.
 * This sealed class is part of the MVI pattern, ensuring a unidirectional data flow.
 */
sealed class LoginAction {
    data class EmailChanged(val email: String) : LoginAction()
    data class PasswordChanged(val password: String) : LoginAction()
    data object SignInClicked : LoginAction()
    data class SocialSignInClicked(val provider: IdentityProvider) : LoginAction()
}
