package com.apptolast.customlogin.di

import com.apptolast.customlogin.LoginSDK
import com.apptolast.customlogin.domain.model.LoginConfig
import org.koin.dsl.module

/**
 * Koin module for providing default application-level configurations.
 * This can be overridden by the consuming application to provide custom branding.
 */
val configModule = module {
    // Provides the default login screen configuration.
    // This will be overridden if the consuming app provides its own LoginConfig bean.
        single { LoginSDK.config }
}
