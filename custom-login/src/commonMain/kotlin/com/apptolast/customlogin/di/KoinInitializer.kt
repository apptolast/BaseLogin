package com.apptolast.customlogin.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Initializes Koin with custom LoginConfig.
 *
 * @param config The LoginConfig to use. If null, default config will be used.
 * @param appDeclaration Additional Koin configuration.
 */
fun initLoginKoin(
    appDeclaration: KoinAppDeclaration? = null,
//    config: LoginConfig = LoginConfig(),
) {
    startKoin {
        // Platform-specific configuration (optional)
        appDeclaration?.invoke(this)

        modules(
            dataModule,
            presentationModule,
//            configModule(config),
        )
    }
}