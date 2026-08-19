package com.apptolast.customlogin.provider

import com.apptolast.customlogin.config.GoogleSignInConfig
import com.apptolast.customlogin.util.Logger
import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

/**
 * iOS implementation of Google Sign-In.
 *
 * This provider uses a callback mechanism to integrate with Swift. The hosting app should:
 * 1. Configure GoogleSignIn in Swift AppDelegate
 * 2. Set [signInHandler] to trigger the sign-in flow
 * 3. The result travels back through the completion block handed to that handler
 *
 * An `object`, like the other five iOS providers, so there is a single way to reach any of them from
 * Swift: `GoogleSignInProviderIOS.shared.…`. It used to be a `class` taking [GoogleSignInConfig] in
 * its constructor, which Kotlin/Native exported through the companion instead and made this the odd
 * one out — the reason the published examples drifted into two contradictory forms. The config now
 * travels in [signIn], mirroring `AppleSignInProviderIOS.signIn(scopes)`.
 *
 * Making it an `object` does not globalise anything that was not global already: [signInHandler],
 * [signOutHandler] and the pending callback lived in the companion, so there was ever only one of
 * each per process. The old shape merely suggested otherwise.
 */
object GoogleSignInProviderIOS {

    /**
     * Callback to be set from Swift to perform the actual sign-in.
     * Swift should set this and call GIDSignIn.sharedInstance.signIn().
     */
    var signInHandler: ((String?, (String?) -> Unit) -> Unit)? = null

    /**
     * Set from Swift to clear GoogleSignIn's own session when the user signs out:
     *
     * ```swift
     * GoogleSignInProviderIOS.shared.signOutHandler = {
     *     GIDSignIn.sharedInstance.signOut()
     * }
     * ```
     *
     * Firebase's `signOut()` does not touch it. `GIDSignIn.sharedInstance.currentUser` lives in
     * the keychain and survives, so without this the next Google sign-in silently reuses the
     * previous account and **the user cannot switch accounts from inside the app** — the same
     * failure `clearSocialSignInState` fixes on Android for Credential Manager.
     *
     * Leaving it unset keeps today's behaviour: a warning, and nothing else.
     */
    var signOutHandler: (() -> Unit)? = null

    private var pendingCallback: ((String?) -> Unit)? = null

    /**
     * Called from Swift to provide the sign-in result.
     */
    @Deprecated(
        "Unused: the result travels in the completion block handed to signInHandler, which is " +
            "what every integration does. Will be removed once no consumer references it.",
        level = DeprecationLevel.WARNING,
    )
    fun onSignInResult(idToken: String?) {
        pendingCallback?.invoke(idToken)
        pendingCallback = null
    }

    /**
     * Initiates the Google Sign-In flow and returns the ID token.
     *
     * @param config the client ids to hand to Swift; [GoogleSignInConfig.iosClientId] wins over
     *   [GoogleSignInConfig.webClientId] when both are set.
     * @return The Google ID token on success, or null if cancelled/failed.
     */
    @OptIn(ExperimentalForeignApi::class)
    suspend fun signIn(config: GoogleSignInConfig): String? = suspendCancellableCoroutine { continuation ->
        val handler = signInHandler
        if (handler == null) {
            Logger.w("GoogleSignIn", "Handler not configured. Set GoogleSignInProviderIOS.signInHandler from Swift.")
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
    @Deprecated(
        "Unused, and wrong on iOS 15+: UIApplication.windows is deprecated and ignores which scene " +
            "is actually in the foreground. Swift hosts pick their own presenter — see " +
            "iosApp/iosApp/AppleSignInCoordinator.swift for how. Will be removed.",
        level = DeprecationLevel.WARNING,
    )
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
