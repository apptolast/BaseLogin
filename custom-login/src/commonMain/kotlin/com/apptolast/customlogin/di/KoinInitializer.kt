package com.apptolast.customlogin.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

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
            config.magicLinkConfig?.let { mlConfig ->
                single { mlConfig }
            }
        }

        modules(
            configModule,
            dataModule,
            presentationModule,
        )
    }
}
