package com.apptolast.customlogin.presentation.screens.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.customlogin.domain.AuthRepository
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.PhoneAuthResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PhoneAuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhoneAuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<PhoneAuthEffect>()
    val effect = _effect.asSharedFlow()

    private var verificationId: String? = null

    fun onAction(action: PhoneAuthAction) {
        when (action) {
            is PhoneAuthAction.PhoneNumberChanged -> _uiState.update { it.copy(phoneNumber = action.phoneNumber, error = null) }
            is PhoneAuthAction.VerificationCodeChanged -> _uiState.update { it.copy(verificationCode = action.code, error = null) }
            PhoneAuthAction.SendVerificationCode -> sendVerificationCode()
            PhoneAuthAction.SignInWithCode -> signInWithCode()
        }
    }

    private fun sendVerificationCode() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        authRepository.verifyPhoneNumber(_uiState.value.phoneNumber)
            .onEach { result ->
                when (result) {
                    is PhoneAuthResult.CodeSent -> {
                        this.verificationId = result.verificationId
                        _uiState.update { it.copy(isLoading = false, isCodeSent = true) }
                    }

                    is PhoneAuthResult.Failure -> {
                        _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                    }

                    is PhoneAuthResult.VerificationCompleted -> {
                        // This case is for auto-retrieval, which we can handle directly
                        // For simplicity, we are letting the user enter the code manually
                        // but a full implementation could sign in automatically here.
                    }
                }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "An unknown error occurred") }
            }
            .launchIn(viewModelScope)
    }

    private fun signInWithCode() {
        val verificationId = this.verificationId ?: return
        val code = _uiState.value.verificationCode

        if (code.isBlank()) {
            _uiState.update { it.copy(error = "Verification code cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signInWithPhoneCredential(verificationId, code)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.emit(PhoneAuthEffect.NavigateToHome)
                }
                is AuthResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false, error = "An unexpected error occurred") }
                }
            }
        }
    }
}
