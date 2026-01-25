package com.apptolast.login.di

import coil3.ImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Provides the actual Koin module for creating the ImageLoader on Android.
 * It uses the Android application context and configures Ktor as the network client.
 */
actual fun imageLoaderModule(): Module = module {
    single {
        ImageLoader.Builder(androidContext())
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }
}
