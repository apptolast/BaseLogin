package com.apptolast.customlogin.presentation.screens.resetpassword

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
 * ViewModel for the Reset Password screen using MVI pattern.
 * Handles the business logic and exposes state and effects to the UI.
 */
class ResetPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ResetPasswordEffect>()
    val effect = _effect.asSharedFlow()

    /**
     * The single entry point for all user actions from the UI.
     */
    fun onAction(action: ResetPasswordAction) {
        when (action) {
            is ResetPasswordAction.NewPasswordChanged -> onNewPasswordChange(action.password)
            is ResetPasswordAction.ConfirmPasswordChanged -> onConfirmPasswordChange(action.confirmPassword)
            is ResetPasswordAction.ResetPasswordClicked -> onResetPasswordClicked()
        }
    }

    /**
     * Sets the password reset code obtained from the navigation arguments.
     * This remains separate as it's a one-time setup event from navigation, not a user action.
     */
    fun setResetCode(code: String) {
        _uiState.update { it.copy(resetCode = code) }
    }

    private fun onNewPasswordChange(password: String) {
        _uiState.update { it.copy(newPassword = password, passwordError = null) }
    }

    private fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    private fun onResetPasswordClicked() {
        val state = _uiState.value
        val (passwordError, confirmPasswordError) = validate(state)

        _uiState.update {
            it.copy(
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
        }

        if (passwordError == null && confirmPasswordError == null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                when (val result = authRepository.confirmPasswordReset(state.toPasswordResetData())) {
                    is AuthResult.PasswordResetSuccess -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(ResetPasswordEffect.NavigateToLogin)
                    }
                    is AuthResult.Failure -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(ResetPasswordEffect.ShowError(result.error.message))
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(ResetPasswordEffect.ShowError("An unexpected error occurred"))
                    }
                }
            }
        }
    }

    private fun validate(state: ResetPasswordUiState): Pair<String?, String?> {
        val passwordError = when {
            state.newPassword.isBlank() -> "Password cannot be empty"
            state.newPassword.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }

        val confirmPasswordError = when {
            state.confirmPassword.isBlank() -> "Please confirm your password"
            state.newPassword != state.confirmPassword -> "Passwords do not match"
            else -> null
        }

        return passwordError to confirmPasswordError
    }
}
