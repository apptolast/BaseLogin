package com.apptolast.customlogin.presentation.screens.resetpassword

/**
 * Represents the state of the Reset Password screen.
 *
 * @property resetCode The code received from the password reset link.
 * @property newPassword The new password entered by the user.
 * @property confirmPassword The confirmation password entered by the user.
 * @property passwordError An optional error message for the new password field.
 * @property confirmPasswordError An optional error message for the confirm password field.
 * @property isLoading Indicates if a request is in progress.
 */
data class ResetPasswordUiState(
    val resetCode: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false
)
