package com.apptolast.customlogin.presentation.screens.magiclink

import com.apptolast.customlogin.util.ValidationError

data class MagicLinkUiState(
    val email: String = "",
    val emailError: ValidationError? = null,
    val isLoading: Boolean = false,
    val isLinkSent: Boolean = false
)
