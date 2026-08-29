package com.apptolast.login

import androidx.compose.ui.window.ComposeUIViewController
import com.apptolast.baselogin.config.AppleSignInConfig
import com.apptolast.baselogin.config.GoogleSignInConfig
import com.apptolast.baselogin.config.MagicLinkConfig
import com.apptolast.baselogin.di.LoginLibraryConfig
import com.apptolast.baselogin.di.initLoginKoin
import com.apptolast.login.di.appModule

/**
 * Creates the main iOS view controller.
 * Note: Firebase should be initialized in iOSApp.swift (AppDelegate) before this.
 */
// Swift calls this entry point as MainViewControllerKt.MainViewController(), so it keeps PascalCase.
@Suppress("ktlint:standard:function-naming")
fun MainViewController() = ComposeUIViewController(
    configure = {
        // Initialize Koin once when the controller is configured
        initKoinIfNeeded()
    },
) {
    App()
}

private var koinInitialized = false

private fun initKoinIfNeeded() {
    if (!koinInitialized) {
        // Configure Google Sign-In with both Web and iOS Client IDs
        val loginConfig = LoginLibraryConfig(
            googleSignInConfig = GoogleSignInConfig(
                webClientId = "218717255604-h57da28qm4s2ed0f8js5a9q54gnbett5.apps.googleusercontent.com",
                iosClientId = "218717255604-lncshc55j3qko90c3v798h15g2oocuv4.apps.googleusercontent.com",
            ),
            appleSignInConfig = AppleSignInConfig(),
            githubEnabled = true,
            microsoftEnabled = true,
            twitterEnabled = true,
            facebookEnabled = true,
            magicLinkConfig = MagicLinkConfig(
                // Must be the app's real bundle id, or the link never reopens the app.
                // See iosApp/Configuration/Config.xcconfig.
                continueUrl = "https://apptolast.com/login",
                iosBundleId = "com.apptolast.login",
            ),
        )

        initLoginKoin(config = loginConfig) {
            modules(appModule)
        }
        koinInitialized = true
    }
}
