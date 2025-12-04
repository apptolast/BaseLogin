package com.apptolast.login

import androidx.compose.ui.window.ComposeUIViewController
import com.apptolast.customlogin.di.initLoginKoin
import com.apptolast.customlogin.domain.model.LoginConfig
import login.composeapp.generated.resources.Res
import login.composeapp.generated.resources.compose_multiplatform

fun MainViewController() = ComposeUIViewController {

    // Inicializar Koin con la config de login
    // Nota: Firebase ya está inicializado en iOSApp.swift (AppDelegate)
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
