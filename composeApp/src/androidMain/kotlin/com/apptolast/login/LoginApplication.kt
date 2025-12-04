// composeApp/src/androidMain/kotlin/com/apptolast/login/MyApplication.kt
package com.apptolast.login

import android.app.Application
import com.apptolast.customlogin.di.initLoginKoin
import com.apptolast.customlogin.domain.model.LoginConfig
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import org.koin.android.ext.koin.androidContext

class LoginApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Inicializar Firebase
        Firebase.initialize(this)

        // 2. Configurar App Check (ANTES de usar cualquier servicio de Firebase)
        // Nota: Para desarrollo usa DebugAppCheckProviderFactory
        // Para producción usa PlayIntegrityAppCheckProviderFactory
        Firebase.appCheck.installAppCheckProviderFactory(
            if (BuildConfig.DEBUG) {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        )

        // 3. Inicializar Koin con la config de login
        initLoginKoin(
            appDeclaration = { androidContext(this@LoginApplication) },
            config = LoginConfig().copy(
                appName = "SuperApp",
//                drawableResource = Res.drawable.ic_launcher,
                subtitle = "Bienvenido a la app definitiva",
                googleEnabled = false,
                appleEnabled = true,
                passwordMinLength = 8,
                signInWithGoogleText = "Continuar con Google",
                onGoogleSignIn = {
                    println("Google Sign-In clicked")
                },
                onForgotPassword = {
                    println("Forgot password clicked")
                }
            )
        )
    }
}