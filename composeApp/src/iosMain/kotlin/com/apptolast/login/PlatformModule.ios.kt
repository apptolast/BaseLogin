package com.apptolast.login

import com.apptolast.login.data.repository.AuthRepositoryIos
import com.apptolast.login.domain.repository.AuthRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single <AuthRepository>{ AuthRepositoryIos() }
}