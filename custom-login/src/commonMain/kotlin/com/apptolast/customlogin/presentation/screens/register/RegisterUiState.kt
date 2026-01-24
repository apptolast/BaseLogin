package com.apptolast.customlogin.presentation.screens.register

/**
 * Represents the state of the Register screen.
 *
 * @property fullName The full name entered by the user.
 * @property email The email address entered by the user.
 * @property password The password entered by the user.
 * @property confirmPassword The confirmation password entered by the user.
 * @property termsAccepted A boolean indicating if the user has accepted the terms.
 * @property fullNameError An optional error message for the full name field.
 * @property emailError An optional error message for the email field.
 * @property passwordError An optional error message for the password field.
 * @property confirmPasswordError An optional error message for the confirm password field.
 * @property isLoading Indicates if a registration operation is in progress.
 */
data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
)
