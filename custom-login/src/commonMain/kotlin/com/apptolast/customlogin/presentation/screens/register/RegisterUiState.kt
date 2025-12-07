package com.apptolast.customlogin.presentation.screens.register

import com.apptolast.customlogin.domain.model.LoginConfig
import com.apptolast.customlogin.domain.model.UserSession

data class RegisterUiState(
    val config: LoginConfig = LoginConfig(),
    val isLoading: Boolean = false,
    val user: UserSession? = null,
    val errorMessage: String? = null,
    val validationErrors: Map<Field, String> = emptyMap()
) {
    enum class Field { FULL_NAME, EMAIL, PASSWORD, CONFIRM_PASSWORD }
}
