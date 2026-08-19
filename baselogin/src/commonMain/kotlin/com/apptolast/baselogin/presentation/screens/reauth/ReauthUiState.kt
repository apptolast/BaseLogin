package com.apptolast.baselogin.presentation.screens.reauth

import com.apptolast.baselogin.domain.model.AuthError
import com.apptolast.baselogin.domain.model.IdentityProvider
import com.apptolast.baselogin.util.ValidationError

data class ReauthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loadingProvider: IdentityProvider? = null,
    val emailError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val authError: AuthError? = null,
    val availableProviders: List<IdentityProvider> = emptyList(),
)
