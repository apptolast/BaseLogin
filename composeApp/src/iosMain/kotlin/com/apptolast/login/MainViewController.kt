package com.apptolast.login

import androidx.compose.ui.window.ComposeUIViewController
import com.apptolast.customlogin.domain.model.GoogleSignInConfig
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
        // Configure Google Sign-In with both Web and iOS Client IDs
        val loginConfig = GoogleSignInConfig(
            webClientId = "218717255604-h57da28qm4s2ed0f8js5a9q54gnbett5.apps.googleusercontent.com",
            iosClientId = "495458702268-1ekoub6nmp7hmkhinuasdlup1rke9kg4.apps.googleusercontent.com"
        )


        initLoginKoin(googleSignInConfig = loginConfig) {
            modules(appModule)
        }
        koinInitialized = true
    }
}
