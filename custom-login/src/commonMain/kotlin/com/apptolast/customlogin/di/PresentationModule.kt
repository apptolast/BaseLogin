package com.apptolast.customlogin.di

import com.apptolast.customlogin.presentation.screens.login.LoginViewModel
import com.apptolast.customlogin.presentation.screens.register.RegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for authentication-related presentation layer.
 */
val presentationModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
}
