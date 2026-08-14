package com.apptolast.customlogin

import com.apptolast.customlogin.SocialTokenResult
import com.apptolast.customlogin.config.GoogleSignInConfig
import com.apptolast.customlogin.data.PhoneAuthProviderIOS
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.domain.model.PhoneAuthResult
import com.apptolast.customlogin.provider.AppleSignInProviderIOS
import com.apptolast.customlogin.provider.FacebookSignInProviderIOS
import com.apptolast.customlogin.provider.GitHubSignInProviderIOS
import com.apptolast.customlogin.provider.GoogleSignInProviderIOS
import com.apptolast.customlogin.provider.MicrosoftSignInProviderIOS
import com.apptolast.customlogin.provider.TwitterSignInProviderIOS
import com.apptolast.customlogin.util.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.UIKit.UIDevice

/**
 * iOS-specific implementation of the common `expect` declarations.
 */
actual fun platform(): String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

/**
 * Helper object for Koin dependency injection in platform code.
 */
private object PlatformKoinHelper : KoinComponent {
    val googleSignInConfig: GoogleSignInConfig? by lazy {
        try {
            val config: GoogleSignInConfig by inject()
            config
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Actual implementation for getting a social ID token on iOS.
 *
 * GitHub and Microsoft providers complete the entire Firebase sign-in from Swift and signal
 * completion via [PLATFORM_AUTH_HANDLED]. This implementation converts that sentinel to the
 * type-safe [SocialTokenResult.PlatformHandled].
 */
actual suspend fun getSocialIdToken(provider: IdentityProvider): SocialTokenResult? {
    return when (provider) {
        is IdentityProvider.Google -> {
            val config = PlatformKoinHelper.googleSignInConfig
            if (config == null) {
                Logger.w(
                    "Platform",
                    "Google Sign-In is not configured. Provide GoogleSignInConfig in LoginLibraryConfig.",
                )
                return null
            }

            val googleProvider = GoogleSignInProviderIOS(config = config)
            googleProvider.signIn()?.let { SocialTokenResult.Token(it) }
        }
        is IdentityProvider.Apple -> AppleSignInProviderIOS.signIn()?.let { SocialTokenResult.Token(it) }
        is IdentityProvider.GitHub -> GitHubSignInProviderIOS.signIn()?.toSocialTokenResult()
        is IdentityProvider.Microsoft -> MicrosoftSignInProviderIOS.signIn()?.toSocialTokenResult()
        is IdentityProvider.Twitter -> TwitterSignInProviderIOS.signIn()?.toSocialTokenResult()
        is IdentityProvider.Facebook -> FacebookSignInProviderIOS.signIn()?.toSocialTokenResult()
        else -> {
            Logger.w("Platform", "Social sign-in for ${provider.id} is not implemented on iOS yet.")
            null
        }
    }
}

/** Converts a raw String callback result from Swift to a type-safe [SocialTokenResult]. */
private fun String.toSocialTokenResult(): SocialTokenResult = if (this == PLATFORM_AUTH_HANDLED) {
    SocialTokenResult.PlatformHandled
} else {
    SocialTokenResult.Token(this)
}

/**
 * iOS actual implementation: delegates to [PhoneAuthProviderIOS] which uses a Swift callback
 * to call Firebase's [PhoneAuthProvider.provider().verifyPhoneNumber()].
 */
actual suspend fun sendPhoneVerificationCode(phoneNumber: String, timeoutSeconds: Long): PhoneAuthResult =
    PhoneAuthProviderIOS.sendCode(phoneNumber)

/**
 * iOS actual implementation: delegates to [PhoneAuthProviderIOS] which uses a Swift callback
 * to create the credential and sign in via the native Firebase iOS SDK.
 */
actual suspend fun verifyPhoneCode(verificationId: String, otpCode: String): AuthResult =
    PhoneAuthProviderIOS.verifyCode(verificationId, otpCode)

/**
 * No-op on iOS: neither ASAuthorizationController nor GIDSignIn cache an account selection that
 * survives sign-out the way Credential Manager does on Android.
 */
actual suspend fun clearSocialSignInState() {
    // Intentionally empty.
}

/**
 * Revokes the Apple token before the account is deleted, as App Review guideline 5.1.1(v) requires.
 *
 * **Opt-in**: with no `revokeHandler` set this logs a warning and does nothing, so a host that
 * updates its pin does not suddenly get an Apple sheet in the middle of an already shipped
 * account-deletion flow. Nothing to do for the other providers — none of them requires revocation
 * and none exposes an equivalent API.
 */
actual suspend fun revokeSocialToken(provider: IdentityProvider) {
    if (provider !is IdentityProvider.Apple) return
    AppleSignInProviderIOS.revoke()?.let { error ->
        // Reported, never rethrown: deletion goes ahead regardless.
        Logger.w("Platform", "Apple token revocation failed: $error")
    }
}
