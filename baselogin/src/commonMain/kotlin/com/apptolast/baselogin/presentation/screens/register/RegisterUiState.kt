package com.apptolast.baselogin.presentation.screens.register

import com.apptolast.baselogin.domain.model.IdentityProvider
import com.apptolast.baselogin.util.ValidationError

/**
 * Represents the state of the Register screen.
 *
 * @property fullName The full name entered by the user.
 * @property email The email address entered by the user.
 * @property password The password entered by the user.
 * @property confirmPassword The confirmation password entered by the user.
 * @property termsAccepted A boolean indicating if the user has accepted the terms.
 * @property fullNameError An optional typed validation error for the full name field.
 * @property emailError An optional typed validation error for the email field.
 * @property passwordError An optional typed validation error for the password field.
 * @property confirmPasswordError An optional typed validation error for the confirm password field.
 * @property isLoading Indicates if a registration operation is in progress.
 */
data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val fullNameError: ValidationError? = null,
    val emailError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val confirmPasswordError: ValidationError? = null,
    val isLoading: Boolean = false,
    val availableProviders: List<IdentityProvider> = emptyList(),
    val loadingProvider: IdentityProvider? = null,
)
