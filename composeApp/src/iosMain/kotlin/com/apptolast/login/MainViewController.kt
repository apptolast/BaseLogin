package com.apptolast.login

import androidx.compose.ui.window.ComposeUIViewController
import com.apptolast.customlogin.di.initLoginKoin
import com.apptolast.login.di.appModule

/**
 * Creates the main iOS view controller.
 * Note: Firebase should be initialized in iOSApp.swift (AppDelegate) before this.
 */
fun MainViewController() = ComposeUIViewController(
    configure = {
        // Initialize Koin once when the controller is configured
        initKoinIfNeeded()
    }
) {
    App()
}

private var koinInitialized = false

private fun initKoinIfNeeded() {
    if (!koinInitialized) {
        initLoginKoin {
            modules(appModule)
        }
        koinInitialized = true
    }
}
