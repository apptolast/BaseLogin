package com.apptolast.customlogin.presentation.screens.forgotpassword

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
 * ViewModel for the Forgot Password screen using MVI pattern.
 * Handles business logic and exposes state and effects to the UI.
 */
class ForgotPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ForgotPasswordEffect>()
    val effect = _effect.asSharedFlow()

    /**
     * The single entry point for all user actions from the UI.
     */
    fun onAction(action: ForgotPasswordAction) {
        when (action) {
            is ForgotPasswordAction.EmailChanged -> onEmailChange(action.email)
            is ForgotPasswordAction.SendResetEmailClicked -> onSendResetEmailClicked()
        }
    }

    private fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    private fun onSendResetEmailClicked() {
        val state = _uiState.value
        val emailError = validate(state)

        _uiState.update { it.copy(emailError = emailError) }

        if (emailError == null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }

                when (val result = authRepository.sendPasswordResetEmail(state.email)) {
                    is AuthResult.PasswordResetSent -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(ForgotPasswordEffect.ResetEmailSent)
                    }
                    is AuthResult.Failure -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(ForgotPasswordEffect.ShowError(result.error.message))
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(ForgotPasswordEffect.ShowError("An unexpected error occurred"))
                    }
                }
            }
        }
    }

    private fun validate(state: ForgotPasswordUiState): String? {
        return when {
            state.email.isBlank() -> "Email cannot be empty"
            !"^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex().matches(state.email) -> "Invalid email format"
            else -> null
        }
    }
}
