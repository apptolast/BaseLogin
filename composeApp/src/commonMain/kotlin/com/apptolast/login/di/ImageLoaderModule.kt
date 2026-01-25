package com.apptolast.login.di

import org.koin.core.module.Module

/**
 * Declares the Koin module for providing a platform-specific ImageLoader.
 * This allows shared code to use Coil's image loading capabilities
 * without depending on a specific platform implementation.
 */
expect fun imageLoaderModule(): Module
