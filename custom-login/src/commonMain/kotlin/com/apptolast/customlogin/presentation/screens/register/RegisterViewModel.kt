package com.apptolast.customlogin.presentation.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.customlogin.domain.AuthRepository
import com.apptolast.customlogin.domain.model.AuthResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Register screen using MVI pattern.
 * Handles business logic and exposes state and effects to the UI.
 */
class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<RegisterEffect>()
    val effect = _effect.asSharedFlow()

    /**
     * The single entry point for all user actions from the UI.
     */
    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.FullNameChanged -> onFullNameChange(action.name)
            is RegisterAction.EmailChanged -> onEmailChange(action.email)
            is RegisterAction.PasswordChanged -> onPasswordChange(action.password)
            is RegisterAction.ConfirmPasswordChanged -> onConfirmPasswordChange(action.confirmPassword)
            is RegisterAction.TermsAcceptedChanged -> onTermsAcceptedChange(action.accepted)
            is RegisterAction.SignUpClicked -> onSignUpClicked()
            is RegisterAction.ErrorMessageDismissed -> { /* No-op, errors are now one-time effects */ }
        }
    }

    private fun onFullNameChange(name: String) {
        _uiState.update { it.copy(fullName = name, fullNameError = null) }
    }

    private fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    private fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    private fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    private fun onTermsAcceptedChange(accepted: Boolean) {
        _uiState.update { it.copy(termsAccepted = accepted) }
    }

    private fun onSignUpClicked() {
        val state = _uiState.value
        val validationErrors = validate(state)

        _uiState.update {
            it.copy(
                fullNameError = validationErrors.fullNameError,
                emailError = validationErrors.emailError,
                passwordError = validationErrors.passwordError,
                confirmPasswordError = validationErrors.confirmPasswordError
            )
        }

        if (validationErrors.isValid) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                when (val result = authRepository.signUp(state.toSignUpData())) {
                    is AuthResult.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(RegisterEffect.NavigateToHome)
                    }
                    is AuthResult.Failure -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(RegisterEffect.ShowError(result.error.message))
                    }
                    is AuthResult.RequiresEmailVerification -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(RegisterEffect.ShowError("Please check your email to verify your account"))
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(RegisterEffect.ShowError("Registration failed"))
                    }
                }
            }
        }
    }

    private fun validate(state: RegisterUiState): ValidationErrors {
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
        return ValidationErrors(
            fullNameError = fullNameError,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError
        )
    }

    private data class ValidationErrors(
        val fullNameError: String?,
        val emailError: String?,
        val passwordError: String?,
        val confirmPasswordError: String?
    ) {
        val isValid: Boolean
            get() = fullNameError == null && emailError == null && passwordError == null && confirmPasswordError == null
    }
}
