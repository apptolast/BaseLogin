package com.apptolast.customlogin

import com.apptolast.customlogin.domain.model.IdentityProvider
import platform.UIKit.UIDevice

/**
 * iOS-specific implementation of the common `expect` declarations.
 */
actual fun platform(): String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

/**
 * Actual implementation for getting a social ID token on iOS.
 */
actual suspend fun getSocialIdToken(provider: IdentityProvider): String? {
    return when (provider) {
        is IdentityProvider.Google -> {
            // TODO: Implement the native Google Sign-In flow for iOS.
            // 1. Add `pod 'GoogleSignIn', '~> 7.0'` to your `iosApp/Podfile`.
            // 2. You will need to obtain the top UIViewController to present the sign-in flow.
            //    This is a common challenge in KMP and often requires a helper class or passing the controller down from SwiftUI.
            // 3. Call `GIDSignIn.sharedInstance.signIn(...)` and await the result.
            println("Google Sign-In for iOS is not implemented yet.")
            null
        }
        is IdentityProvider.GitHub -> {
            // TODO: Implement GitHub OAuth flow for iOS.
            // This usually involves using ASWebAuthenticationSession to open a browser to the GitHub auth URL.
            println("GitHub Sign-In for iOS is not implemented yet.")
            null
        }
        else -> {
            println("Social sign-in for ${provider.id} is not implemented on iOS yet.")
            null
        }
    }
}
