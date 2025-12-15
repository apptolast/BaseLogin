package com.apptolast.customlogin.presentation.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Register screen.
 * Handles business logic and exposes state to the UI.
 */
class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onFullNameChange(name: String) {
        _uiState.update { it.copy(fullName = name, fullNameError = null, errorMessage = null) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, errorMessage = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null, errorMessage = null) }
    }

    fun onTermsAcceptedChange(accepted: Boolean) {
        _uiState.update { it.copy(termsAccepted = accepted, errorMessage = null) }
    }

    fun createUserWithEmail() = with(_uiState) {

        if (!validate(value)) return@with

        viewModelScope.launch {
            update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.createUserWithEmail(value.email, value.password, value.fullName)) {
                is AuthResult.Success -> {
                    update { it.copy(isLoading = false, user = result.session) }
                }
                is AuthResult.Failure -> {
                    update { it.copy(isLoading = false, errorMessage = result.error.message) }
                }
                is AuthResult.RequiresEmailVerification -> {
                    update { it.copy(isLoading = false, errorMessage = "Please check your email to verify your account") }
                }
                else -> {
                    update { it.copy(isLoading = false, errorMessage = "Registration failed") }
                }
            }
        }
    }

    private fun validate(state: RegisterUiState): Boolean {
        val fullNameError = when {
            state.fullName.isBlank() -> "Full name is required"
            else -> null
        }

        val emailError = when {
            state.email.isBlank() -> "Email cannot be empty"
            !"^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex().matches(state.email) -> "Invalid email format"
            else -> null
        }

        val passwordError = when {
            state.password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }

        val confirmPasswordError = when {
            state.confirmPassword.isBlank() -> "Please confirm your password"
            state.password != state.confirmPassword -> "Passwords do not match"
            else -> null
        }

        _uiState.update {
            it.copy(
                fullNameError = fullNameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
        }

        return fullNameError == null && emailError == null && passwordError == null && confirmPasswordError == null
    }
}
