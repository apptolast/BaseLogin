package com.apptolast.customlogin.util

/**
 * Typed validation errors for form fields.
 * Each variant maps to a localized string resource via [toStringRes].
 */
sealed interface ValidationError {
    data object EmailEmpty : ValidationError
    data object EmailInvalid : ValidationError
    data object PasswordEmpty : ValidationError
    data object PasswordTooShort : ValidationError
    data object NameRequired : ValidationError
    data object ConfirmPasswordEmpty : ValidationError
    data object PasswordsDoNotMatch : ValidationError
    data object PhoneEmpty : ValidationError
    data object OtpEmpty : ValidationError
}
