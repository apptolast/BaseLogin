package com.apptolast.customlogin

import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.data.GoogleAuthProviderIOS

/**
 * Actual implementation for getting a social ID token on iOS.
 */
actual suspend fun getSocialIdToken(provider: IdentityProvider): String? {
    return when (provider) {
        is IdentityProvider.Google -> {
            val googleProvider = GoogleAuthProviderIOS()
            googleProvider.signIn()
        }

        is IdentityProvider.GitHub -> {
            // TODO: Implement GitHub OAuth flow for iOS.
            println("GitHub Sign-In for iOS is not implemented yet.")
            null
        }

        else -> {
            println("Social sign-in for ${provider::class.simpleName} is not implemented on iOS yet.")
            null
        }
    }
}
