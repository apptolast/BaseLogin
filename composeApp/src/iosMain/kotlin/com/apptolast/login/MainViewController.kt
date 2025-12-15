package com.apptolast.login

import androidx.compose.ui.window.ComposeUIViewController
import com.apptolast.customlogin.di.initLoginKoin
import com.apptolast.customlogin.domain.model.LoginConfig
import login.composeapp.generated.resources.Res
import login.composeapp.generated.resources.compose_multiplatform

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
        initLoginKoin(
//            config = LoginConfig(
//                appName = "Sample Login App",
//                drawableResource = Res.drawable.compose_multiplatform,
//                subtitle = "Sign in to continue",
//                emailEnabled = true,
//                googleEnabled = true,
//                appleEnabled = true,
//                showRegisterLink = true,
//                showForgotPassword = true,
//                passwordMinLength = 6
//            )
        )
        koinInitialized = true
    }
}
