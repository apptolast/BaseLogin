package com.apptolast.customlogin.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Represents the authentication flow navigation graph.
 */
@Serializable
data object AuthGraph

/**
 * Represents the main application flow after authentication.
 */
@Serializable
data object HomeGraph

// --- Routes within AuthGraph ---

@Serializable
data object WelcomeRoute

@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

@Serializable
data object ForgotPasswordRoute

// --- Routes within HomeGraph ---

@Serializable
data object HomeRoute
