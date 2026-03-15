package com.apptolast.customlogin.data

import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.PhoneAuthResult
import com.apptolast.customlogin.domain.model.UserSession
import com.apptolast.customlogin.util.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * iOS implementation of phone OTP sending.
 *
 * The hosting app must set [sendCodeHandler] from Swift before phone auth is used.
 * Swift is responsible for calling Firebase's [PhoneAuthProvider.provider().verifyPhoneNumber()].
 *
 * Swift setup example:
 * ```swift
 * PhoneAuthProviderIOS.companion.sendCodeHandler = { phoneNumber, completion in
 *     PhoneAuthProvider.provider().verifyPhoneNumber(phoneNumber, uiDelegate: nil) { verificationId, error in
 *         completion(verificationId)
 *     }
 * }
 * ```
 */
object PhoneAuthProviderIOS {

    /**
     * Set from Swift. Receives the phone number, triggers the native Firebase verification,
     * and calls the completion block with the verificationId (or null on failure).
     */
    var sendCodeHandler: ((String, (String?) -> Unit) -> Unit)? = null

    /**
     * Set from Swift. Receives the verificationId and OTP code, signs in with the native
     * Firebase SDK, and calls the completion block with the user's UID on success or null on failure.
     *
     * Swift setup example:
     * ```swift
     * PhoneAuthProviderIOS.companion.verifyCodeHandler = { verificationId, smsCode, completion in
     *     let credential = PhoneAuthProvider.provider().credential(withVerificationID: verificationId, verificationCode: smsCode)
     *     Auth.auth().signIn(with: credential) { authResult, error in
     *         completion(authResult?.user.uid)
     *     }
     * }
     * ```
     */
    var verifyCodeHandler: ((String, String, (String?) -> Unit) -> Unit)? = null

    suspend fun sendCode(phoneNumber: String): PhoneAuthResult =
        suspendCancellableCoroutine { cont ->
            val handler = sendCodeHandler
            if (handler == null) {
                Logger.w("PhoneAuth", "sendCodeHandler not configured. Set PhoneAuthProviderIOS.sendCodeHandler from Swift.")
                cont.resume(PhoneAuthResult.Failure(AuthError.Unknown("Phone auth handler not configured. Set PhoneAuthProviderIOS.sendCodeHandler from Swift.")))
                return@suspendCancellableCoroutine
            }

            handler(phoneNumber) { verificationId ->
                if (cont.isActive) {
                    if (verificationId != null) {
                        cont.resume(PhoneAuthResult.CodeSent(verificationId))
                    } else {
                        cont.resume(PhoneAuthResult.Failure(AuthError.Unknown("Failed to send verification code")))
                    }
                }
            }
        }

    suspend fun verifyCode(verificationId: String, otpCode: String): AuthResult =
        suspendCancellableCoroutine { cont ->
            val handler = verifyCodeHandler
            if (handler == null) {
                Logger.w("PhoneAuth", "verifyCodeHandler not configured. Set PhoneAuthProviderIOS.verifyCodeHandler from Swift.")
                cont.resume(AuthResult.Failure(AuthError.Unknown("Phone verify handler not configured. Set PhoneAuthProviderIOS.verifyCodeHandler from Swift.")))
                return@suspendCancellableCoroutine
            }

            handler(verificationId, otpCode) { userId ->
                if (cont.isActive) {
                    if (userId != null) {
                        cont.resume(AuthResult.Success(UserSession(userId = userId, email = null)))
                    } else {
                        cont.resume(AuthResult.Failure(AuthError.Unknown("Phone OTP verification failed")))
                    }
                }
            }
        }
}
