package com.apptolast.login.di

import com.apptolast.login.presentation.viewmodel.login.LoginViewModel
import org.koin.dsl.module

val authModule = module {
    factory { LoginViewModel(get()) }
}