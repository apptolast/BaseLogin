package com.apptolast.customlogin.presentation.screens.forgotpassword

/**
 * Represents the state of the Forgot Password screen.
 *
 * @property email The email address entered by the user.
 * @property emailError An optional error message for the email field.
 * @property isLoading Indicates if a request is in progress.
 * @property isSuccessPasswordResetSent Indicates if a password reset email was sent successfully.
 * @property errorMessage A general error message to display.
 */
data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isSuccessPasswordResetSent: Boolean = false,
    val errorMessage: String? = null
)
