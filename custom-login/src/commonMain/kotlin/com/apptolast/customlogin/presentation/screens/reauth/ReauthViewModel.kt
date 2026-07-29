package com.apptolast.customlogin.presentation.screens.reauth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.customlogin.domain.AuthRepository
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.util.ValidationError
import com.apptolast.customlogin.util.Validators
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Re-authentication screen using MVI pattern.
 * Used before sensitive account operations (delete account, update email/password, etc.).
 */
class ReauthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReauthUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ReauthEffect>()
    val effect = _effect.asSharedFlow()

    init {
        // Exclude Phone and MagicLink — not valid for reauthentication
        val oauthProviders = authRepository.getAvailableProviders().filter { provider ->
            provider !is IdentityProvider.Phone && provider !is IdentityProvider.MagicLink
        }
        _uiState.update { it.copy(availableProviders = oauthProviders) }
    }

    fun onAction(action: ReauthAction) {
        when (action) {
            is ReauthAction.EmailChanged -> _uiState.update { it.copy(email = action.email, emailError = null) }
            is ReauthAction.PasswordChanged -> _uiState.update {
                it.copy(password = action.password, passwordError = null)
            }
            is ReauthAction.SubmitEmailPassword -> onSubmitEmailPassword()
            is ReauthAction.SubmitOAuth -> onSubmitOAuth(action.provider)
        }
    }

    private fun onSubmitEmailPassword() {
        val state = _uiState.value
        val emailError: ValidationError? = when {
            state.email.isBlank() -> ValidationError.EmailEmpty
            !Validators.isValidEmail(state.email) -> ValidationError.EmailInvalid
            else -> null
        }
        val passwordError: ValidationError? = when {
            state.password.isBlank() -> ValidationError.PasswordEmpty
            else -> null
        }

        _uiState.update { it.copy(emailError = emailError, passwordError = passwordError, authError = null) }

        if (emailError != null || passwordError != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val credentials = Credentials.EmailPassword(state.email, state.password)
            when (val result = authRepository.reauthenticate(credentials)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.emit(ReauthEffect.Success)
                }
                is AuthResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, authError = result.error) }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.emit(ReauthEffect.ShowError(AuthError.Unknown()))
                }
            }
        }
    }

    private fun onSubmitOAuth(provider: IdentityProvider) {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingProvider = provider, authError = null) }
            val credentials = Credentials.OAuthToken(provider = provider)
            when (val result = authRepository.reauthenticate(credentials)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(loadingProvider = null) }
                    _effect.emit(ReauthEffect.Success)
                }
                is AuthResult.Failure -> {
                    _uiState.update { it.copy(loadingProvider = null, authError = result.error) }
                }
                else -> {
                    _uiState.update { it.copy(loadingProvider = null) }
                    _effect.emit(ReauthEffect.ShowError(AuthError.Unknown()))
                }
            }
        }
    }
}
