package com.apptolast.customlogin.di

import com.apptolast.customlogin.domain.model.GoogleSignInConfig
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Initializes Koin with custom LoginConfig.
 *
 * @param googleSignInConfig The library configuration including social sign-in providers.
 * @param appDeclaration Additional Koin configuration.
 */
fun initLoginKoin(
    googleSignInConfig: GoogleSignInConfig,
    appDeclaration: KoinAppDeclaration? = null,
) {
    startKoin {
        appDeclaration?.invoke(this)
        modules(
            dataModule(googleSignInConfig),
            presentationModule,
        )
    }
}