package com.apptolast.customlogin.presentation.navigation

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apptolast.customlogin.presentation.screens.login.LoginRoute
import com.apptolast.customlogin.presentation.screens.register.RegisterRoute
import com.apptolast.customlogin.presentation.screens.welcome.WelcomeScreen

@Composable
fun RootNavGraph(
    onLoginSuccess: () -> Unit = {}
) {
    val navController = rememberNavController()

    Surface {
        NavHost(
            navController = navController,
            startDestination = WelcomeRoute,
            enterTransition = { NavTransitions.enter },
            exitTransition = { NavTransitions.exit },
            popEnterTransition = { NavTransitions.popEnter },
            popExitTransition = { NavTransitions.popExit }
        ) {

            // ---------- WELCOME SCREEN ----------
            composable<WelcomeRoute> {
                WelcomeScreen(
                    onNavigateToLogin = {
                        navController.navigate(LoginRoute) {
                            // No añadir Welcome al back stack
                            // Para que al presionar back desde Login no vuelva a Welcome
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(RegisterRoute) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ---------- LOGIN SCREEN ----------
            composable<LoginRoute> {
                LoginRoute(
                    onLoginSuccess = {
                        // Limpiar el back stack y notificar al consumidor
                        navController.navigate(WelcomeRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                        onLoginSuccess()
                    },
                    onNavigateToRegister = {
                        // Si ya estamos en Login y queremos ir a Register
                        navController.navigate(RegisterRoute) {
                            // Reemplazar Login con Register
                            popUpTo(LoginRoute) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ---------- REGISTER SCREEN ----------
            composable<RegisterRoute> {
                RegisterRoute(
                    onRegisterSuccess = {
                        // Después de registrarse exitosamente, ir a login
                        navController.navigate(LoginRoute) {
                            popUpTo(RegisterRoute) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLogin = {
                        // Si ya tiene cuenta, volver a Login
                        navController.navigate(LoginRoute) {
                            popUpTo(RegisterRoute) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
