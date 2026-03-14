package com.apptolast.customlogin

import android.content.Context
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.data.GoogleAuthProviderAndroid

/**
 * The Android context, required for many platform-specific operations.
 * This must be initialized at app startup.
 */
lateinit var appContext: Context

/**
 * Actual implementation for getting a social ID token on Android.
 */
actual suspend fun getSocialIdToken(provider: IdentityProvider): String? {
    return when (provider) {
        is IdentityProvider.Google -> {
            val googleProvider = GoogleAuthProviderAndroid(appContext)
            googleProvider.signIn()
        }

        is IdentityProvider.GitHub -> {
            // TODO: Implement GitHub OAuth flow for Android.
            println("GitHub Sign-In for Android is not implemented yet.")
            null
        }

        else -> {
            println("Social sign-in for ${provider::class.simpleName} is not implemented on Android yet.")
            null
        }
    }
}
