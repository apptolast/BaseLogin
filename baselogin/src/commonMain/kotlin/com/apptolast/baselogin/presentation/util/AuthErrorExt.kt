package com.apptolast.baselogin.presentation.util

import com.apptolast.baselogin.domain.model.AuthError
import login.baselogin.generated.resources.Res
import login.baselogin.generated.resources.auth_error_account_exists_with_different_credential
import login.baselogin.generated.resources.auth_error_credential_already_in_use
import login.baselogin.generated.resources.auth_error_email_already_in_use
import login.baselogin.generated.resources.auth_error_invalid_credentials
import login.baselogin.generated.resources.auth_error_invalid_email
import login.baselogin.generated.resources.auth_error_invalid_otp
import login.baselogin.generated.resources.auth_error_invalid_reset_code
import login.baselogin.generated.resources.auth_error_network_error
import login.baselogin.generated.resources.auth_error_operation_not_allowed
import login.baselogin.generated.resources.auth_error_phone_invalid
import login.baselogin.generated.resources.auth_error_provider_not_configured
import login.baselogin.generated.resources.auth_error_quota_exceeded
import login.baselogin.generated.resources.auth_error_requires_email_verification
import login.baselogin.generated.resources.auth_error_requires_recent_login
import login.baselogin.generated.resources.auth_error_session_expired
import login.baselogin.generated.resources.auth_error_sign_in_cancelled
import login.baselogin.generated.resources.auth_error_too_many_requests
import login.baselogin.generated.resources.auth_error_unknown
import login.baselogin.generated.resources.auth_error_user_disabled
import login.baselogin.generated.resources.auth_error_user_not_found
import login.baselogin.generated.resources.auth_error_verification_code_expired
import login.baselogin.generated.resources.auth_error_weak_password
import org.jetbrains.compose.resources.StringResource

/**
 * Maps an [AuthError] to its corresponding localized [StringResource].
 * Call [org.jetbrains.compose.resources.stringResource] on the result inside a Composable.
 */
fun AuthError.toStringRes(): StringResource = when (this) {
    is AuthError.InvalidCredentials -> Res.string.auth_error_invalid_credentials
    is AuthError.UserNotFound -> Res.string.auth_error_user_not_found
    is AuthError.EmailAlreadyInUse -> Res.string.auth_error_email_already_in_use
    is AuthError.WeakPassword -> Res.string.auth_error_weak_password
    is AuthError.InvalidEmail -> Res.string.auth_error_invalid_email
    is AuthError.InvalidResetCode -> Res.string.auth_error_invalid_reset_code
    is AuthError.NetworkError -> Res.string.auth_error_network_error
    is AuthError.TooManyRequests -> Res.string.auth_error_too_many_requests
    is AuthError.UserDisabled -> Res.string.auth_error_user_disabled
    is AuthError.OperationNotAllowed -> Res.string.auth_error_operation_not_allowed
    is AuthError.RequiresEmailVerification -> Res.string.auth_error_requires_email_verification
    is AuthError.SessionExpired -> Res.string.auth_error_session_expired
    is AuthError.PhoneNumberInvalid -> Res.string.auth_error_phone_invalid
    is AuthError.InvalidVerificationCode -> Res.string.auth_error_invalid_otp
    is AuthError.Unknown -> Res.string.auth_error_unknown
    is AuthError.AccountExistsWithDifferentCredential -> Res.string.auth_error_account_exists_with_different_credential
    is AuthError.CredentialAlreadyInUse -> Res.string.auth_error_credential_already_in_use
    is AuthError.RequiresRecentLogin -> Res.string.auth_error_requires_recent_login
    is AuthError.VerificationCodeExpired -> Res.string.auth_error_verification_code_expired
    is AuthError.QuotaExceeded -> Res.string.auth_error_quota_exceeded
    is AuthError.SignInCancelled -> Res.string.auth_error_sign_in_cancelled
    is AuthError.ProviderNotConfigured -> Res.string.auth_error_provider_not_configured
}

/**
 * Whether this error means the user backed out on purpose, so screens clear their loading state
 * without showing anything.
 */
internal val AuthError.isUserCancellation: Boolean
    get() = this is AuthError.SignInCancelled
