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
 * Handles the password reset confirmation flow.
 */
class ResetPasswordViewModel(
//    private val loginConfig: LoginConfig,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Set the reset code from deep link or email.
     */
    fun setResetCode(code: String) {
        _uiState.update { it.copy(resetCode = code) }
    }

    fun onNewPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                newPassword = password,
                passwordError = validatePassword(password),
                confirmPasswordError = if (it.confirmPassword.isNotBlank() && password != it.confirmPassword) {
                    "Passwords do not match"
                } else null,
                errorMessage = null
            )
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update {
            it.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = if (confirmPassword != it.newPassword) {
                    "Passwords do not match"
                } else null,
                errorMessage = null
            )
        }
    }

    fun resetPassword() {
        val state = _uiState.value

        // Validate passwords
        val passwordError = validatePassword(state.newPassword)
        if (passwordError != null) {
            _uiState.update { it.copy(passwordError = passwordError) }
            return
        }

        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            return
        }

        if (state.resetCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Invalid reset code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.confirmPasswordReset(
                code = state.resetCode,
                newPassword = state.newPassword
            )) {
                is AuthResult.PasswordResetSent,
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPasswordReset = true
                        )
                    }
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

    /**
     * Update password for already authenticated user.
     */
    fun updatePassword() {
        val state = _uiState.value

        val passwordError = validatePassword(state.newPassword)
        if (passwordError != null) {
            _uiState.update { it.copy(passwordError = passwordError) }
            return
        }

        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            authRepository.updatePassword(state.newPassword)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPasswordReset = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to update password"
                        )
                    }
                }
        }
    }

    private fun validatePassword(password: String): String? {
//        val minLength = loginConfig.passwordMinLength
        return when {
            password.isBlank() -> "Password is required"
//            password.length < minLength -> "Password must be at least $minLength characters"
            !password.any { it.isUpperCase() } -> "Password must contain an uppercase letter"
            !password.any { it.isLowerCase() } -> "Password must contain a lowercase letter"
            !password.any { it.isDigit() } -> "Password must contain a number"
            else -> null
        }
    }

    fun resetState() {
        _uiState.update { ResetPasswordUiState() }
    }
}
