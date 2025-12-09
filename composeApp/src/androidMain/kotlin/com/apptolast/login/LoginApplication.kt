package com.apptolast.login

import android.app.Application
import com.apptolast.customlogin.di.initLoginKoin
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import org.koin.android.ext.koin.androidContext

class LoginApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Firebase
        Firebase.initialize(this)

        // 2. Configure App Check
        Firebase.appCheck.installAppCheckProviderFactory(
            // Use Debug provider for development, PlayIntegrity for production
            DebugAppCheckProviderFactory.getInstance()
        )

        // 3. Initialize Koin with custom login config
        initLoginKoin(
            appDeclaration = { androidContext(this@LoginApplication) },
//            config = LoginConfig(
//                appName = "Sample Login App",
//                subtitle = "Sign in to continue",
//                drawableResource = Res.drawable.compose_multiplatform,
//
//                // Enable features
//                emailEnabled = true,
//                googleEnabled = true,
//                appleEnabled = false,
//                showRegisterLink = true,
//                showForgotPassword = true,
//
//                // Validation
//                passwordMinLength = 6,
//
//                // Custom labels
//                signInButtonText = "Sign In",
//                signInWithGoogleText = "Continue with Google",
//                registerLinkText = "Don't have an account? Sign up",
//                forgotPasswordText = "Forgot password?"
//            )
        )
    }
}