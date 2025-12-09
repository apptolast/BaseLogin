package com.apptolast.customlogin.presentation.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository,
//    private val loginConfig: LoginConfig,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadConfig()
    }

    private fun loadConfig() {
//        _uiState.update { it.copy(config = loginConfig) }
    }

    fun createUserWithEmail(fullName: String, email: String, password: String, confirmPassword: String) {
        val errors = validate(fullName, email, password, confirmPassword)
        if (errors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(validationErrors = errors)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.createUserWithEmail(email, password, fullName)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = result.session
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

                is AuthResult.RequiresEmailVerification -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Please check your email to verify your account"
                        )
                    }
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Registration failed"
                        )
                    }
                }
            }
        }
    }

    private fun validate(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Map<RegisterUiState.Field, String> {
        val errors = mutableMapOf<RegisterUiState.Field, String>()

        if (fullName.isBlank()) {
            errors[RegisterUiState.Field.FULL_NAME] = "Full name is required"
        }
        if (email.isBlank() || !isValidEmail(email)) {
            errors[RegisterUiState.Field.EMAIL] = "Invalid email"
        }
//        if (password.length < loginConfig.passwordMinLength) {
//            errors[RegisterUiState.Field.PASSWORD] =
//                "Password must be at least ${loginConfig.passwordMinLength} characters"
//        }
        if (password != confirmPassword) {
            errors[RegisterUiState.Field.CONFIRM_PASSWORD] = "Passwords do not match"
        }

        return errors
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

    sealed class RegisterEvent {
        data class ShowMessage(val message: String) : RegisterEvent()
    }
}
