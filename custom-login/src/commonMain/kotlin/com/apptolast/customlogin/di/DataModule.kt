package com.apptolast.customlogin.di

import com.apptolast.customlogin.data.repository.AuthRepositoryImpl
import com.apptolast.customlogin.domain.repository.AuthRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for data layer dependencies.
 * Provides repository implementations and their dependencies.
 */
val dataModule = module {
    single { Firebase.auth } // Provide the FirebaseAuth instance from GitLive Firebase
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
}
