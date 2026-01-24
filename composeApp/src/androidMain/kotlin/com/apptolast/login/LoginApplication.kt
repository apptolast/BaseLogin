package com.apptolast.login

import android.app.Application
import com.apptolast.customlogin.appContext
import com.apptolast.customlogin.config.GoogleSignInConfig
import com.apptolast.customlogin.di.LoginLibraryConfig
import com.apptolast.customlogin.di.initLoginKoin
import com.apptolast.login.di.appModule
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import org.koin.android.ext.koin.androidContext

class LoginApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Set the app context for custom-login library
        appContext = this

        Firebase.initialize(this)

        // Configure App Check
        Firebase.appCheck.installAppCheckProviderFactory(
            // Use Debug provider for development, PlayIntegrity for production
            DebugAppCheckProviderFactory.getInstance()
        )

        // Configure Google Sign-In with the Web Client ID from Firebase Console
        val loginConfig = LoginLibraryConfig(
            googleSignInConfig = GoogleSignInConfig(
                webClientId = "495458702268-al98mksrlh27v607972b0oaa0g98pfru.apps.googleusercontent.com"
            )
        )

        initLoginKoin(config = loginConfig) {
            androidContext(this@LoginApplication)
            modules(appModule)
        }
    }
}
