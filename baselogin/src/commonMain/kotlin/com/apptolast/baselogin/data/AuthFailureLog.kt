package com.apptolast.baselogin.data

import com.apptolast.baselogin.domain.model.AuthError

/**
 * Builds the log line written for every failed auth operation.
 *
 * Deliberately never receives the SDK message: on iOS it is `NSError.toString()`, which dumps the
 * whole `userInfo` — including `FIRAuthErrorUserInfoEmailKey` on account collisions. Only the
 * [code], the [exceptionClass] and the resulting [error] type are logged.
 */
internal fun authFailureLogLine(code: String?, exceptionClass: String?, error: AuthError): String =
    "auth failure: code=${code ?: "none"} exception=${exceptionClass ?: "none"} -> ${error.typeName()}"

/**
 * Builds the log line written when the platform returns no social token for [providerId], so a real
 * failure on iOS (where the Swift handlers cannot tell it apart from a cancellation) stays visible.
 */
internal fun nullSocialTokenLogLine(providerId: String): String =
    "social token null for $providerId -> ${AuthError.SignInCancelled::class.simpleName}"

/** The variant name only: [AuthError.toString] would print the message, which may hold PII. */
private fun AuthError.typeName(): String = this::class.simpleName ?: "AuthError"
