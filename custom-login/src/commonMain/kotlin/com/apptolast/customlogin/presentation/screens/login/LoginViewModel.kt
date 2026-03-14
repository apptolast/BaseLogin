package com.apptolast.customlogin.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.customlogin.domain.AuthRepository
import com.apptolast.customlogin.domain.model.AuthRequest
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect = _effect.asSharedFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> onEmailChange(action.email)
            is LoginAction.PasswordChanged -> onPasswordChange(action.password)
            is LoginAction.SignIn -> onSignIn(action.provider)
        }
    }

    private fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    private fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    private fun onSignIn(provider: IdentityProvider) {
        viewModelScope.launch {
            val state = _uiState.value
            val request = createAuthRequest(provider, state)

            if (request == null) { // Validation failed for email
                _uiState.update { it.copy(loadingState = LoginLoadingState.Idle) }
                return@launch
            }

            val loadingState = if (provider is IdentityProvider.Email) LoginLoadingState.EmailSignIn else LoginLoadingState.SocialSignIn(provider)
            _uiState.update { it.copy(loadingState = loadingState) }

            handleAuthResult(authRepository.signIn(request))
        }
    }

    private fun createAuthRequest(provider: IdentityProvider, state: LoginUiState): AuthRequest? {
        return when (provider) {
            is IdentityProvider.Email -> {
                if (!validate(state.email, state.password)) return null
                AuthRequest(provider, email = state.email, password = state.password)
            }
            else -> AuthRequest(provider)
        }
    }

    private suspend fun handleAuthResult(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                _effect.emit(LoginEffect.NavigateToHome)
            }
            is AuthResult.Failure -> {
                _effect.emit(LoginEffect.ShowError(result.error.message))
            }
            else -> {
                _effect.emit(LoginEffect.ShowError("An unexpected result was returned: $result"))
            }
        }
        _uiState.update { it.copy(loadingState = LoginLoadingState.Idle) }
    }

    private fun validate(email: String, password: String): Boolean {
        val emailError = if (email.isBlank()) "Email can't be empty" else null
        val passwordError = if (password.isBlank()) "Password can't be empty" else null

        _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }

        return emailError == null && passwordError == null
    }
}
