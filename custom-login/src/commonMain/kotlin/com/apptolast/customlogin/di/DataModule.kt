package com.apptolast.customlogin.di

import com.apptolast.customlogin.data.AuthRepositoryImpl
import com.apptolast.customlogin.data.FirebaseAuthProvider
import com.apptolast.customlogin.domain.AuthProvider
import com.apptolast.customlogin.domain.AuthRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for data layer dependencies.
 * Provides repository implementations and their dependencies.
 */
fun loginDataModule(authProvider: AuthProvider? = null): Module = module {
    if (authProvider == null) {
        // Firebase Auth instance from GitLive. Only registered for the default Firebase provider,
        // so custom providers do not have to initialize Firebase if they do not use it.
        single { Firebase.auth }
        single<AuthProvider> { FirebaseAuthProvider(get()) }
    } else {
        single<AuthProvider> { authProvider }
    }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
}

/**
 * Internal default module kept for source compatibility inside the library.
 * Integrators should prefer [loginDataModule] so they can pass a custom [AuthProvider].
 */
internal val dataModule: Module = loginDataModule()
