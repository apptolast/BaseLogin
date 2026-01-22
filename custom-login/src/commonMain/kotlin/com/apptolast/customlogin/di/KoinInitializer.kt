package com.apptolast.customlogin.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Initializes Koin with custom LoginConfig.
 *
 * @param appDeclaration Additional Koin configuration.
 */
fun initLoginKoin(appDeclaration: KoinAppDeclaration? = null) {
    startKoin {
        appDeclaration?.invoke(this)

        modules(
            dataModule,
            presentationModule,
        )
    }
}