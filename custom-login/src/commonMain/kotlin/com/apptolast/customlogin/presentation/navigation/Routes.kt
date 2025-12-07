package com.apptolast.customlogin.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object WelcomeRoute

@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

@Serializable
data object ForgotPasswordRoute

@Serializable
data class ResetPasswordRoute(val code: String = "")
