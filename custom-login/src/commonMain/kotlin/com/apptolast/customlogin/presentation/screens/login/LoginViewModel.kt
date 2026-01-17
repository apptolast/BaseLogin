package com.apptolast.customlogin.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.SocialProvider
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Updates the email value in the UI state.
     */
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    /**
     * Updates the password value in the UI state.
     */
    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, errorMessage = null) }
    }

    /**
     * Handles social sign-in for different providers.
     * @param provider The social provider to sign in with.
     */
    fun onSocialSignIn(provider: SocialProvider) {
        when (provider) {
            is SocialProvider.Google -> signInWithGoogle()
            is SocialProvider.Phone -> signInWithPhone()
            is SocialProvider.Custom -> signInWithCustom(provider.id)
        }
    }

    private fun signInWithGoogle() {
        // TODO: Implement Google sign-in logic
    }

    private fun signInWithPhone() {
        // TODO: Implement Phone sign-in logic
    }

    private fun signInWithCustom(id: String) {
        // TODO: Implement custom sign-in logic for $id
    }

    /**
     * Executes the sign-in flow.
     * It validates the input and calls the repository to sign in.
     */
    fun signInWithEmail() = with(_uiState) {

        if (!validate(value)) return@with

        viewModelScope.launch {
            update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.signInWithEmail(value.email, value.password)) {
                is AuthResult.Success -> {
                    update { it.copy(isLoading = false, user = result.session) }
                }
                is AuthResult.Failure -> {
                    update { it.copy(isLoading = false, errorMessage = result.error.message) }
                }
                else -> {
                    update { it.copy(isLoading = false, errorMessage = "An unexpected error occurred") }
                }
            }
        }
    }

    private fun validate(state: LoginUiState): Boolean {
        val emailError = when {
            state.email.isBlank() -> "Email cannot be empty"
            !"^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex().matches(state.email) -> "Invalid email format"
            else -> null
        }

        val passwordError = when {
            state.password.isBlank() -> "Password cannot be empty"
            else -> null
        }

        _uiState.update {
            it.copy(
                emailError = emailError,
                passwordError = passwordError
            )
        }

        return emailError == null && passwordError == null
    }
}
