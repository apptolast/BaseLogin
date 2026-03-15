package com.apptolast.customlogin.provider

import com.apptolast.customlogin.util.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * iOS implementation of Microsoft Sign-In via Firebase's OAuthProvider web flow.
 *
 * The hosting app must set [signInHandler] from Swift before Microsoft Sign-In is triggered.
 * Swift handles the entire OAuth flow using Firebase's OAuthProvider, then calls the
 * Kotlin callback with [com.apptolast.customlogin.PLATFORM_AUTH_HANDLED] on success.
 *
 * ## Swift setup example
 * ```swift
 * import FirebaseAuth
 *
 * MicrosoftSignInProviderIOS.companion.signInHandler = { _, completion in
 *     let provider = OAuthProvider(providerID: "microsoft.com")
 *     provider.scopes = ["email", "profile"]
 *     // Optional: force a specific tenant
 *     // provider.customParameters = ["tenant": "your-tenant-id"]
 *
 *     provider.getCredentialWith(nil) { credential, error in
 *         guard let credential = credential else {
 *             completion(nil); return
 *         }
 *         Auth.auth().signIn(with: credential) { result, error in
 *             if result?.user != nil {
 *                 completion(MicrosoftSignInProviderIOS.companion.PLATFORM_AUTH_HANDLED)
 *             } else {
 *                 completion(nil)
 *             }
 *         }
 *     }
 * }
 * ```
 */
object MicrosoftSignInProviderIOS {

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
                "MicrosoftSignIn",
                "signInHandler not configured. Set MicrosoftSignInProviderIOS.signInHandler from Swift."
            )
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        handler(null) { result ->
            if (cont.isActive) cont.resume(result)
        }
    }
}