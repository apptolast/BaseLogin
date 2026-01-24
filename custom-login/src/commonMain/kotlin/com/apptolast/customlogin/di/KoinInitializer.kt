package com.apptolast.customlogin.di

import com.apptolast.customlogin.config.GoogleSignInConfig
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * Configuration for the login library.
 *
 * @property googleSignInConfig Configuration for Google Sign-In. If null, Google Sign-In will not be available.
 */
data class LoginLibraryConfig(
    val googleSignInConfig: GoogleSignInConfig? = null
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
            config.googleSignInConfig?.let { googleConfig ->
                single { googleConfig }
            }
        }

        modules(
            configModule,
            dataModule,
            presentationModule,
        )
    }
}