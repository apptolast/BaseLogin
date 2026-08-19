package com.apptolast.baselogin.presentation.screens.magiclink

sealed interface MagicLinkAction {
    data class EmailChanged(val email: String) : MagicLinkAction
    data object SendLinkClicked : MagicLinkAction
}
