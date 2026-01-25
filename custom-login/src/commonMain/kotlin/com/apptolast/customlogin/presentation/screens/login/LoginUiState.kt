package com.apptolast.customlogin.presentation.screens.login

/**
 * Represents the state of the Login screen.
 *
 * @property email The email address entered by the user.
 * @property password The password entered by the user.
 * @property emailError An optional error message for the email field.
 * @property passwordError An optional error message for the password field.
 * @property loadingProvider Indicates which login operation is in progress.
 * Can be "email", a social provider id, or null if no operation is active.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val loadingProvider: String? = null
)
