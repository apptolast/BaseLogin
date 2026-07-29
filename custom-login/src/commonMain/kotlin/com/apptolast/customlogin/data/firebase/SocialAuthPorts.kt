package com.apptolast.customlogin.data.firebase

import com.apptolast.customlogin.SocialTokenResult
import com.apptolast.customlogin.clearSocialSignInState
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.getSocialIdToken

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
