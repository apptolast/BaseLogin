package com.apptolast.login

import androidx.compose.ui.window.ComposeUIViewController
import com.apptolast.customlogin.di.initLoginKoin
import com.apptolast.customlogin.domain.model.LoginConfig
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import login.composeapp.generated.resources.Res
import login.composeapp.generated.resources.compose_multiplatform

fun MainViewController() = ComposeUIViewController {

    // 1. Inicializar Firebase (GitLive)
    Firebase.initialize()

    // 2. Inicializar Koin con la config de login
    initLoginKoin(
        config = LoginConfig(
            appName = "SuperApp",
            drawableResource = Res.drawable.compose_multiplatform,
            subtitle = "Bienvenido a la app definitiva",
            googleEnabled = true,
            appleEnabled = true,
            passwordMinLength = 8,
            onGoogleSignIn = {
                println("Google Sign-In clicked")
            },
            onForgotPassword = {
                println("Forgot password clicked")
            }
        )
    )

    App()
}
