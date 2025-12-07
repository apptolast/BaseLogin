package com.apptolast.customlogin.presentation.navigation

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.apptolast.customlogin.presentation.screens.welcome.WelcomeScreen
import com.apptolast.customlogin.presentation.theme.AuthScreenSlots
import com.apptolast.customlogin.presentation.screens.forgotpassword.ForgotPasswordRoute as ForgotPasswordScreen
import com.apptolast.customlogin.presentation.screens.login.LoginRoute as LoginScreen
import com.apptolast.customlogin.presentation.screens.register.RegisterRoute as RegisterScreen
import com.apptolast.customlogin.presentation.screens.resetpassword.ResetPasswordRoute as ResetPasswordScreen

/**
 * Root navigation graph for the authentication flow.
 *
 * @param startDestination The initial destination (default: WelcomeRoute)
 * @param slots Custom slots for all auth screens
 * @param onLoginSuccess Callback when login is successful
 * @param onRegisterSuccess Callback when registration is successful
 */
@Composable
fun RootNavGraph(
    startDestination: Any = WelcomeRoute,
    slots: AuthScreenSlots = AuthScreenSlots(),
    onLoginSuccess: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {}
) {
    val navController = rememberNavController()

    Surface {
        NavHost(
            navController = navController,
            startDestination = startDestination,
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
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(WelcomeRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                        onLoginSuccess()
                    },
                    onNavigateToRegister = {
                        navController.navigate(RegisterRoute) {
                            popUpTo(LoginRoute) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(ForgotPasswordRoute) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ---------- REGISTER SCREEN ----------
            composable<RegisterRoute> {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(LoginRoute) {
                            popUpTo(RegisterRoute) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                        onRegisterSuccess()
                    },
                    onNavigateToLogin = {
                        navController.navigate(LoginRoute) {
                            popUpTo(RegisterRoute) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ---------- FORGOT PASSWORD SCREEN ----------
            composable<ForgotPasswordRoute> {
                ForgotPasswordScreen(
                    slots = slots.forgotPassword,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSuccess = {
                        navController.navigate(LoginRoute) {
                            popUpTo(ForgotPasswordRoute) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ---------- RESET PASSWORD SCREEN ----------
            composable<ResetPasswordRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<ResetPasswordRoute>()
                ResetPasswordScreen(
                    resetCode = route.code,
                    slots = slots.resetPassword,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSuccess = {
                        navController.navigate(LoginRoute) {
                            popUpTo(0) {
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
