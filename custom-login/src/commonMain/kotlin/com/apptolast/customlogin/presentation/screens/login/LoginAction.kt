package com.apptolast.customlogin.presentation.screens.login

import com.apptolast.customlogin.domain.model.IdentityProvider

/**
 * Defines all possible user actions that can be triggered from the Login screen.
 * This sealed interface is part of the MVI pattern, ensuring a unidirectional data flow.
 */
sealed interface LoginAction {
    data class EmailChanged(val email: String) : LoginAction
    data class PasswordChanged(val password: String) : LoginAction
    data class SignIn(val provider: IdentityProvider) : LoginAction
}
