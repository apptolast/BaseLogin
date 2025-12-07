package com.apptolast.customlogin.presentation.screens.login

import com.apptolast.customlogin.domain.model.LoginConfig
import com.apptolast.customlogin.domain.model.UserSession

/**
 * Represents the state of the Login screen.
 */
data class LoginUiState(
    val config: LoginConfig = LoginConfig(),
    val isLoading: Boolean = false,
    val user: UserSession? = null,
    val errorMessage: String? = null,
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null
) {
    val isValid: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && emailError == null && passwordError == null
}