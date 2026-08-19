package com.apptolast.customlogin.provider

import com.apptolast.customlogin.util.Logger
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * iOS implementation of Apple Sign-In via AuthenticationServices.
 *
 * Uses a callback mechanism to integrate with Swift. The hosting app must set [signInHandler] from
 * Swift, at startup and before the first Composable renders, or Apple Sign-In resolves to `null`
 * and the user sees a generic "cancelled or failed" error.
 *
 * This is a Kotlin `object`, so from Swift it is reached through **`.shared`** —
 * `AppleSignInProviderIOS.shared.signInHandler = …`. (`.companion` exists only for the companion
 * object of a class, such as `GoogleSignInProviderIOS`.)
 *
 * ## Token format
 *
 * Call the completion with one of these, and nothing else:
 *
 * - `"<idToken>|||rawNonce|||<rawNonce>"` — signed in, with replay protection.
 * - `"<idToken>|||rawNonce|||<rawNonce>|||displayName|||<name>"` — same, plus the name Apple sent.
 * - `"<idToken>"` — no nonce. Accepted for backwards compatibility, **not** fit for production.
 * - `null` — cancelled or failed.
 *
 * The `displayName` segment is optional and **appended**, never a replacement: hosts already wired
 * against the two-segment form keep working untouched.
 *
 * ### Why the nonce is not optional in practice
 *
 * Apple receives the SHA-256 of the nonce in the request; Firebase receives the raw one here and
 * checks that both match. Without it, an identity token intercepted from another app can be
 * replayed against your Firebase project. Generate it with a CSPRNG per attempt and never reuse it.
 *
 * ### Why the name has to travel with the token
 *
 * Apple returns `fullName` **only on the very first authorisation** of each user; every later
 * sign-in comes back with it empty. If it is not sent here, the library cannot persist it into the
 * Firebase profile and it is lost for good — re-installing the app does not bring it back, the user
 * has to revoke the app in *Settings → Apple Account → Sign in with Apple*.
 *
 * ## Swift setup
 *
 * The reference implementation lives in the demo app, in `iosApp/iosApp/AppleSignInCoordinator.swift`;
 * copy it as-is. In short:
 *
 * ```swift
 * // AppDelegate.application(_:didFinishLaunchingWithOptions:)
 * AppleSignInProviderIOS.shared.signInHandler = { [weak self] _, completion in
 *     self?.startAppleSignIn(completion: completion)   // runs ASAuthorizationController
 * }
 *
 * // ASAuthorizationControllerDelegate, on success:
 * var packed = "\(idToken)|||rawNonce|||\(rawNonce)"
 * if let name = displayName(from: credential.fullName) {
 *     packed += "|||displayName|||\(name)"
 * }
 * completion(packed)
 * ```
 *
 * Three things that are easy to get wrong and only fail at runtime:
 * - keep a strong reference to the `ASAuthorizationController`, or it is deallocated before the
 *   delegate fires and no sheet ever appears;
 * - call `performRequests()` on the main thread;
 * - call the completion exactly once on every path, including cancellation — otherwise the
 *   coroutine below never resumes and the button stays spinning forever.
 *
 * Requires the **Sign in with Apple** capability in the app's entitlements and the Apple provider
 * enabled in the Firebase console.
 */
object AppleSignInProviderIOS {

    /**
     * Set from Swift (as `AppleSignInProviderIOS.shared.signInHandler`) to perform the native
     * Apple Sign-In.
     *
     * The first parameter carries the **requested scopes**, comma-separated, straight from
     * `AppleSignInConfig.scopes` — `"email,name"` by default. Map `email` to `.email` and `name` to
     * `.fullName`; treat a blank string as "both", which is what a host that ignores this parameter
     * effectively did before it carried anything.
     *
     * Call the completion with the packed token string on success, or `null` on failure.
     */
    var signInHandler: ((String?, (String?) -> Unit) -> Unit)? = null

    /**
     * @param scopes comma-separated scopes for the handler. Defaults to `null`, so a caller that
     *   does not care keeps compiling — the library itself always passes what the host configured.
     */
    suspend fun signIn(scopes: String? = null): String? = suspendCancellableCoroutine { cont ->
        val handler = signInHandler
        if (handler == null) {
            Logger.w(
                "AppleSignIn",
                "signInHandler not configured. Set AppleSignInProviderIOS.shared.signInHandler from Swift.",
            )
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        // A host that calls the completion more than once (cancel racing with error, say) must not
        // crash the app: the second call finds the continuation already resumed and is dropped.
        handler(scopes) { tokenData ->
            if (cont.isActive) {
                cont.resume(tokenData)
            }
        }
    }

    /**
     * Set from Swift (as `AppleSignInProviderIOS.shared.revokeHandler`) to revoke the Apple token
     * before the account is deleted, which App Review guideline 5.1.1(v) requires.
     *
     * `Auth.auth().revokeToken(withAuthorizationCode:)` needs an authorization code that is **fresh
     * (under five minutes) and single-use**, so the handler has to ask Apple to authorise again and
     * use the code from that pass — which also satisfies the recent-login requirement Firebase puts
     * on deleting a user. See `iosApp/iosApp/AppleSignInCoordinator.swift`.
     *
     * Call the completion with `null` when the token was revoked, or with a message describing the
     * failure. **Failing is allowed**: the account is deleted either way, because a user who asked
     * to delete it must not be stuck behind a server that will not answer.
     *
     * Leaving this unset is a supported choice: revocation is then skipped with a warning, and hosts
     * that already ship an account-deletion flow do not get an Apple sheet they never asked for.
     */
    var revokeHandler: (((String?) -> Unit) -> Unit)? = null

    /** @return `null` if the token was revoked, or a message describing the failure. */
    suspend fun revoke(): String? = suspendCancellableCoroutine { cont ->
        val handler = revokeHandler
        if (handler == null) {
            cont.resume("revokeHandler not configured. Set AppleSignInProviderIOS.shared.revokeHandler from Swift.")
            return@suspendCancellableCoroutine
        }

        handler { error ->
            if (cont.isActive) {
                cont.resume(error)
            }
        }
    }
}
