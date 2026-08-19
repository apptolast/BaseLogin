package com.apptolast.baselogin.presentation.screens.resetpassword

import com.apptolast.baselogin.domain.model.PasswordResetData

/**
 * Maps the ResetPasswordUiState to a domain PasswordResetData object.
 */
fun ResetPasswordUiState.toPasswordResetData(): PasswordResetData = PasswordResetData(
    code = this.resetCode,
    newPassword = this.newPassword,
)
