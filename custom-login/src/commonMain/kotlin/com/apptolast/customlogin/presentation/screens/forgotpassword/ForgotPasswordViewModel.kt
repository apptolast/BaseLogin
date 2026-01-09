package com.apptolast.customlogin.presentation.screens.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Forgot Password screen.
 * Handles business logic and exposes state to the UI.
 */
class ForgotPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Updates the email value in the UI state.
     */
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    /**
     * Executes the send password reset email flow.
     * It validates the input and calls the repository.
     */
    fun sendPasswordResetEmail() {
        val state = _uiState.value

        if (!validate(state)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.sendPasswordResetEmail(state.email)) {
                is AuthResult.PasswordResetSent -> {
                    _uiState.update { it.copy(isLoading = false, isSuccessPasswordResetSent = true) }
                }
                is AuthResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.error.message) }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "An unexpected error occurred") }
                }
            }
        }
    }

    private fun validate(state: ForgotPasswordUiState): Boolean {
        val emailError = when {
            state.email.isBlank() -> "Email cannot be empty"
            !"^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex().matches(state.email) -> "Invalid email format"
            else -> null
        }

        _uiState.update { it.copy(emailError = emailError) }

        return emailError == null
    }
}
