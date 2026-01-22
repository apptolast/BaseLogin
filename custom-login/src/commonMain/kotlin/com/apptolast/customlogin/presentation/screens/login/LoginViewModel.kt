package com.apptolast.customlogin.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.customlogin.domain.AuthRepository
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.IdentityProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Login screen using MVI pattern.
 * Handles business logic and exposes state and effects to the UI.
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect = _effect.asSharedFlow()

    /**
     * The single entry point for all user actions from the UI.
     */
    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> onEmailChange(action.email)
            is LoginAction.PasswordChanged -> onPasswordChange(action.password)
            is LoginAction.SocialSignInClicked -> onSocialSignIn(action.provider)
            is LoginAction.SignInClicked -> onSignInClicked()
            is LoginAction.ErrorMessageDismissed -> { /* No-op, errors are now one-time effects */
            }
        }
    }

    private fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    private fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    private fun onSocialSignIn(provider: IdentityProvider) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val credentials = Credentials.OAuthToken(provider = provider)
            when (val result = authRepository.signIn(credentials)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.emit(LoginEffect.NavigateToHome)
                }

                is AuthResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.emit(LoginEffect.ShowError(result.error.message))
                }

                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.emit(LoginEffect.ShowError("An unexpected error occurred"))
                }
            }
        }
    }

    private fun onSignInClicked() {
        val state = _uiState.value
        val (emailError, passwordError) = validate(state)

        _uiState.update {
            it.copy(
                emailError = emailError,
                passwordError = passwordError
            )
        }

        if (emailError == null && passwordError == null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }

                when (val result = authRepository.signIn(state.toEmailPasswordCredentials())) {
                    is AuthResult.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(LoginEffect.NavigateToHome)
                    }

                    is AuthResult.Failure -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(LoginEffect.ShowError(result.error.message))
                    }

                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(LoginEffect.ShowError("An unexpected error occurred"))
                    }
                }
            }
        }
    }

    private fun validate(state: LoginUiState): Pair<String?, String?> {
        val emailError = when {
            state.email.isBlank() -> "Email cannot be empty"
            !"^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
                .matches(state.email) -> "Invalid email format"

            else -> null
        }

        val passwordError = when {
            state.password.isBlank() -> "Password cannot be empty"
            else -> null
        }

        return emailError to passwordError
    }
}
