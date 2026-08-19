package com.apptolast.baselogin.presentation.screens.resetpassword

import com.apptolast.baselogin.util.ValidationError

/**
 * Represents the state of the Reset Password screen.
 *
 * @property resetCode The code received from the password reset link.
 * @property newPassword The new password entered by the user.
 * @property confirmPassword The confirmation password entered by the user.
 * @property passwordError An optional typed validation error for the new password field.
 * @property confirmPasswordError An optional typed validation error for the confirm password field.
 * @property isLoading Indicates if a request is in progress.
 * @property isSuccess True after the password has been successfully reset.
 */
data class ResetPasswordUiState(
    val resetCode: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val passwordError: ValidationError? = null,
    val confirmPasswordError: ValidationError? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
)
