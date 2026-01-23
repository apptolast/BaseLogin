package com.apptolast.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.presentation.navigation.AuthRoutesFlow
import com.apptolast.customlogin.presentation.navigation.LoginRoute
import com.apptolast.customlogin.presentation.navigation.NavTransitions
import com.apptolast.customlogin.presentation.navigation.authRoutesFlow
import com.apptolast.customlogin.presentation.slots.AuthScreenSlots
import com.apptolast.customlogin.presentation.slots.LoginScreenSlots
import com.apptolast.customlogin.presentation.slots.defaultslots.GoogleSocialButton
import com.apptolast.customlogin.presentation.slots.defaultslots.PhoneSocialButton
import com.apptolast.login.home.navigation.HomeRoute
import com.apptolast.login.home.navigation.HomeRoutesFlow
import com.apptolast.login.home.presentation.home.ProfileScreen
import com.apptolast.login.splash.SplashViewModel
import com.apptolast.login.theme.SampleAppTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main App composable that orchestrates the entire navigation based on authentication state.
 */
@Composable
fun App(splashViewModel: SplashViewModel = koinViewModel()) {

    SampleAppTheme {

        val authState by splashViewModel.authState.collectAsStateWithLifecycle()
        val navController = rememberNavController()

        // This is the single source of truth for navigation between the two main flows.
        // It reacts to any change in the authentication state while the app is running.
        LaunchedEffect(authState) {
            val currentGraph = navController.currentDestination?.parent?.route

            if (authState is AuthState.Authenticated && currentGraph != HomeRoutesFlow.serializer().descriptor.serialName) {
                navController.navigate(HomeRoutesFlow) {
                    popUpTo(AuthRoutesFlow) { inclusive = true }
                }
            } else if (authState is AuthState.Unauthenticated && currentGraph != AuthRoutesFlow.serializer().descriptor.serialName) {
                navController.navigate(AuthRoutesFlow) {
                    popUpTo(HomeRoutesFlow) { inclusive = true }
                }
            }
        }

        // The NavHost is now simpler. Its job is just to host the graphs.
        // The `startDestination` is determined once based on the initial auth state.
        if (authState !is AuthState.Loading) {
            Surface {
                NavHost(
                    navController = navController,
                    startDestination = if (authState is AuthState.Authenticated) HomeRoutesFlow else AuthRoutesFlow,
                    enterTransition = { NavTransitions.enter },
                    exitTransition = { NavTransitions.exit },
                    popEnterTransition = { NavTransitions.popEnter },
                    popExitTransition = { NavTransitions.popExit },
                ) {
                    // The auth flow graph. `onNavigateToHome` is no longer needed
                    // as the global LaunchedEffect handles it.
                    authRoutesFlow(
                        navController = navController,
                        startDestination = LoginRoute,
//                        slots = createCustomSlots(),
                        onNavigateToHome = { /* Handled globally by authState */ },
                    )

                    // The main app (home) flow graph.
                    homeRoutesFlow(navController)
                }
            }
        }
        // While loading, we implicitly show the splash screen because nothing is composed here.
    }
}

private fun NavGraphBuilder.homeRoutesFlow(navController: NavHostController) {
    navigation<HomeRoutesFlow>(
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            // ProfileScreen is now self-contained and doesn't need navigation callbacks.
            ProfileScreen()
        }
    }
}

/**
 * Creates a custom slot configuration for the authentication screens.
 */
private fun createCustomSlots() = AuthScreenSlots(
    login = LoginScreenSlots(
        socialProviders = { onProviderClick ->
            Column {
                GoogleSocialButton { onProviderClick(IdentityProvider.Google) }
                Spacer(Modifier.height(8.dp))
                PhoneSocialButton { onProviderClick(IdentityProvider.Phone) }
            }
        }
    )
)
