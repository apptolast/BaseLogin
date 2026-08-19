package com.apptolast.baselogin.provider

import com.apptolast.baselogin.util.Logger
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * iOS implementation of Facebook Sign-In via Firebase's OAuthProvider web flow.
 *
 * The hosting app must set [signInHandler] from Swift before Facebook Sign-In is triggered.
 * Swift handles the entire OAuth flow using Firebase's OAuthProvider, then calls the
 * Kotlin callback with [com.apptolast.baselogin.PLATFORM_AUTH_HANDLED] on success.
 *
 * ## Swift setup example
 * ```swift
 * import FirebaseAuth
 *
 * FacebookSignInProviderIOS.shared.signInHandler = { _, completion in
 *     let provider = OAuthProvider(providerID: "facebook.com")
 *     provider.scopes = ["email", "public_profile"]
 *
 *     provider.getCredentialWith(nil) { credential, error in
 *         guard let credential = credential else {
 *             completion(nil); return
 *         }
 *         Auth.auth().signIn(with: credential) { result, error in
 *             if result?.user != nil {
 *                 completion(FacebookSignInProviderIOS.shared.PLATFORM_AUTH_HANDLED)
 *             } else {
 *                 completion(nil)
 *             }
 *         }
 *     }
 * }
 * ```
 */
object FacebookSignInProviderIOS {

    const val PLATFORM_AUTH_HANDLED = com.apptolast.baselogin.PLATFORM_AUTH_HANDLED

    /**
     * Set from Swift. Call the completion with [PLATFORM_AUTH_HANDLED] on success, or `null` on
     * failure/cancellation.
     */
    var signInHandler: ((String?, (String?) -> Unit) -> Unit)? = null

    suspend fun signIn(): String? = suspendCancellableCoroutine { cont ->
        val handler = signInHandler
        if (handler == null) {
            Logger.w(
                "FacebookSignIn",
                "signInHandler not configured. Set FacebookSignInProviderIOS.signInHandler from Swift.",
            )
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        handler(null) { result ->
            if (cont.isActive) cont.resume(result)
        }
    }
}
