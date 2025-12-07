package com.apptolast.customlogin.di

import com.apptolast.customlogin.domain.model.LoginConfig
import org.koin.dsl.module

/**
 * Provides LoginConfig for dependency injection.
 * Uses provided config or falls back to default.
 */
fun configModule(config: LoginConfig) = module {
    single { config }
}