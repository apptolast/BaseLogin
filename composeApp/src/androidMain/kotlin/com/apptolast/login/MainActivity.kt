package com.apptolast.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.apptolast.customlogin.CustomLoginAndroid
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        CustomLoginAndroid.attachActivity(this)

        val splashScreen = installSplashScreen()

        // Keep splash screen visible until auth check completes
        splashScreen.setKeepOnScreenCondition {
            !splashViewModel.isReady.value
        }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(splashViewModel = splashViewModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CustomLoginAndroid.detachActivity(this)
    }
}
