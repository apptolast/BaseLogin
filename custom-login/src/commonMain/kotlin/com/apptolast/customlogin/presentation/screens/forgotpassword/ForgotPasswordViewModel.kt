package com.apptolast.customlogin.presentation.screens.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.LoginConfig
import com.apptolast.customlogin.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Forgot Password screen.
 */
class ForgotPasswordViewModel(
    private val loginConfig: LoginConfig,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(config = loginConfig) }
    }

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailError = validateEmail(email),
                errorMessage = null
            )
        }
    }

    fun sendPasswordResetEmail() {
        val email = _uiState.value.email.trim()

        // Validate email
        val emailError = validateEmail(email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is AuthResult.PasswordResetSent -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEmailSent = true
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

    fun resetState() {
        _uiState.update {
            ForgotPasswordUiState(config = loginConfig)
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !isValidEmail(email) -> "Invalid email format"
            else -> null
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }
}
