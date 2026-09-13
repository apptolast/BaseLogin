package com.apptolast.baselogin.data

import com.apptolast.baselogin.data.FirebaseAuthProvider.Companion.PROVIDER_ID
import com.apptolast.baselogin.data.firebase.FirebaseAuthUser
import com.apptolast.baselogin.domain.model.AuthError
import com.apptolast.baselogin.domain.model.UserSession

/**
 * Maps a [FirebaseAuthUser] to a domain [UserSession].
 *
 * [accessToken] is passed in by the caller rather than fetched here, because obtaining one is a
 * potentially networked operation: `AuthProvider.getCurrentSession` documents that it MUST NOT
 * perform network I/O, so it maps with a null token and callers that need one use
 * `getIdToken(forceRefresh)`.
 */
internal fun FirebaseAuthUser.toUserSession(accessToken: String? = null): UserSession = UserSession(
    userId = uid,
    email = email,
    displayName = displayName,
    photoUrl = photoUrl,
    isEmailVerified = isEmailVerified,
    providerId = PROVIDER_ID,
    accessToken = accessToken,
    refreshToken = null,
    expiresAt = null,
)

/**
 * Maps a Firebase failure to a domain [AuthError], by **error code** first.
 *
 * [code] may come in any of the four families Firebase uses — native Android (`ERROR_EMAIL_ALREADY_IN_USE`),
 * web (`auth/email-already-in-use`, `email-already-in-use`), REST (`EMAIL_EXISTS`) or iOS numeric
 * (`17007`) — and is compared by equality after normalisation, never as a substring. When [code] is
 * null or unrecognised it falls back to [mapFirebaseErrorMessage]. [AuthError.Unknown] keeps both
 * [message] and [cause].
 */
internal fun mapFirebaseError(code: String?, message: String, cause: Throwable? = null): AuthError {
    val canonical = code?.let(::canonicalErrorCode)
    val factory = canonical?.let { AUTH_ERRORS_BY_CODE[it] }
    return factory?.invoke(message, cause) ?: mapFirebaseErrorMessage(message, cause)
}

/**
 * Brings the four spellings of a Firebase error code to one form: `auth/email-already-in-use`,
 * `email-already-in-use` and `ERROR_EMAIL_ALREADY_IN_USE` all become `EMAIL_ALREADY_IN_USE`.
 */
private fun canonicalErrorCode(code: String): String = code.trim()
    .removePrefix("auth/")
    .uppercase()
    .replace('-', '_')
    .removePrefix("ERROR_")

private typealias AuthErrorFactory = (message: String, cause: Throwable?) -> AuthError

/**
 * Canonical code → [AuthError]. Holds the canonical Android/web names, the REST names that differ
 * from them and the iOS `FIRAuthErrorCode` numbers (verified against `AuthErrors.swift`).
 */
private val AUTH_ERRORS_BY_CODE: Map<String, AuthErrorFactory> = buildMap {
    fun register(vararg codes: String, factory: AuthErrorFactory) = codes.forEach { put(it, factory) }

    register(
        "INVALID_CREDENTIAL",
        "INVALID_LOGIN_CREDENTIALS",
        "INVALID_PASSWORD",
        "WRONG_PASSWORD",
        "17004",
        "17009",
    ) { _, _ -> AuthError.InvalidCredentials() }
    register("USER_NOT_FOUND", "17011") { _, _ -> AuthError.UserNotFound() }
    register("EMAIL_ALREADY_IN_USE", "EMAIL_EXISTS", "17007") { _, _ -> AuthError.EmailAlreadyInUse() }
    register("WEAK_PASSWORD", "17026") { _, _ -> AuthError.WeakPassword() }
    register("INVALID_EMAIL", "17008") { _, _ -> AuthError.InvalidEmail() }
    register(
        "INVALID_ACTION_CODE",
        "EXPIRED_ACTION_CODE",
        "INVALID_OOB_CODE",
        "EXPIRED_OOB_CODE",
        "17029",
        "17030",
    ) { _, _ -> AuthError.InvalidResetCode() }
    register("TOO_MANY_REQUESTS", "TOO_MANY_ATTEMPTS_TRY_LATER", "17010") { _, _ -> AuthError.TooManyRequests() }
    register("USER_DISABLED", "17005") { _, _ -> AuthError.UserDisabled() }
    register("OPERATION_NOT_ALLOWED", "17006") { _, _ -> AuthError.OperationNotAllowed() }
    register("NETWORK_REQUEST_FAILED", "NETWORK_ERROR", "17020") { message, cause ->
        AuthError.NetworkError(message, cause)
    }
    register("INVALID_PHONE_NUMBER", "17042") { _, _ -> AuthError.PhoneNumberInvalid() }
    register("INVALID_VERIFICATION_CODE", "17044") { _, _ -> AuthError.InvalidVerificationCode() }
    register("ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL", "17012") { _, _ ->
        AuthError.AccountExistsWithDifferentCredential()
    }
    register("CREDENTIAL_ALREADY_IN_USE", "FEDERATED_USER_ID_ALREADY_LINKED", "17025") { _, _ ->
        AuthError.CredentialAlreadyInUse()
    }
    register("REQUIRES_RECENT_LOGIN", "CREDENTIAL_TOO_OLD_LOGIN_AGAIN", "17014") { _, _ ->
        AuthError.RequiresRecentLogin()
    }
    register("SESSION_EXPIRED", "CODE_EXPIRED", "17051") { _, _ -> AuthError.VerificationCodeExpired() }
    register("QUOTA_EXCEEDED", "17052") { _, _ -> AuthError.QuotaExceeded() }
    register(
        "WEB_CONTEXT_CANCELED",
        "WEB_CONTEXT_CANCELLED",
        "POPUP_CLOSED_BY_USER",
        "CANCELLED_POPUP_REQUEST",
        "17058",
    ) { _, _ -> AuthError.SignInCancelled() }
    // Developer configuration: not actionable for the end user, but it must not read as "unexpected".
    register("APP_NOT_AUTHORIZED", "17028", "INVALID_API_KEY", "17023", "CONFIGURATION_NOT_FOUND") { _, _ ->
        AuthError.ProviderNotConfigured()
    }
}

