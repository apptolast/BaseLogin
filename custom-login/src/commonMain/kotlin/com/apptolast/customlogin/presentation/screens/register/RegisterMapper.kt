package com.apptolast.customlogin.presentation.screens.register

import com.apptolast.customlogin.domain.model.SignUpData

/**
 * Maps the RegisterUiState to a domain SignUpData object.
 */
fun RegisterUiState.toSignUpData(): SignUpData {
    return SignUpData(
        email = this.email,
        password = this.password,
        displayName = this.fullName
    )
}