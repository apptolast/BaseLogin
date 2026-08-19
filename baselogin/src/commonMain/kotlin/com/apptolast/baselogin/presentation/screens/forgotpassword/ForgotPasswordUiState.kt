package com.apptolast.baselogin.presentation.screens.forgotpassword

import com.apptolast.baselogin.util.ValidationError

/**
 * Represents the state of the Forgot Password screen.
 *
 * @property email The email address entered by the user.
 * @property emailError An optional typed validation error for the email field.
 * @property isLoading Indicates if a request is in progress.
 * @property isSuccess True after the reset email has been sent successfully.
 */
data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: ValidationError? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
)
