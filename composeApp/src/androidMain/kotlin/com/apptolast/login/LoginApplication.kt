// composeApp/src/androidMain/kotlin/com/apptolast/login/MyApplication.kt
package com.apptolast.login

import android.app.Application
import com.apptolast.customlogin.di.initLoginKoin
import com.apptolast.customlogin.domain.model.LoginConfig
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import org.koin.android.ext.koin.androidContext

class LoginApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Inicializar Firebase (GitLive)
        Firebase.initialize(this)

        // 2. Inicializar Koin con la config de login
        initLoginKoin(
            appDeclaration = { androidContext(this@LoginApplication) },
            config = LoginConfig().copy(
                appName = "SuperApp",
//                drawableResource = Res.drawable.compose_multiplatform,
                subtitle = "Bienvenido a la app definitiva",
                googleEnabled = true,
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