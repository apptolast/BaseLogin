package com.apptolast.baselogin.presentation.screens.magiclink

import com.apptolast.baselogin.domain.model.AuthError

sealed interface MagicLinkEffect {
    data class ShowError(val error: AuthError) : MagicLinkEffect
}
