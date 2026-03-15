package com.apptolast.customlogin.di

import com.apptolast.customlogin.config.AppleSignInConfig
import com.apptolast.customlogin.config.GoogleSignInConfig
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * Configuration for the login library.
 *
 * @property googleSignInConfig Configuration for Google Sign-In. If null, Google Sign-In will not be available.
 * @property appleSignInConfig Configuration for Apple Sign-In. If null, Apple Sign-In will not be available.
 *   Apple Sign-In is iOS-only; it is ignored on Android.
 */
data class LoginLibraryConfig(
    val googleSignInConfig: GoogleSignInConfig? = null,
    val appleSignInConfig: AppleSignInConfig? = null
)

/**
 * Initializes Koin with custom LoginConfig.
 *
 * @param config The library configuration including social sign-in providers.
 * @param appDeclaration Additional Koin configuration.
 */
fun initLoginKoin(
    config: LoginLibraryConfig = LoginLibraryConfig(),
    appDeclaration: KoinAppDeclaration? = null
) {
    startKoin {
        appDeclaration?.invoke(this)

        // Module for configuration
        val configModule = module {
            single { config }
            config.googleSignInConfig?.let { googleConfig ->
                single { googleConfig }
            }
            config.appleSignInConfig?.let { appleConfig ->
                single { appleConfig }
            }
        }

        modules(
            configModule,
            dataModule,
            presentationModule,
        )
    }
}