package com.apptolast.baselogin.di

import com.apptolast.baselogin.presentation.screens.forgotpassword.ForgotPasswordViewModel
import com.apptolast.baselogin.presentation.screens.login.LoginViewModel
import com.apptolast.baselogin.presentation.screens.magiclink.MagicLinkViewModel
import com.apptolast.baselogin.presentation.screens.phone.PhoneAuthViewModel
import com.apptolast.baselogin.presentation.screens.reauth.ReauthViewModel
import com.apptolast.baselogin.presentation.screens.register.RegisterViewModel
import com.apptolast.baselogin.presentation.screens.resetpassword.ResetPasswordViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
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
