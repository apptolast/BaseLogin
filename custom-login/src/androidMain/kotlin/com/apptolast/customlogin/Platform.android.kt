package com.apptolast.customlogin

import android.content.Context
import com.apptolast.customlogin.config.GoogleSignInConfig
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.provider.GoogleSignInProviderAndroid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android-specific implementation of the common `expect` declarations.
 */
actual fun platform(): String = "Android ${android.os.Build.VERSION.SDK_INT}"

/**
 * The Android context, required for many platform-specific operations.
 * This must be initialized at app startup.
 */
lateinit var appContext: Context

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
 * Actual implementation for getting a social ID token on Android.
 */
actual suspend fun getSocialIdToken(provider: IdentityProvider): String? {
    return when (provider) {
        is IdentityProvider.Google -> {
            val config = PlatformKoinHelper.googleSignInConfig
            if (config == null) {
                println("Google Sign-In is not configured. Provide GoogleSignInConfig in LoginLibraryConfig.")
                return null
            }

            val googleProvider = GoogleSignInProviderAndroid(
                config = config,
                context = appContext
            )
            googleProvider.signIn()
        }
        is IdentityProvider.GitHub -> {
            // TODO: Implement GitHub OAuth flow for Android.
            println("GitHub Sign-In for Android is not implemented yet.")
            null
        }
        else -> {
            println("Social sign-in for ${provider.id} is not implemented on Android yet.")
            null
        }
    }
}
