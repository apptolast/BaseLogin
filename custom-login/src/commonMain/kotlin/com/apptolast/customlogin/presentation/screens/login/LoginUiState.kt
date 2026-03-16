package com.apptolast.customlogin.presentation.screens.login

import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.util.ValidationError

/**
 * Represents the state of the Login screen.
 *
 * @property email The email address entered by the user.
 * @property password The password entered by the user.
 * @property emailError An optional typed validation error for the email field.
 * @property passwordError An optional typed validation error for the password field.
 * @property isLoading Indicates if the email/password sign-in operation is in progress.
 * @property loadingProvider The social provider currently processing a sign-in, or null if none.
 * @property availableProviders The list of social/OAuth providers available for sign-in.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val isLoading: Boolean = false,
    val loadingProvider: IdentityProvider? = null,
    val availableProviders: List<IdentityProvider> = emptyList(),
)
