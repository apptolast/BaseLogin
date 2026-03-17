package com.apptolast.customlogin.presentation.screens.magiclink

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.customlogin.domain.AuthRepository
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.util.ValidationError
import com.apptolast.customlogin.util.Validators
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MagicLinkViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MagicLinkUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MagicLinkEffect>()
    val effect = _effect.asSharedFlow()

    fun onAction(action: MagicLinkAction) {
        when (action) {
            is MagicLinkAction.EmailChanged -> _uiState.update {
                it.copy(email = action.email, emailError = null)
            }
            is MagicLinkAction.SendLinkClicked -> onSendLink()
        }
    }

    private fun onSendLink() {
        val email = _uiState.value.email.trim()

        val emailError: ValidationError? = when {
            email.isBlank() -> ValidationError.EmailEmpty
            !Validators.isValidEmail(email) -> ValidationError.EmailInvalid
            else -> null
        }

        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = authRepository.sendMagicLink(email)) {
                is AuthResult.MagicLinkSent -> {
                    _uiState.update { it.copy(isLoading = false, isLinkSent = true) }
                }
                is AuthResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.emit(MagicLinkEffect.ShowError(result.error))
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.emit(MagicLinkEffect.ShowError(AuthError.Unknown()))
                }
            }
        }
    }
}
