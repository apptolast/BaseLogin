package com.apptolast.login.di

import com.apptolast.login.SplashViewModel
import com.apptolast.login.presentation.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // ViewModels
    viewModelOf(::SplashViewModel)
    viewModelOf(::ProfileViewModel)
}
