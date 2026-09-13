package com.apptolast.baselogin.data.firebase

import com.apptolast.baselogin.data.mapFirebaseError
import com.apptolast.baselogin.domain.model.AuthError
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException

/**
 * The Firebase error code of a failure raised by the native Android SDK, outside the GitLive gateway
 * (web OAuth, phone auth).
 *
 * The Android message is human text with no code in it, so without this those failures could only
 * ever be `AuthError.Unknown`. `FirebaseNetworkException` and `FirebaseTooManyRequestsException` are
 * siblings of `FirebaseAuthException` and carry no code: they get the same synthetic one the gateway
 * uses.
 */
internal fun Throwable.firebaseErrorCode(): String? = when (this) {
    is FirebaseAuthException -> errorCode
    is FirebaseNetworkException -> "ERROR_NETWORK_REQUEST_FAILED"
    is FirebaseTooManyRequestsException -> "ERROR_TOO_MANY_REQUESTS"
    else -> null
}

/** Classifies a native Android failure with the same common mapper the gateway failures go through. */
internal fun Throwable.toAuthError(fallbackMessage: String): AuthError =
    mapFirebaseError(firebaseErrorCode(), message ?: fallbackMessage, this)
