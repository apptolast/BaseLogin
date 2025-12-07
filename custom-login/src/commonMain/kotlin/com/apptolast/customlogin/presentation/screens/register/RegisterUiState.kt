// customlogin/src/androidMain/.../presentation/screens/register/RegisterUiState.kt
package com.apptolast.customlogin.presentation.screens.register

import com.apptolast.customlogin.domain.model.LoginConfig
import com.apptolast.customlogin.domain.model.User

data class RegisterUiState(
    val config: LoginConfig = LoginConfig(),
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val validationErrors: Map<Field, String> = emptyMap()
) {
    enum class Field { FULL_NAME, EMAIL, PASSWORD, CONFIRM_PASSWORD }
}
