package com.apptolast.customlogin.presentation.screens.login

import com.apptolast.customlogin.domain.model.IdentityProvider

/**
 * Represents the state of the Login screen.
 *
 * @property email The email address entered by the user.
 * @property password The password entered by the user.
 * @property emailError An optional error message for the email field.
 * @property passwordError An optional error message for the password field.
 * @property loadingState Indicates the current loading state of the login process.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val loadingState: LoginLoadingState = LoginLoadingState.Idle
)
