package com.apptolast.customlogin.data.firebase

import kotlinx.coroutines.flow.Flow

/**
 * Thin adapter over the GitLive Firebase SDK. The **only** file in `commonMain` allowed to import
 * `dev.gitlive.firebase.auth.*`.
 *
 * Two invariants worth stating, because both have bitten this codebase before:
 *
 * 1. **Lazy resolution.** `Firebase.auth` throws if it is touched before `FirebaseApp` has been
 *    initialised, and Koin constructs this object while building the graph — well before the host
 *    app configures Firebase. Nothing may resolve the SDK in the constructor.
 * 2. **Every throwable becomes [FirebaseAuthFailure], carrying the original message.** Catching only
 *    `FirebaseAuthException` is not enough: `FirebaseNetworkException` and
 *    `FirebaseTooManyRequestsException` extend `FirebaseException` and are its *siblings*, not its
 *    subclasses, so they slip through and end up mis-mapped as `AuthError.Unknown`.
 */
class GitLiveFirebaseAuthGateway : FirebaseAuthGateway {

    override val currentUser: FirebaseAuthUser?
        get() = TODO("FLE-90: map Firebase.auth.currentUser to FirebaseAuthUser")

    override val authStateChanged: Flow<FirebaseAuthUser?>
        get() = TODO("FLE-90: map Firebase.auth.authStateChanged")

    override suspend fun signIn(credential: FirebaseAuthCredential): FirebaseAuthUser =
        TODO("FLE-90: build the SDK credential and call signInWithCredential")

    override suspend fun signUp(email: String, password: String): FirebaseAuthUser =
        TODO("FLE-90: createUserWithEmailAndPassword")

    override suspend fun signOut(): Unit = TODO("FLE-90: Firebase.auth.signOut()")

    override suspend fun sendPasswordResetEmail(email: String): Unit =
        TODO("FLE-90: Firebase.auth.sendPasswordResetEmail")

    override suspend fun confirmPasswordReset(code: String, newPassword: String): Unit =
        TODO("FLE-90: Firebase.auth.confirmPasswordReset")

    override suspend fun sendSignInLinkToEmail(email: String, continueUrl: String, iosBundleId: String?): Unit =
        TODO("FLE-90: ActionCodeSettings + sendSignInLinkToEmail")

    override suspend fun signInWithEmailLink(email: String, link: String): FirebaseAuthUser =
        TODO("FLE-90: Firebase.auth.signInWithEmailLink")

    override suspend fun getIdToken(forceRefresh: Boolean): String? =
        TODO("FLE-90: currentUser?.getIdToken(forceRefresh)")

    override suspend fun updateDisplayName(displayName: String): Unit =
        TODO("FLE-90: currentUser.updateProfile(displayName)")

    override suspend fun updateEmail(newEmail: String): Unit = TODO("FLE-90: currentUser.verifyBeforeUpdateEmail")

    override suspend fun updatePassword(newPassword: String): Unit = TODO("FLE-90: currentUser.updatePassword")

    override suspend fun sendEmailVerification(): Unit = TODO("FLE-90: currentUser.sendEmailVerification")

    override suspend fun deleteCurrentUser(): Unit = TODO("FLE-90: currentUser.delete()")

    override suspend fun reauthenticate(credential: FirebaseAuthCredential): Unit =
        TODO("FLE-90: currentUser.reauthenticate")
}
