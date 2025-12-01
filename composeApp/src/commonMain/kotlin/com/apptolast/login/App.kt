package com.apptolast.login

import androidx.compose.runtime.Composable
import com.apptolast.customlogin.LoginSDK
import com.apptolast.customlogin.domain.model.LoginConfig
import com.apptolast.customlogin.presentation.navigation.RootNavGraph
import com.apptolast.login.theme.AppToLastLoginTheme

@Composable
fun App() {
    LoginSDK.init(
        LoginConfig(
            appName = "SuperApp",
            appLogoUrl = "",
            subtitle = "Bienvenido a la app definitiva"
        )
    )

    AppToLastLoginTheme {
        RootNavGraph()
    }
}
