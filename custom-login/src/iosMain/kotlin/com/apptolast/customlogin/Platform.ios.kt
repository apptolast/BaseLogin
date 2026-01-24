package com.apptolast.customlogin

import com.apptolast.customlogin.config.GoogleSignInConfig
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.provider.GoogleSignInProviderIOS
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
 */
actual suspend fun getSocialIdToken(provider: IdentityProvider): String? {
    return when (provider) {
        is IdentityProvider.Google -> {
            val config = PlatformKoinHelper.googleSignInConfig
            if (config == null) {
                println("Google Sign-In is not configured. Provide GoogleSignInConfig in LoginLibraryConfig.")
                return null
            }

            val googleProvider = GoogleSignInProviderIOS(config = config)
            googleProvider.signIn()
        }
        is IdentityProvider.GitHub -> {
            // TODO: Implement GitHub OAuth flow for iOS.
            println("GitHub Sign-In for iOS is not implemented yet.")
            null
        }
        else -> {
            println("Social sign-in for ${provider.id} is not implemented on iOS yet.")
            null
        }
    }
}
