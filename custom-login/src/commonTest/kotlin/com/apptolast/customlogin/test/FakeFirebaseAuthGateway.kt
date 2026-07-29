package com.apptolast.customlogin.test

import com.apptolast.customlogin.SocialTokenResult
import com.apptolast.customlogin.data.firebase.FirebaseAuthCredential
import com.apptolast.customlogin.data.firebase.FirebaseAuthFailure
import com.apptolast.customlogin.data.firebase.FirebaseAuthGateway
import com.apptolast.customlogin.data.firebase.FirebaseAuthUser
import com.apptolast.customlogin.data.firebase.SocialSignInStateCleaner
import com.apptolast.customlogin.data.firebase.SocialTokenProvider
import com.apptolast.customlogin.domain.model.IdentityProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Hand-written [FirebaseAuthGateway] double.
 *
 * This class is the reason FLE-90 exists: `FirebaseAuth` is a platform `expect` type that cannot be
 * faked, so before the port there was no way to exercise `FirebaseAuthProvider` at all.
 *
 * Configure outcomes with the `var`s, assert against the recorded interactions.
 */
class FakeFirebaseAuthGateway : FirebaseAuthGateway {

    // ── Configuration ─────────────────────────────────────────────────────────

    var user: FirebaseAuthUser? = null

    /** When set, every suspending operation throws it. */
    var failWith: FirebaseAuthFailure? = null

    var idToken: String? = "fake-id-token"

    // ── Recorded interactions ─────────────────────────────────────────────────

    val credentials = mutableListOf<FirebaseAuthCredential>()
    val updatedDisplayNames = mutableListOf<String>()
    var signUpCalls = 0
    var signOutCalls = 0
    var getIdTokenCalls = 0
    var passwordResetEmails = mutableListOf<String>()

    /** Every call that reaches the SDK, to assert "the graph was built without touching Firebase". */
    var totalInteractions = 0
        private set

    private val authState = MutableStateFlow<FirebaseAuthUser?>(null)

    fun emitAuthState(next: FirebaseAuthUser?) {
        authState.value = next
    }

    private fun record() {
        totalInteractions++
        failWith?.let { throw it }
    }

    // ── FirebaseAuthGateway ───────────────────────────────────────────────────

    override val currentUser: FirebaseAuthUser?
        get() {
            totalInteractions++
            return user
        }

    override val authStateChanged: Flow<FirebaseAuthUser?> get() = authState

    override suspend fun signIn(credential: FirebaseAuthCredential): FirebaseAuthUser {
        record()
        credentials += credential
        return user ?: FirebaseAuthUser(uid = "fake-uid").also { user = it }
    }

    override suspend fun signUp(email: String, password: String): FirebaseAuthUser {
        record()
        signUpCalls++
        credentials += FirebaseAuthCredential.EmailPassword(email, password)
        return user ?: FirebaseAuthUser(uid = "fake-uid", email = email).also { user = it }
    }

    override suspend fun signOut() {
        record()
        signOutCalls++
        user = null
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        record()
        passwordResetEmails += email
    }

    override suspend fun confirmPasswordReset(code: String, newPassword: String) = record()

    override suspend fun sendSignInLinkToEmail(email: String, continueUrl: String, iosBundleId: String?) = record()

    override suspend fun signInWithEmailLink(email: String, link: String): FirebaseAuthUser {
        record()
        return user ?: FirebaseAuthUser(uid = "fake-uid", email = email).also { user = it }
    }

    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        record()
        getIdTokenCalls++
        return idToken
    }

    override suspend fun updateDisplayName(displayName: String) {
        record()
        updatedDisplayNames += displayName
        user = user?.copy(displayName = displayName)
    }

    override suspend fun updateEmail(newEmail: String) = record()

    override suspend fun updatePassword(newPassword: String) = record()

    override suspend fun sendEmailVerification() = record()

    override suspend fun deleteCurrentUser() {
        record()
        user = null
    }

    override suspend fun reauthenticate(credential: FirebaseAuthCredential) {
        record()
        credentials += credential
    }
}

/** [SocialSignInStateCleaner] double, to assert that sign-out clears the platform state. */
class FakeSocialSignInStateCleaner : SocialSignInStateCleaner {
    var clearCalls = 0
        private set

    override suspend fun clear() {
        clearCalls++
    }
}

/**
 * [SocialTokenProvider] double.
 *
 * [tokenByProvider] lets a test hand back the exact packed string the platform would produce, which
 * is what the Apple displayName and legacy-format cases are about.
 */
class FakeSocialTokenProvider : SocialTokenProvider {
    val tokenByProvider = mutableMapOf<String, SocialTokenResult?>()
    val requested = mutableListOf<IdentityProvider>()

    fun returnsToken(provider: IdentityProvider, packed: String) {
        tokenByProvider[provider.id] = SocialTokenResult.Token(packed)
    }

    fun returnsCancelled(provider: IdentityProvider) {
        tokenByProvider[provider.id] = null
    }

    override suspend fun tokenFor(provider: IdentityProvider): SocialTokenResult? {
        requested += provider
        return tokenByProvider[provider.id]
    }
}
