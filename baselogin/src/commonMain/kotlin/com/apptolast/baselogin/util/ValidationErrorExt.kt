package com.apptolast.baselogin.util

import login.baselogin.generated.resources.Res
import login.baselogin.generated.resources.validation_confirm_password_empty
import login.baselogin.generated.resources.validation_email_empty
import login.baselogin.generated.resources.validation_email_invalid
import login.baselogin.generated.resources.validation_name_required
import login.baselogin.generated.resources.validation_otp_empty
import login.baselogin.generated.resources.validation_password_empty
import login.baselogin.generated.resources.validation_password_too_short
import login.baselogin.generated.resources.validation_passwords_no_match
import login.baselogin.generated.resources.validation_phone_empty
import org.jetbrains.compose.resources.StringResource

/**
 * Maps a [ValidationError] to its corresponding localized [StringResource].
 * Call [org.jetbrains.compose.resources.stringResource] on the result inside a Composable.
 */
fun ValidationError.toStringRes(): StringResource = when (this) {
    ValidationError.EmailEmpty -> Res.string.validation_email_empty
    ValidationError.EmailInvalid -> Res.string.validation_email_invalid
    ValidationError.PasswordEmpty -> Res.string.validation_password_empty
    ValidationError.PasswordTooShort -> Res.string.validation_password_too_short
    ValidationError.NameRequired -> Res.string.validation_name_required
    ValidationError.ConfirmPasswordEmpty -> Res.string.validation_confirm_password_empty
    ValidationError.PasswordsDoNotMatch -> Res.string.validation_passwords_no_match
    ValidationError.PhoneEmpty -> Res.string.validation_phone_empty
    ValidationError.OtpEmpty -> Res.string.validation_otp_empty
}
