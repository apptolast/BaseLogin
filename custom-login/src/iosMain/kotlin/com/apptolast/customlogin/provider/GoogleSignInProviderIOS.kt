package com.apptolast.customlogin.provider

import com.apptolast.customlogin.domain.model.GoogleSignInConfig
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import kotlin.coroutines.resume

/**
 * iOS implementation of Google Sign-In.
 *
 * This provider uses a callback mechanism to integrate with Swift.
 * The hosting app should:
 * 1. Configure GoogleSignIn in Swift AppDelegate
 * 2. Call [signInFromSwift] to trigger the sign-in flow
 * 3. The result will be passed back via the callback
 *
 * @property config The Google Sign-In configuration containing client IDs.
 */
class GoogleSignInProviderIOS(
    private val config: GoogleSignInConfig
) {
    companion object {
        /**
         * Callback to be set from Swift to perform the actual sign-in.
         * Swift should set this and call GIDSignIn.sharedInstance.signIn().
         */
        var signInHandler: ((String?, (String?) -> Unit) -> Unit)? = null

        /**
         * Called from Swift to complete the sign-in with the ID token.
         */
        private var pendingCallback: ((String?) -> Unit)? = null

        /**
         * Called from Swift to provide the sign-in result.
         */
        fun onSignInResult(idToken: String?) {
            pendingCallback?.invoke(idToken)
            pendingCallback = null
        }
    }

    /**
     * Initiates the Google Sign-In flow and returns the ID token.
     *
     * @return The Google ID token on success, or null if cancelled/failed.
     */
    @OptIn(ExperimentalForeignApi::class)
    suspend fun signIn(): String? = suspendCancellableCoroutine { continuation ->
        val handler = signInHandler
        if (handler == null) {
            println("Google Sign-In handler not configured. Set GoogleSignInProviderIOS.signInHandler from Swift.")
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val clientId = config.iosClientId ?: config.webClientId

        // Set up continuation callback
        pendingCallback = { token ->
            if (continuation.isActive) {
                continuation.resume(token)
            }
        }

        continuation.invokeOnCancellation {
            pendingCallback = null
        }

        // Call Swift handler with the client ID
        handler(clientId) { token ->
            if (continuation.isActive) {
                continuation.resume(token)
            }
            pendingCallback = null
        }
    }

    /**
     * Gets the top-most view controller for presenting the sign-in UI.
     */
    @OptIn(ExperimentalForeignApi::class)
    fun getTopViewController(): UIViewController? {
        val keyWindow = UIApplication.sharedApplication.windows
            .filterIsInstance<UIWindow>()
            .firstOrNull { it.isKeyWindow() }

        var topController = keyWindow?.rootViewController
        while (topController?.presentedViewController != null) {
            topController = topController.presentedViewController
        }
        return topController
    }
}
