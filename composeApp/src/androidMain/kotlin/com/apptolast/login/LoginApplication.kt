package com.apptolast.login

import android.app.Application
import com.apptolast.baselogin.BaseLoginAndroid
import com.apptolast.baselogin.config.AppleSignInConfig
import com.apptolast.baselogin.config.GoogleSignInConfig
import com.apptolast.baselogin.config.MagicLinkConfig
import com.apptolast.baselogin.di.LoginLibraryConfig
import com.apptolast.baselogin.di.initLoginKoin
import com.apptolast.login.di.appModule
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import org.koin.android.ext.koin.androidContext

class LoginApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        BaseLoginAndroid.initialize(this)

        Firebase.initialize(this)

        // Configure App Check
        Firebase.appCheck.installAppCheckProviderFactory(
            // Use Debug provider for development, PlayIntegrity for production
            DebugAppCheckProviderFactory.getInstance(),
        )

        // Configure Google Sign-In with the Web Client ID from Firebase Console
        val loginConfig = LoginLibraryConfig(
            googleSignInConfig = GoogleSignInConfig(
                webClientId = "218717255604-h57da28qm4s2ed0f8js5a9q54gnbett5.apps.googleusercontent.com",
            ),
            appleSignInConfig = AppleSignInConfig(),
            githubEnabled = true,
            microsoftEnabled = true,
            twitterEnabled = true,
            facebookEnabled = true,
            magicLinkConfig = MagicLinkConfig(
                continueUrl = "https://apptolast.com/login",
            ),
        )

        initLoginKoin(config = loginConfig) {
            androidContext(this@LoginApplication)
            modules(appModule)
        }
    }
}
