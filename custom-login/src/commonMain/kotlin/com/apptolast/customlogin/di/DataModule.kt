package com.apptolast.customlogin.di

import com.apptolast.customlogin.data.AppleAuthProvider
import com.apptolast.customlogin.data.AuthRepositoryImpl
import com.apptolast.customlogin.data.FirebaseAuthProvider
import com.apptolast.customlogin.data.GoogleAuthProvider
import com.apptolast.customlogin.data.PhoneAuthProvider
import com.apptolast.customlogin.domain.AuthRepository
import org.koin.dsl.module

/**
 * Koin module for the data layer.
 * It provides concrete implementations for the domain interfaces.
 */
internal val dataModule = module {
    // Concrete Auth Providers
    single { FirebaseAuthProvider() }      // For Email/Password
    single { GoogleAuthProvider() }      // expect/actual for Google Sign-In
    single { AppleAuthProvider() }       // expect/actual for Apple Sign-In
    single { PhoneAuthProvider() }       // expect/actual for Phone/OTP

    // Auth Repository Implementation
    single<AuthRepository> {
        AuthRepositoryImpl(
            // Inject all providers into the repository
            firebaseProvider = get(),
            googleProvider = get(),
            appleProvider = get(),
            phoneProvider = get()
        )
    }
}
