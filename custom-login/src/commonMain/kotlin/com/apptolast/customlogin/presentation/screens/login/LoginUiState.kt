package com.apptolast.customlogin.presentation.screens.login

import com.apptolast.customlogin.domain.model.IdentityProvider

/**
 * Represents the state of the Login screen.
 *
 * @property email The email address entered by the user.
 * @property password The password entered by the user.
 * @property emailError An optional error message for the email field.
 * @property passwordError An optional error message for the password field.
 * @property isLoading Indicates if a login operation is in progress.
 * @property availableProviders The list of social/OAuth providers available for sign-in.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val availableProviders: List<IdentityProvider> = emptyList(),
)
