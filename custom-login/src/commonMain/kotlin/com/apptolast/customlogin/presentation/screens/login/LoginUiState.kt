package com.apptolast.customlogin.presentation.screens.login

import com.apptolast.customlogin.domain.model.LoginConfig
import com.apptolast.customlogin.domain.model.User

/**
 * Represents the state of the Login screen.
 */
data class LoginUiState(
    val config: LoginConfig = LoginConfig(),
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)