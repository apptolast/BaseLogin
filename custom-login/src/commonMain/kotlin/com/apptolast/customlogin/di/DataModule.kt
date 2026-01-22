package com.apptolast.customlogin.di

import com.apptolast.customlogin.data.FirebaseAuthProvider
import com.apptolast.customlogin.data.AuthRepositoryImpl
import com.apptolast.customlogin.domain.AuthProvider
import com.apptolast.customlogin.domain.AuthRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.koin.dsl.module

/**
 * Koin module for data layer dependencies.
 * Provides repository implementations and their dependencies.
 */
internal val dataModule = module {
    // Firebase Auth instance from GitLive
    single { Firebase.auth }

    // Firebase Auth Provider
    single<AuthProvider> { FirebaseAuthProvider(get()) }

    // Auth Repository using the default provider
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}

/**
 * Alternative module for custom backend (Ktor-based).
 * Use this instead of dataModule if you want to use a custom backend.
 */
// val customBackendModule = module {
//     single<AuthProvider> { KtorAuthProvider(get(), config) }
//     single<AuthRepository> { AuthRepositoryImpl(get()) }
// }
