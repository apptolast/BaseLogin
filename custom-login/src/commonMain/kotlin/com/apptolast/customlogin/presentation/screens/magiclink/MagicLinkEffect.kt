package com.apptolast.customlogin.presentation.screens.magiclink

import com.apptolast.customlogin.domain.model.AuthError

sealed interface MagicLinkEffect {
    data class ShowError(val error: AuthError) : MagicLinkEffect
}