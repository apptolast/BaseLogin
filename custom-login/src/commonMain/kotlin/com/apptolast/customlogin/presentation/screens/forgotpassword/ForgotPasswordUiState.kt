package com.apptolast.customlogin.presentation.screens.forgotpassword

import com.apptolast.customlogin.domain.model.LoginConfig

/**
 * UI state for the Forgot Password screen.
 */
data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val isEmailSent: Boolean = false,
    val config: LoginConfig = LoginConfig()
) {
    val isValid: Boolean
        get() = email.isNotBlank() && emailError == null
}
