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

        // Module for configuration.
        // - LoginLibraryConfig: always registered; consumed by AuthRepositoryImpl and ViewModels.
        // - GoogleSignInConfig: registered only when Google sign-in is enabled; injected by type
        //   in PlatformKoinHelper (both Android and iOS) to configure the Google sign-in client.
        //   Apple / MagicLink / other configs are accessed directly from LoginLibraryConfig
        //   and do not need separate Koin registrations.
        val configModule = module {
            single { config }
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
