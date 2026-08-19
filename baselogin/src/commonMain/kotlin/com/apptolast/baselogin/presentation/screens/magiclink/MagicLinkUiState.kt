package com.apptolast.baselogin.presentation.screens.magiclink

import com.apptolast.baselogin.util.ValidationError

data class MagicLinkUiState(
    val email: String = "",
    val emailError: ValidationError? = null,
    val isLoading: Boolean = false,
    val isLinkSent: Boolean = false,
)
