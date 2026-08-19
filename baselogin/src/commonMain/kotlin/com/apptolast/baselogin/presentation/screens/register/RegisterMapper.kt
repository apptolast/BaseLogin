package com.apptolast.baselogin.presentation.screens.register

import com.apptolast.baselogin.domain.model.SignUpData

/**
 * Maps the RegisterUiState to a domain SignUpData object.
 */
fun RegisterUiState.toSignUpData(): SignUpData = SignUpData(
    email = this.email,
    password = this.password,
    displayName = this.fullName,
)
