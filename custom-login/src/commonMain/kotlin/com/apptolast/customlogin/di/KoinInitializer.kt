package com.apptolast.customlogin.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Initializes Koin with all the required modules for the custom-login feature.
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            configModule,       // Provides default LoginConfig
            dataModule,         // Provides AuthRepository
            presentationModule,  // Provides ViewModels
        )
    }
}