/**
 * Maps a Firebase error message string to a domain [AuthError].
 * Extracted as a pure function for testability.
 *
 * Handles both Firebase REST API codes (e.g. `INVALID_CREDENTIAL`) and
 * native Android SDK codes (e.g. `ERROR_WRONG_PASSWORD`) and web SDK codes
 * (e.g. `wrong-password`).
 *
 * The rules for the spec 013 error names come first, so no generic rule (`INVALID_CREDENTIAL`,
 * `NETWORK`) swallows them. [AuthError.Unknown] and [AuthError.NetworkError] keep [cause].
 */
internal fun mapFirebaseErrorMessage(errorMessage: String, cause: Throwable? = null): AuthError = when {
    errorMessage.containsAny(
        "ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL",
        "account-exists-with-different-credential",
    ) -> AuthError.AccountExistsWithDifferentCredential()

    errorMessage.containsAny(
        "CREDENTIAL_ALREADY_IN_USE",
        "credential-already-in-use",
        "FEDERATED_USER_ID_ALREADY_LINKED",
    ) -> AuthError.CredentialAlreadyInUse()

    errorMessage.containsAny(
        "REQUIRES_RECENT_LOGIN",
        "requires-recent-login",
        "CREDENTIAL_TOO_OLD_LOGIN_AGAIN",
    ) -> AuthError.RequiresRecentLogin()

    errorMessage.containsAny(
        "ERROR_SESSION_EXPIRED",
        "code-expired",
    ) -> AuthError.VerificationCodeExpired()

    errorMessage.containsAny(
        "QUOTA_EXCEEDED",
        "quota-exceeded",
    ) -> AuthError.QuotaExceeded()

    errorMessage.containsAny(
        "WEB_CONTEXT_CANCELED",
        "WEB_CONTEXT_CANCELLED",
        "web-context-cancelled",
        "popup-closed-by-user",
    ) -> AuthError.SignInCancelled()

    // An API key restricted to other apps, or not valid at all, surfaces as an internal error.
    errorMessage.containsAny(
        "are blocked",
        "API key not valid",
    ) -> AuthError.ProviderNotConfigured()

    // Unified credential error (Firebase REST API since 2023) + legacy codes
    errorMessage.containsAny(
        "INVALID_CREDENTIAL",
        "ERROR_INVALID_CREDENTIAL",
        "INVALID_LOGIN_CREDENTIALS",
        "INVALID_PASSWORD",
        "wrong-password",
        "ERROR_WRONG_PASSWORD",
    ) -> AuthError.InvalidCredentials()

    errorMessage.containsAny(
        "USER_NOT_FOUND",
        "user-not-found",
        "ERROR_USER_NOT_FOUND",
    ) -> AuthError.UserNotFound()

    errorMessage.containsAny(
        "EMAIL_EXISTS",
        "email-already-in-use",
        "ERROR_EMAIL_ALREADY_IN_USE",
    ) -> AuthError.EmailAlreadyInUse()

    errorMessage.containsAny(
        "WEAK_PASSWORD",
        "weak-password",
        "ERROR_WEAK_PASSWORD",
    ) -> AuthError.WeakPassword()

    errorMessage.containsAny(
        "INVALID_EMAIL",
        "invalid-email",
        "ERROR_INVALID_EMAIL",
    ) -> AuthError.InvalidEmail()

    errorMessage.containsAny(
        "EXPIRED_OOB_CODE",
        "expired-action-code",
        "INVALID_OOB_CODE",
        "invalid-action-code",
    ) -> AuthError.InvalidResetCode()

    errorMessage.containsAny(
        "TOO_MANY_ATTEMPTS",
        "too-many-requests",
        "ERROR_TOO_MANY_REQUESTS",
    ) -> AuthError.TooManyRequests()

    errorMessage.containsAny(
        "USER_DISABLED",
        "user-disabled",
        "ERROR_USER_DISABLED",
    ) -> AuthError.UserDisabled()

    errorMessage.containsAny(
        "OPERATION_NOT_ALLOWED",
        "operation-not-allowed",
        "ERROR_OPERATION_NOT_ALLOWED",
    ) -> AuthError.OperationNotAllowed()

    errorMessage.containsAny(
        "NETWORK",
        "network-request-failed",
        "ERROR_NETWORK_REQUEST_FAILED",
    ) -> AuthError.NetworkError(errorMessage, cause)

    errorMessage.containsAny(
        "INVALID_PHONE_NUMBER",
        "invalid-phone-number",
        "ERROR_INVALID_PHONE_NUMBER",
    ) -> AuthError.PhoneNumberInvalid()

    errorMessage.containsAny(
        "INVALID_VERIFICATION_CODE",
        "invalid-verification-code",
        "ERROR_INVALID_VERIFICATION_CODE",
    ) -> AuthError.InvalidVerificationCode()

    else -> AuthError.Unknown(errorMessage, cause)
}

private fun String.containsAny(vararg values: String): Boolean = values.any { this.contains(it, ignoreCase = true) }
