package com.apptolast.login

import android.app.Application
import com.apptolast.customlogin.di.initKoin
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/**
 * Main Application class for the Android app.
 * This is the entry point for initializing app-wide components like Koin and Firebase.
 */
class LoginApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Firebase using the GitLive method
        Firebase.initialize(this)

        // 2. Initialize Koin with all the necessary modules
        initKoin {
            androidLogger() // Enable Android-specific logging for Koin
            androidContext(this@LoginApplication) // Provide the Android context
        }
    }
}