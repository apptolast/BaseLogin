package com.apptolast.customlogin.data.firebase

import com.apptolast.customlogin.SocialTokenResult
import com.apptolast.customlogin.clearSocialSignInState
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.getSocialIdToken
import com.apptolast.customlogin.revokeSocialToken

/**
 * Obtains a social sign-in token from the platform.
 *
 * Exists as an interface for one reason: `getSocialIdToken` is a top-level `expect` function, and
 * those cannot be faked from `commonTest`. Without this port the whole OAuth path of
 * `FirebaseAuthProvider` — Google, Apple, GitHub, Microsoft, Twitter and Facebook — would stay
 * untestable even after introducing [FirebaseAuthGateway].
 */
interface SocialTokenProvider {
    suspend fun tokenFor(provider: IdentityProvider): SocialTokenResult?
}

/** Production implementation: delegates to the platform's `actual`. */
class PlatformSocialTokenProvider : SocialTokenProvider {
    override suspend fun tokenFor(provider: IdentityProvider): SocialTokenResult? = getSocialIdToken(provider)
}

/**
 * Clears the platform's cached social sign-in state on sign-out.
 *
 * Deliberately a separate port from [FirebaseAuthGateway]: what has to be cleared is Credential
 * Manager's cached Google credential, which belongs to Google Sign-In and not to Firebase Auth.
 * Folding it into the Firebase port would blur that boundary.
 *
 * Same testability reason as [SocialTokenProvider]: the underlying call is a top-level `expect`.
 */
interface SocialSignInStateCleaner {
    suspend fun clear()
}

/** Production implementation: delegates to the platform's `actual`. */
class PlatformSocialSignInStateCleaner : SocialSignInStateCleaner {
    override suspend fun clear() = clearSocialSignInState()
}

/**
 * Revokes the token a social provider issued, before the account is deleted.
 *
 * Only Apple requires this, and only on iOS: App Review guideline 5.1.1(v) asks for the token to be
 * revoked, not just the Firebase user removed. An app that skips it keeps showing up under
 * *Settings → Apple Account → Sign in with Apple* after the user deleted their account.
 *
 * Third port of the same family as [SocialTokenProvider] and [SocialSignInStateCleaner], and for the
 * same reason: the call underneath is a top-level `expect` function, which cannot be faked from
 * `commonTest`.
 */
interface SocialTokenRevoker {
    /**
     * Best effort by contract: implementations may fail, and the caller deletes the account anyway.
     * A user who asked to delete their account must never be stuck because a remote server is down.
     */
    suspend fun revoke(provider: IdentityProvider)
}

/** Production implementation: delegates to the platform's `actual`. */
class PlatformSocialTokenRevoker : SocialTokenRevoker {
    override suspend fun revoke(provider: IdentityProvider) = revokeSocialToken(provider)
}
