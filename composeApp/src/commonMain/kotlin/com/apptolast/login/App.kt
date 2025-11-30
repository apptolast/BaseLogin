package com.apptolast.login

import androidx.compose.runtime.Composable
import com.apptolast.customlogin.presentation.navigation.RootNavGraph
import com.apptolast.login.theme.AppToLastLoginTheme

@Composable
fun App() {
    AppToLastLoginTheme {
        RootNavGraph()
    }
}
