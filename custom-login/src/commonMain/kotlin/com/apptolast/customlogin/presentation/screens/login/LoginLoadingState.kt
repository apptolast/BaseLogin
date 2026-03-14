package com.apptolast.customlogin.presentation.screens.login

import com.apptolast.customlogin.domain.model.IdentityProvider

/**
 * Represents the different loading states of the login screen.
 * It's a sealed interface to ensure type safety and explicit state handling.
 */
sealed interface LoginLoadingState {
    /** The screen is not in a loading state. */
    data object Idle : LoginLoadingState

    /** The email and password sign-in process is in progress. */
    data object EmailSignIn : LoginLoadingState

    /** A social sign-in process is in progress. */
    data class SocialSignIn(val provider: IdentityProvider) : LoginLoadingState
}
