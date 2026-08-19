package com.apptolast.baselogin.presentation.screens.phone

import com.apptolast.baselogin.domain.model.AuthError

sealed interface PhoneAuthEffect {
    data object NavigateToHome : PhoneAuthEffect
    data class ShowError(val error: AuthError) : PhoneAuthEffect
}
