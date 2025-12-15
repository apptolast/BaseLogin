package com.apptolast.login.di

import com.apptolast.login.home.presentation.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // ViewModels
    viewModelOf(::HomeViewModel)
}
