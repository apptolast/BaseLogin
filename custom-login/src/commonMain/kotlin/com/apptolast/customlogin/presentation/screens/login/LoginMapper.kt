package com.apptolast.customlogin.presentation.screens.login

import com.apptolast.customlogin.domain.model.Credentials

/**
 * Maps the LoginUiState to a domain Credentials object.
 */
fun LoginUiState.toEmailPasswordCredentials(): Credentials.EmailPassword {
    return Credentials.EmailPassword(
        email = this.email,
        password = this.password
    )
}