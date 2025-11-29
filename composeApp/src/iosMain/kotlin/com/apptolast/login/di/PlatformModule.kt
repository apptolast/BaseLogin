package com.apptolast.login.di

import com.apptolast.login.domain.repository.AuthRepository
import cocoapods.FirebaseAuth.FIRAuth
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<com.apptolast.login.domain.repository.AuthRepository> { AuthRepository(get()) }
    single { FIRAuth.auth() }
}