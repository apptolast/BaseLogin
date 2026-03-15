package com.apptolast.customlogin.presentation.screens.magiclink

data class MagicLinkUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isLinkSent: Boolean = false
)