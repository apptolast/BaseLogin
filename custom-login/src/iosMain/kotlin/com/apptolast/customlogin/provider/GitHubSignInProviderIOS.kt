package com.apptolast.customlogin.provider

import com.apptolast.customlogin.util.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * iOS implementation of GitHub Sign-In via Firebase's OAuthProvider web flow.
 *
 * The hosting app must set [signInHandler] from Swift before GitHub Sign-In is triggered.
 * Swift handles the entire OAuth flow using Firebase's OAuthProvider, then calls the
 * Kotlin callback with [com.apptolast.customlogin.PLATFORM_AUTH_HANDLED] on success.
 *
 * ## Swift setup example
 * ```swift
 * import FirebaseAuth
 *
 * GitHubSignInProviderIOS.companion.signInHandler = { _, completion in
 *     let provider = OAuthProvider(providerID: "github.com")
 *     provider.scopes = ["user:email"]
 *
 *     provider.getCredentialWith(nil) { credential, error in
 *         guard let credential = credential else {
 *             completion(nil); return
 *         }
 *         Auth.auth().signIn(with: credential) { result, error in
 *             if result?.user != nil {
 *                 completion(GitHubSignInProviderIOS.companion.PLATFORM_AUTH_HANDLED)
 *             } else {
 *                 completion(nil)
 *             }
 *         }
 *     }
 * }
 * ```
 */
object GitHubSignInProviderIOS {

    const val PLATFORM_AUTH_HANDLED = com.apptolast.customlogin.PLATFORM_AUTH_HANDLED

    /**
     * Set from Swift. Call the completion with [PLATFORM_AUTH_HANDLED] on success, or `null` on
     * failure/cancellation.
     */
    var signInHandler: ((String?, (String?) -> Unit) -> Unit)? = null

    suspend fun signIn(): String? = suspendCancellableCoroutine { cont ->
        val handler = signInHandler
        if (handler == null) {
            Logger.w(
                "GitHubSignIn",
                "signInHandler not configured. Set GitHubSignInProviderIOS.signInHandler from Swift."
            )
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        handler(null) { result ->
            if (cont.isActive) cont.resume(result)
        }
    }
}