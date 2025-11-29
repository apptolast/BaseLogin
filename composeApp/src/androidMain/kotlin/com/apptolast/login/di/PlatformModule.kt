package com.apptolast.login.di

import com.apptolast.login.data.repository.AuthRepositoryImpl
import com.apptolast.login.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single { FirebaseAuth.getInstance() }
}