package com.apptolast.customlogin.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.ActionCodeSettings
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GithubAuthProvider
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Thin adapter over the GitLive Firebase SDK. The **only** file in `commonMain` allowed to import
 * `dev.gitlive.firebase.auth.*`.
 *
 * Two invariants worth stating, because both have bitten this codebase before:
 *
 * 1. **Lazy resolution.** `Firebase.auth` throws if it is touched before `FirebaseApp` has been
 *    initialised, and Koin constructs this object while building the graph — well before the host
 *    app configures Firebase. Nothing may resolve the SDK in the constructor, hence [auth] being a
 *    getter rather than a field.
 * 2. **Every throwable becomes [FirebaseAuthFailure], carrying the original message.** Catching only
 *    `FirebaseAuthException` is not enough: `FirebaseNetworkException` and
 *    `FirebaseTooManyRequestsException` extend `FirebaseException` and are its *siblings*, not its
 *    subclasses, so they slip through and end up mis-mapped as `AuthError.Unknown`.
 */
class GitLiveFirebaseAuthGateway : FirebaseAuthGateway {

    private val auth: FirebaseAuth get() = Firebase.auth

    override val currentUser: FirebaseAuthUser?
        get() = auth.currentUser?.toFirebaseAuthUser()

    override val authStateChanged: Flow<FirebaseAuthUser?>
        get() = auth.authStateChanged.map { it?.toFirebaseAuthUser() }

    override suspend fun signIn(credential: FirebaseAuthCredential): FirebaseAuthUser = runGateway {
        val result = when (credential) {
            is FirebaseAuthCredential.EmailPassword ->
                auth.signInWithEmailAndPassword(credential.email, credential.password)
            else -> auth.signInWithCredential(credential.toSdkCredential())
        }
        result.user.requireUser("No user returned after sign in")
    }

    override suspend fun signUp(email: String, password: String): FirebaseAuthUser = runGateway {
        auth.createUserWithEmailAndPassword(email, password)
            .user
            .requireUser("No user returned after registration")
    }

    override suspend fun signOut() = runGateway { auth.signOut() }

    override suspend fun sendPasswordResetEmail(email: String) = runGateway { auth.sendPasswordResetEmail(email) }

    override suspend fun confirmPasswordReset(code: String, newPassword: String) =
        runGateway { auth.confirmPasswordReset(code, newPassword) }

    override suspend fun sendSignInLinkToEmail(email: String, continueUrl: String, iosBundleId: String?) = runGateway {
        val settings = ActionCodeSettings(url = continueUrl, canHandleCodeInApp = true, iOSBundleId = iosBundleId)
        auth.sendSignInLinkToEmail(email, settings)
    }

    override suspend fun signInWithEmailLink(email: String, link: String): FirebaseAuthUser = runGateway {
        auth.signInWithEmailLink(email, link)
            .user
            .requireUser("No user returned after magic link sign-in")
    }

    // ── Operations on the current user ────────────────────────────────────────

    override suspend fun getIdToken(forceRefresh: Boolean): String? =
        runGateway { auth.currentUser?.getIdToken(forceRefresh) }

    override suspend fun updateDisplayName(displayName: String) =
        runGateway { requireCurrentUser().updateProfile(displayName = displayName) }

    override suspend fun updateEmail(newEmail: String) =
        runGateway { requireCurrentUser().verifyBeforeUpdateEmail(newEmail) }

    override suspend fun updatePassword(newPassword: String) =
        runGateway { requireCurrentUser().updatePassword(newPassword) }

    override suspend fun sendEmailVerification() = runGateway { requireCurrentUser().sendEmailVerification() }

    override suspend fun deleteCurrentUser() = runGateway { requireCurrentUser().delete() }

    override suspend fun reauthenticate(credential: FirebaseAuthCredential) =
        runGateway { requireCurrentUser().reauthenticate(credential.toSdkCredential()) }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private fun FirebaseUser.toFirebaseAuthUser() = FirebaseAuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoURL,
        isEmailVerified = isEmailVerified,
        providerIds = providerData.map { it.providerId },
    )

    private fun FirebaseUser?.requireUser(message: String): FirebaseAuthUser =
        this?.toFirebaseAuthUser() ?: throw FirebaseAuthFailure(message)

    private fun requireCurrentUser(): FirebaseUser =
        auth.currentUser ?: throw FirebaseAuthFailure("No authenticated user")

    private fun FirebaseAuthCredential.toSdkCredential(): AuthCredential = when (this) {
        is FirebaseAuthCredential.EmailPassword -> EmailAuthProvider.credential(email, password)
        is FirebaseAuthCredential.Google -> GoogleAuthProvider.credential(idToken, accessToken)
        is FirebaseAuthCredential.GitHub -> GithubAuthProvider.credential(token)
        is FirebaseAuthCredential.OAuth -> OAuthProvider.credential(
            providerId = providerId,
            accessToken = accessToken,
            idToken = idToken,
            rawNonce = rawNonce,
        )
    }

    /**
     * Funnels every SDK throwable into [FirebaseAuthFailure] **without touching the message**: that
     * string is what `mapFirebaseErrorMessage` inspects to produce a typed `AuthError`.
     */
    private inline fun <T> runGateway(block: () -> T): T = try {
        block()
    } catch (e: FirebaseAuthFailure) {
        throw e
    } catch (e: Exception) {
        throw FirebaseAuthFailure(e.message ?: "Authentication error", e)
    }
}
