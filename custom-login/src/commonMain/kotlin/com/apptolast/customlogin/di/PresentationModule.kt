package com.apptolast.customlogin.di

import com.apptolast.customlogin.presentation.screens.forgotpassword.ForgotPasswordViewModel
import com.apptolast.customlogin.presentation.screens.login.LoginViewModel
import com.apptolast.customlogin.presentation.screens.magiclink.MagicLinkViewModel
import com.apptolast.customlogin.presentation.screens.phone.PhoneAuthViewModel
import com.apptolast.customlogin.presentation.screens.reauth.ReauthViewModel
import com.apptolast.customlogin.presentation.screens.register.RegisterViewModel
import com.apptolast.customlogin.presentation.screens.resetpassword.ResetPasswordViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for authentication-related presentation layer.
 */
val loginPresentationModule: Module = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::ResetPasswordViewModel)
    viewModelOf(::PhoneAuthViewModel)
    viewModelOf(::MagicLinkViewModel)
    viewModelOf(::ReauthViewModel)
}

internal val presentationModule: Module = loginPresentationModule
