package com.apptolast.customlogin.data.firebase

import kotlinx.coroutines.flow.Flow

/**
 * Port over Firebase Authentication.
 *
 * No `dev.gitlive.*` type crosses this boundary, and that is the whole point: `FirebaseAuth` is a
 * platform `expect` class that cannot be faked from `commonTest`, so any class taking it directly is
 * untestable by construction. Keeping the SDK behind this port is what lets `FirebaseAuthProvider`
 * be exercised with a hand-written fake.
 *
 * Verifiable rule: `commonTest` must never import `dev.gitlive.*`.
 *
 * Phone authentication deliberately stays out of this port: it is handled by the
 * `sendPhoneVerificationCode` / `verifyPhoneCode` expect/actual pair, which talks to the native SDK
 * directly because it needs an Activity on Android and APNS registration on iOS.
 */
interface FirebaseAuthGateway {

    /**
     * The signed-in user read from the SDK's local cache, or `null`.
     *
     * Reading this must not perform network I/O — [AuthProvider.getCurrentSession] relies on that.
     */
    val currentUser: FirebaseAuthUser?

    /** Emits on every sign-in / sign-out, starting with the current value. */
    val authStateChanged: Flow<FirebaseAuthUser?>

    suspend fun signIn(credential: FirebaseAuthCredential): FirebaseAuthUser

    suspend fun signUp(email: String, password: String): FirebaseAuthUser

    suspend fun signOut()

    suspend fun sendPasswordResetEmail(email: String)

    suspend fun confirmPasswordReset(code: String, newPassword: String)

    suspend fun sendSignInLinkToEmail(email: String, continueUrl: String, iosBundleId: String?)

    suspend fun signInWithEmailLink(email: String, link: String): FirebaseAuthUser

    // ── Operations on the current user ────────────────────────────────────────

    suspend fun getIdToken(forceRefresh: Boolean): String?

    suspend fun updateDisplayName(displayName: String)

    suspend fun updateEmail(newEmail: String)

    suspend fun updatePassword(newPassword: String)

    suspend fun sendEmailVerification()

    suspend fun deleteCurrentUser()

    suspend fun reauthenticate(credential: FirebaseAuthCredential)
}

/**
 * A Firebase user, flattened to plain data so it can be built in tests.
 *
 * Deliberately carries no token: obtaining one is a potentially networked operation and belongs to
 * [FirebaseAuthGateway.getIdToken].
 */
data class FirebaseAuthUser(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    /**
     * Provider ids Firebase has linked to this user (`apple.com`, `password`, `google.com`…).
     *
     * Deleting an account has to revoke the token of some of them, and which ones is a property of
     * the user, not of the app's configuration: an email-only user in an app that offers Apple
     * Sign-In must not be shown an Apple sheet on the way out.
     */
    val providerIds: List<String> = emptyList(),
)

/** A credential the gateway can sign in with, expressed without SDK types. */
sealed interface FirebaseAuthCredential {

    data class EmailPassword(val email: String, val password: String) : FirebaseAuthCredential

    /** Google needs both tokens on iOS; on Android the access token is absent. */
    data class Google(val idToken: String, val accessToken: String? = null) : FirebaseAuthCredential

    /** Apple, Microsoft and Twitter. Apple additionally requires [rawNonce] for replay protection. */
    data class OAuth(
        val providerId: String,
        val idToken: String? = null,
        val accessToken: String? = null,
        val rawNonce: String? = null,
    ) : FirebaseAuthCredential

    data class GitHub(val token: String) : FirebaseAuthCredential
}

/**
 * The only exception this port throws.
 *
 * It carries the SDK's original [message] untouched, because that string is what
 * `mapFirebaseErrorMessage` inspects to produce a typed `AuthError`. Do not normalise or translate
 * it here: the mapper already understands the three families of Firebase error strings (REST codes,
 * native Android codes and web SDK codes).
 */
class FirebaseAuthFailure(message: String, cause: Throwable? = null) : Exception(message, cause)
