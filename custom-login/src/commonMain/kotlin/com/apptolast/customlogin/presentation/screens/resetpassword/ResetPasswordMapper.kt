package com.apptolast.customlogin.presentation.screens.resetpassword

import com.apptolast.customlogin.domain.model.PasswordResetData

/**
 * Maps the ResetPasswordUiState to a domain PasswordResetData object.
 */
fun ResetPasswordUiState.toPasswordResetData(): PasswordResetData {
    return PasswordResetData(
        code = this.resetCode,
        newPassword = this.newPassword
    )
}
