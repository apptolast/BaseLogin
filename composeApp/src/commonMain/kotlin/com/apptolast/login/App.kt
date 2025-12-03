package com.apptolast.login

import androidx.compose.runtime.Composable
import com.apptolast.customlogin.presentation.navigation.RootNavGraph
import com.apptolast.login.theme.AppToLastLoginTheme
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {

    AppToLastLoginTheme {
        RootNavGraph()
    }
}