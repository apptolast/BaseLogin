package com.apptolast.customlogin.presentation.screens.resetpassword

/**
 * UI state for the Reset Password screen.
 */
data class ResetPasswordUiState(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isPasswordReset: Boolean = false,
    val resetCode: String = ""
) {
    val isValid: Boolean
        get() = newPassword.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                passwordError == null &&
                confirmPasswordError == null &&
                newPassword == confirmPassword

    val passwordsMatch: Boolean
        get() = newPassword == confirmPassword
}
