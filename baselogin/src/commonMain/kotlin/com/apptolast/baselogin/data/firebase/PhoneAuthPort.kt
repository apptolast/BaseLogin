package com.apptolast.baselogin.data.firebase

import com.apptolast.baselogin.domain.model.AuthResult
import com.apptolast.baselogin.domain.model.PhoneAuthResult
import com.apptolast.baselogin.sendPhoneVerificationCode
import com.apptolast.baselogin.verifyPhoneCode

/**
 * The SMS OTP flow, which the platform owns end to end.
 *
 * Phone auth is the one flow that cannot go through [FirebaseAuthGateway]: it needs an Activity on
 * Android and APNs registration on iOS, so each platform drives its native SDK directly. That was
 * always fine; what was not fine is that the two entry points are top-level `expect` functions,
 * which cannot be faked from `commonTest` — the same reason [SocialTokenProvider] exists.
 */
interface PhoneAuthPort {

    suspend fun sendCode(phoneNumber: String, timeoutSeconds: Long): PhoneAuthResult

    /**
     * Signs in with the code the user typed.
     *
     * The returned session is **whatever the platform managed to build**, and the two platforms do
     * not agree: Android fills it from the Firebase user, iOS only knows the uid its Swift handler
     * sent back. Callers should re-read the user from [FirebaseAuthGateway] rather than trust this.
     */
    suspend fun verifyCode(verificationId: String, otpCode: String): AuthResult
}

/** Production implementation: delegates to the platform's `actual`. */
class PlatformPhoneAuthPort : PhoneAuthPort {

    override suspend fun sendCode(phoneNumber: String, timeoutSeconds: Long): PhoneAuthResult =
        sendPhoneVerificationCode(phoneNumber, timeoutSeconds)

    override suspend fun verifyCode(verificationId: String, otpCode: String): AuthResult =
        verifyPhoneCode(verificationId, otpCode)
}
