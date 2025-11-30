package com.apptolast.customlogin.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Initializes Koin dependency injection.
 * This function should be called from the entry point of each platform (Android Application, iOS App).
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            presentationModule, // Provides ViewModels
            dataModule     // Provides data
        )
    }
}
