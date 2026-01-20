package com.apptolast.login

import android.app.Application
import com.apptolast.customlogin.di.initLoginKoin
import com.apptolast.login.di.appModule
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LoginApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)

        // 2. Configure App Check
        Firebase.appCheck.installAppCheckProviderFactory(
            // Use Debug provider for development, PlayIntegrity for production
            DebugAppCheckProviderFactory.getInstance()
        )

        initLoginKoin {
            androidContext(this@LoginApplication)
            modules(appModule)
        }
    }
}
