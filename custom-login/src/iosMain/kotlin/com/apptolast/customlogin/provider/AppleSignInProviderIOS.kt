package com.apptolast.customlogin.provider

import com.apptolast.customlogin.util.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * iOS implementation of Apple Sign-In via AuthenticationServices.
 *
 * Uses a callback mechanism to integrate with Swift. The hosting app must set
 * [signInHandler] from Swift before Apple Sign-In is triggered.
 *
 * ## Swift setup example
 *
 * ```swift
 * import AuthenticationServices
 * import CryptoKit
 *
 * // 1. Generate a secure nonce (call once per sign-in attempt)
 * private func randomNonceString(length: Int = 32) -> String { ... }
 * private func sha256(_ input: String) -> String {
 *     let inputData = Data(input.utf8)
 *     let hash = SHA256.hash(data: inputData)
 *     return hash.compactMap { String(format: "%02x", $0) }.joined()
 * }
 *
 * // 2. Configure the handler (do this at app startup, e.g. in AppDelegate)
 * AppleSignInProviderIOS.companion.signInHandler = { _, completion in
 *     let rawNonce = self.randomNonceString()
 *     let hashedNonce = self.sha256(rawNonce)
 *     self.currentRawNonce = rawNonce   // store for delegate callback
 *
 *     let provider = ASAuthorizationAppleIDProvider()
 *     let request = provider.createRequest()
 *     request.requestedScopes = [.fullName, .email]
 *     request.nonce = hashedNonce
 *
 *     // Store the completion to call from delegate
 *     self.appleSignInCompletion = completion
 *
 *     let controller = ASAuthorizationController(authorizationRequests: [request])
 *     controller.delegate = self
 *     controller.presentationContextProvider = self
 *     controller.performRequests()
 * }
 *
 * // 3. In your ASAuthorizationControllerDelegate:
 * func authorizationController(controller:, didCompleteWithAuthorization authorization:) {
 *     if let appleCredential = authorization.credential as? ASAuthorizationAppleIDCredential,
 *        let tokenData = appleCredential.identityToken,
 *        let idToken = String(data: tokenData, encoding: .utf8) {
 *         let rawNonce = self.currentRawNonce ?? ""
 *         // Encode idToken and rawNonce for Kotlin using the "|||rawNonce|||" separator
 *         let combined = rawNonce.isEmpty ? idToken : "\(idToken)|||rawNonce|||\(rawNonce)"
 *         self.appleSignInCompletion?(combined)
 *     } else {
 *         self.appleSignInCompletion?(nil)
 *     }
 *     self.appleSignInCompletion = nil
 * }
 *
 * func authorizationController(controller:, didCompleteWithError error:) {
 *     self.appleSignInCompletion?(nil)
 *     self.appleSignInCompletion = nil
 * }
 * ```
 *
 * ## Token format
 * The completion callback must be called with one of:
 * - `"<idToken>|||rawNonce|||<rawNonce>"` — recommended (includes nonce for replay protection)
 * - `"<idToken>"` — without nonce (less secure)
 * - `null` — cancelled or failed
 */
object AppleSignInProviderIOS {

    /**
     * Set from Swift to perform the native Apple Sign-In.
     * The first `String?` parameter is unused (reserved for future config).
     * Call the completion with the encoded token string on success, or `null` on failure.
     */
    var signInHandler: ((String?, (String?) -> Unit) -> Unit)? = null

    suspend fun signIn(): String? = suspendCancellableCoroutine { cont ->
        val handler = signInHandler
        if (handler == null) {
            Logger.w(
                "AppleSignIn",
                "signInHandler not configured. Set AppleSignInProviderIOS.signInHandler from Swift."
            )
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        handler(null) { tokenData ->
            if (cont.isActive) {
                cont.resume(tokenData)
            }
        }
    }
}