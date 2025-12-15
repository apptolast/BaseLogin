package com.apptolast.customlogin.presentation.screens.resetpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Reset Password screen.
 * Handles the business logic and exposes state for the password reset flow.
 */
class ResetPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Sets the password reset code obtained from the navigation arguments.
     */
    fun setResetCode(code: String) {
        _uiState.update { it.copy(resetCode = code) }
    }

    /**
     * Updates the new password value in the UI state.
     */
    fun onNewPasswordChange(password: String) {
        _uiState.update { it.copy(newPassword = password, passwordError = null, errorMessage = null) }
    }

    /**
     * Updates the confirm password value in the UI state.
     */
    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null, errorMessage = null) }
    }

    /**
     * Executes the password reset flow.
     * It validates the input and calls the repository to confirm the password reset.
     */
    fun resetPassword() {
        val state = _uiState.value

        if (!validate(state)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.confirmPasswordReset(state.resetCode, state.newPassword)) {
                is AuthResult.PasswordResetSuccess -> {
                    _uiState.update { it.copy(isLoading = false, isPasswordReset = true) }
                }
                is AuthResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.error.message
                        )
                    }
                }
                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "An unexpected error occurred"
                        )
                    }
                }
            }
        }
    }

    private fun validate(state: ResetPasswordUiState): Boolean {
        val passwordError = when {
            state.newPassword.isBlank() -> "Password cannot be empty"
            // Add other password strength rules here if needed
            else -> null
        }

        val confirmPasswordError = when {
            state.confirmPassword.isBlank() -> "Please confirm your password"
            state.newPassword != state.confirmPassword -> "Passwords do not match"
            else -> null
        }

        _uiState.update {
            it.copy(
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
        }

        return passwordError == null && confirmPasswordError == null
    }
}
