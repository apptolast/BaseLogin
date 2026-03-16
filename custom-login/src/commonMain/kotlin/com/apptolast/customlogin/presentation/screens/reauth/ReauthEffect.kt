package com.apptolast.customlogin.presentation.screens.reauth

sealed class ReauthEffect {
    data object Success : ReauthEffect()
    data class Error(val message: String) : ReauthEffect()
}
