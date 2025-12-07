package com.apptolast.customlogin.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.LoginConfig
import com.apptolast.customlogin.domain.model.User
import com.apptolast.customlogin.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Login screen.
 * Handles business logic and exposes state to the UI.
 */
class LoginViewModel(
    private val loginConfig: LoginConfig,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadConfig()
    }

    private fun loadConfig() {
        _uiState.update { it.copy(config = loginConfig) }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            // Reset state before new login attempt
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.signInWithEmail(email, password)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = result.user
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
}
