package com.apptolast.customlogin.di

import com.apptolast.customlogin.data.AuthRepositoryImpl
import com.apptolast.customlogin.data.FirebaseAuthProvider
import com.apptolast.customlogin.data.FirebaseAuthService
import com.apptolast.customlogin.data.FirebaseAuthServiceImpl
import com.apptolast.customlogin.domain.AuthProvider
import com.apptolast.customlogin.domain.AuthRepository
import com.apptolast.customlogin.domain.model.GoogleSignInConfig
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.koin.dsl.module

/**
 * Koin module for data layer dependencies.
 * Provides repository implementations and their dependencies.
 */
internal fun dataModule(googleSignInConfig: GoogleSignInConfig) = module {
    // Firebase Auth instance from GitLive
    single { Firebase.auth }

    // Firebase Auth Service (abstraction layer for testing)
    single<FirebaseAuthService> { FirebaseAuthServiceImpl(get()) }

    // Google instance from GitLive
    single { googleSignInConfig }

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
