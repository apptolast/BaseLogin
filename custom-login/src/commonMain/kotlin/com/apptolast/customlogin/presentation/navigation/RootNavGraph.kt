package com.apptolast.customlogin.presentation.navigation

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.apptolast.customlogin.presentation.screens.login.LoginRoute
import com.apptolast.customlogin.presentation.screens.welcome.WelcomeScreen

@Composable
fun RootNavGraph() {
    val navController = rememberNavController()
    val startDestination = AuthGraph

    // The animation spec must be for an IntOffset, not an Int.
    val animationSpec = tween<IntOffset>(durationMillis = 400, easing = EaseInOut)

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        navigation<AuthGraph>(
            startDestination = WelcomeRoute,
        ) {
            composable<WelcomeRoute>(
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = animationSpec) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = animationSpec) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = animationSpec) }
            ) {
                WelcomeScreen(
                    onNavigateToLogin = { navController.navigate(LoginRoute) },
                    onNavigateToRegister = { /* navController.navigate(RegisterRoute) */ }
                )
            }
            composable<LoginRoute>(
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = animationSpec) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = animationSpec) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = animationSpec) }
            ) {
                LoginRoute(
                    onLoginSuccess = {
                        navController.navigate(HomeGraph) {
                            popUpTo(AuthGraph) { inclusive = true }
                        }
                    }
                )
            }
            // TODO: Add composable<RegisterRoute> with similar animations
        }

//        navigation<HomeGraph>(startDestination = HomeRoute) {
            // ...
//        }
    }
}
