package com.apptolast.baselogin.presentation.screens.login

import com.apptolast.baselogin.domain.model.Credentials

/**
 * Maps the LoginUiState to a domain Credentials object.
 */
fun LoginUiState.toEmailPasswordCredentials(): Credentials.EmailPassword = Credentials.EmailPassword(
    email = this.email,
    password = this.password,
)
