package com.apptolast.customlogin

import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.domain.model.PhoneAuthResult

expect fun platform(): String

/**
 * Typed result of a platform-specific social token acquisition.
 */
sealed interface SocialTokenResult {
    /** The platform obtained a token that Kotlin should use to create a Firebase credential. */
    data class Token(val value: String) : SocialTokenResult

    /**
     * The platform itself completed the entire Firebase sign-in (e.g. Android web OAuth via
     * [startActivityForSignInWithProvider]). [FirebaseAuthProvider] calls [refreshSession] instead
     * of creating a new credential.
     */
    data object PlatformHandled : SocialTokenResult
}

/**
 * Sentinel string used by iOS Swift callbacks (GitHub, Microsoft) to signal that the platform
 * already completed the Firebase sign-in. Swift code passes this string to the Kotlin callback,
 * and the iOS [Platform.ios.kt] implementation converts it to [SocialTokenResult.PlatformHandled].
 *
 * Exposed here so iOS provider companion objects can re-export it for Swift callers.
 */
const val PLATFORM_AUTH_HANDLED = "___PLATFORM_AUTH_COMPLETE___"

/**
 * Initiates a platform-specific social sign-in flow.
 *
 * @param provider The social identity provider (e.g., Google, GitHub).
 * @return [SocialTokenResult.Token] with the ID token, [SocialTokenResult.PlatformHandled] if the
 *         platform completed sign-in directly, or `null` if cancelled or failed.
 */
expect suspend fun getSocialIdToken(provider: IdentityProvider): SocialTokenResult?

/**
 * Sends a phone verification OTP using the platform's native Firebase SDK.
 * On Android, this may trigger instant (SIM-based) auto-verification.
 * On iOS, the Firebase SDK handles APNS token registration automatically.
 *
 * @param phoneNumber E.164 format phone number (e.g. "+34612345678").
 * @return [PhoneAuthResult.CodeSent] with the verificationId, [PhoneAuthResult.AutoSignedIn]
 *         on Android instant verification, or [PhoneAuthResult.Failure] on error.
 */
expect suspend fun sendPhoneVerificationCode(phoneNumber: String): PhoneAuthResult

/**
 * Verifies a phone OTP code using the platform's native Firebase SDK and signs the user in.
 *
 * @param verificationId The verification ID returned by [sendPhoneVerificationCode].
 * @param otpCode The SMS code entered by the user.
 * @return [AuthResult.Success] on successful sign-in, or [AuthResult.Failure] on error.
 */
expect suspend fun verifyPhoneCode(verificationId: String, otpCode: String): AuthResult
