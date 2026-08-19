package com.apptolast.baselogin.presentation.screens.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.baselogin.di.LoginLibraryConfig
import com.apptolast.baselogin.domain.AuthRepository
import com.apptolast.baselogin.domain.model.AuthError
import com.apptolast.baselogin.domain.model.AuthResult
import com.apptolast.baselogin.domain.model.PhoneAuthResult
import com.apptolast.baselogin.util.ValidationError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Phone Authentication screen using MVI pattern.
 * Manages the two-step phone auth flow: sending the OTP and verifying it.
 */
class PhoneAuthViewModel(private val authRepository: AuthRepository, config: LoginLibraryConfig) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PhoneAuthUiState(countryCode = config.phoneAuthConfig.defaultCountryCode),
    )
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<PhoneAuthEffect>()
    val effect = _effect.asSharedFlow()

    fun onAction(action: PhoneAuthAction) {
        when (action) {
            is PhoneAuthAction.CountryCodeChanged ->
                _uiState.update { it.copy(countryCode = action.dialCode, phoneError = null) }
            is PhoneAuthAction.PhoneNumberChanged ->
                _uiState.update { it.copy(phoneNumber = action.phoneNumber, phoneError = null) }
            is PhoneAuthAction.OtpCodeChanged ->
                _uiState.update { it.copy(otpCode = action.code, otpError = null) }
            is PhoneAuthAction.SendCodeClicked -> onSendCode()
            is PhoneAuthAction.VerifyCodeClicked -> onVerifyCode()
        }
    }

    private fun onSendCode() {
        val state = _uiState.value
        val digits = state.phoneNumber.filter { it.isDigit() }.trimStart('0')
        if (digits.isBlank()) {
            _uiState.update { it.copy(phoneError = ValidationError.PhoneEmpty) }
            return
        }
        val phone = "${state.countryCode}$digits"
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = authRepository.sendPhoneOtp(phone)) {
                is PhoneAuthResult.CodeSent -> {
                    _uiState.update { it.copy(isLoading = false, verificationId = result.verificationId) }
                }
                is PhoneAuthResult.AutoSignedIn -> {
                    // Android instant verification — no OTP step needed
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.emit(PhoneAuthEffect.NavigateToHome)
                }
                is PhoneAuthResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.emit(PhoneAuthEffect.ShowError(result.error))
                }
            }
        }
    }

    private fun onVerifyCode() {
        val state = _uiState.value
        val verificationId = state.verificationId ?: return
        val code = state.otpCode.trim()
        if (code.isBlank()) {
            _uiState.update { it.copy(otpError = ValidationError.OtpEmpty) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = authRepository.verifyPhoneOtp(verificationId, code)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.emit(PhoneAuthEffect.NavigateToHome)
                }
                is AuthResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.emit(PhoneAuthEffect.ShowError(result.error))
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.emit(PhoneAuthEffect.ShowError(AuthError.Unknown()))
                }
            }
        }
    }
}
