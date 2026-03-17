package com.apptolast.customlogin.presentation.screens.phone

import com.apptolast.customlogin.domain.model.AuthError

sealed interface PhoneAuthEffect {
    data object NavigateToHome : PhoneAuthEffect
    data class ShowError(val error: AuthError) : PhoneAuthEffect
}
