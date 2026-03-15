package com.apptolast.customlogin

import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.domain.model.PhoneAuthResult

expect fun platform(): String

/**
 * Sentinel token returned by platform implementations when the platform
 * itself (e.g. Android via [startActivityForSignInWithProvider]) has already
 * completed the Firebase sign-in. [FirebaseAuthProvider] detects this value
 * and calls [refreshSession] instead of creating a new credential.
 */
const val PLATFORM_AUTH_HANDLED = "___PLATFORM_AUTH_COMPLETE___"

/**
 * Initiates a platform-specific social sign-in flow to get an ID token.
 * This common `expect` function is implemented in each platform's `actual` function.
 *
 * @param provider The social provider (e.g., Google, GitHub).
 * @return The ID token from the provider upon successful sign-in, or null if the flow is cancelled or fails.
 */
expect suspend fun getSocialIdToken(provider: IdentityProvider): String?

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
