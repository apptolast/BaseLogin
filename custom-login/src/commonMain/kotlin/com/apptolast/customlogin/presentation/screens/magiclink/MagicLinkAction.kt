package com.apptolast.customlogin.presentation.screens.magiclink

sealed interface MagicLinkAction {
    data class EmailChanged(val email: String) : MagicLinkAction
    data object SendLinkClicked : MagicLinkAction
}