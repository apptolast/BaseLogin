package com.apptolast.customlogin.presentation.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.customlogin.di.LoginLibraryConfig
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
 * ViewModel for the Register screen using MVI pattern.
 * Handles business logic and exposes state and effects to the UI.
 */
class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val config: LoginLibraryConfig,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(availableProviders = authRepository.getAvailableProviders()) }
    }

    private val _effect = MutableSharedFlow<RegisterEffect>()
    val effect = _effect.asSharedFlow()

    /**
     * The single entry point for all user actions from the UI.
     */
    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.FullNameChanged -> onFullNameChange(action.name)
            is RegisterAction.EmailChanged -> onEmailChange(action.email)
            is RegisterAction.PasswordChanged -> onPasswordChange(action.password)
            is RegisterAction.ConfirmPasswordChanged -> onConfirmPasswordChange(action.confirmPassword)
            is RegisterAction.TermsAcceptedChanged -> onTermsAcceptedChange(action.accepted)
            is RegisterAction.SignUpClicked -> onSignUpClicked()
            is RegisterAction.SignUpWithOAuth -> onSocialSignUp(action.provider)
        }
    }

    private fun onFullNameChange(name: String) {
        _uiState.update { it.copy(fullName = name, fullNameError = null) }
    }

    private fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    private fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    private fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    private fun onTermsAcceptedChange(accepted: Boolean) {
        _uiState.update { it.copy(termsAccepted = accepted) }
    }

    private fun onSocialSignUp(provider: IdentityProvider) {
        if (provider == IdentityProvider.Phone) {
            viewModelScope.launch { _effect.emit(RegisterEffect.NavigateToPhoneAuth) }
            return
        }
        if (provider == IdentityProvider.MagicLink) {
            viewModelScope.launch { _effect.emit(RegisterEffect.NavigateToMagicLink) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loadingProvider = provider) }
            when (val result = authRepository.signIn(Credentials.OAuthToken(provider = provider))) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(loadingProvider = null) }
                    _effect.emit(RegisterEffect.NavigateToHome)
                }
                is AuthResult.Failure -> {
                    _uiState.update { it.copy(loadingProvider = null) }
                    _effect.emit(RegisterEffect.ShowError(result.error))
                }
                else -> {
                    _uiState.update { it.copy(loadingProvider = null) }
                    _effect.emit(RegisterEffect.ShowError(AuthError.Unknown()))
                }
            }
        }
    }

    private fun onSignUpClicked() {
        val state = _uiState.value
        val validationErrors = validate(state)

        _uiState.update {
            it.copy(
                fullNameError = validationErrors.fullNameError,
                emailError = validationErrors.emailError,
                passwordError = validationErrors.passwordError,
                confirmPasswordError = validationErrors.confirmPasswordError
            )
        }

        if (validationErrors.isValid) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                when (val result = authRepository.signUp(state.toSignUpData())) {
                    is AuthResult.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(RegisterEffect.NavigateToHome)
                    }
                    is AuthResult.Failure -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(RegisterEffect.ShowError(result.error))
                    }
                    is AuthResult.RequiresEmailVerification -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(RegisterEffect.ShowError(AuthError.RequiresEmailVerification()))
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.emit(RegisterEffect.ShowError(AuthError.Unknown()))
                    }
                }
            }
        }
    }

    private fun validate(state: RegisterUiState): ValidationErrors {
        val fullNameError: ValidationError? = when {
            state.fullName.isBlank() -> ValidationError.NameRequired
            else -> null
        }
        val emailError: ValidationError? = when {
            state.email.isBlank() -> ValidationError.EmailEmpty
            !Validators.isValidEmail(state.email) -> ValidationError.EmailInvalid
            else -> null
        }
        val passwordError: ValidationError? = when {
            !Validators.isValidPassword(
                password = state.password,
                minLength = config.passwordPolicy.minLength,
                rejectBlank = config.passwordPolicy.rejectBlank
            ) -> ValidationError.PasswordTooShort
            else -> null
        }
        val confirmPasswordError: ValidationError? = when {
            state.confirmPassword.isBlank() -> ValidationError.ConfirmPasswordEmpty
            state.password != state.confirmPassword -> ValidationError.PasswordsDoNotMatch
            else -> null
        }
        return ValidationErrors(
            fullNameError = fullNameError,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError
        )
    }

    private data class ValidationErrors(
        val fullNameError: ValidationError?,
        val emailError: ValidationError?,
        val passwordError: ValidationError?,
        val confirmPasswordError: ValidationError?
    ) {
        val isValid: Boolean
            get() = fullNameError == null && emailError == null && passwordError == null && confirmPasswordError == null
    }
}
