package com.apptolast.login.presentation.profile

import com.apptolast.customlogin.domain.model.UserSession

/**
 * Represents the state of the Profile screen.
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val userSession: UserSession? = null,
)

/**
 * User actions from the Profile screen.
 */
sealed interface ProfileAction {
    data object SignOutClicked : ProfileAction
}

/**
 * One-time side effects from the Profile screen.
 */
sealed interface ProfileEffect {
    // No effects needed for now, as sign-out is handled globally.
}
