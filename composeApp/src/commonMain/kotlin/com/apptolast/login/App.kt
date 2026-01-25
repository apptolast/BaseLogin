package com.apptolast.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.presentation.navigation.AuthRoutesFlow
import com.apptolast.customlogin.presentation.navigation.LoginRoute
import com.apptolast.customlogin.presentation.navigation.NavTransitions
import com.apptolast.customlogin.presentation.navigation.authRoutesFlow
import com.apptolast.login.presentation.navigation.MainRoutesFlow
import com.apptolast.login.presentation.navigation.mainRoutesFlow
import com.apptolast.login.theme.SampleAppTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main App composable that orchestrates the entire navigation based on authentication state.
 */
@Composable
fun App(splashViewModel: SplashViewModel = koinViewModel()) {
    // Configure Coil singleton ImageLoader with Ktor network support
    setSingletonImageLoaderFactory { context ->
        coil3.ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }

    SampleAppTheme {

        Surface(modifier = Modifier.fillMaxSize()) {

            val authState by splashViewModel.authState.collectAsStateWithLifecycle()

            // AnimatedContent provides smooth transitions when the authentication state changes.
            AnimatedContent(
                targetState = authState is AuthState.Authenticated,
                transitionSpec = {
                    // Use the shared slide transitions for a consistent feel.
                    NavTransitions.enter togetherWith NavTransitions.exit
                },
                label = "RootNavigationAnimation"
            ) { isAuthenticated ->
                if (isAuthenticated) {
                    MainAppNavigation()
                } else {
                    // This will be shown for Loading, Unauthenticated, and Error states.
                    // The splash screen condition in MainActivity will handle hiding the app content
                    // while loading for the first time.
                    AuthNavigation()
                }
            }
        }
    }
}

@Composable
private fun AuthNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AuthRoutesFlow,
        enterTransition = { NavTransitions.enter },
        exitTransition = { NavTransitions.exit },
        popEnterTransition = { NavTransitions.popEnter },
        popExitTransition = { NavTransitions.popExit },
    ) {
        authRoutesFlow(
            navController = navController,
            startDestination = LoginRoute,
            onNavigateToHome = { /* Handled by AuthState change */ },
        )
    }
}

@Composable
private fun MainAppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = MainRoutesFlow,
    ) {
        mainRoutesFlow()
    }
}
