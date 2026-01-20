package com.apptolast.login.home.presentation.home

import com.apptolast.customlogin.domain.model.UserSession

/**
 * Represents the state for the HomeScreen.
 *
 * @property isLoading Indicates if the screen is loading initial data.
 * @property userSession The current authenticated user's session data.
 * @property isLoggedOut A flag to signal that the logout process is complete.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val userSession: UserSession? = null,
    val isLoggedOut: Boolean = false
)