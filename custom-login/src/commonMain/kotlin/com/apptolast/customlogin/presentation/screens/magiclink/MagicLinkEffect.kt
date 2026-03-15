package com.apptolast.customlogin.presentation.screens.magiclink

sealed interface MagicLinkEffect {
    data class ShowError(val message: String) : MagicLinkEffect
}