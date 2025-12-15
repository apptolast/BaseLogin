package com.apptolast.customlogin.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.apptolast.customlogin.domain.model.UserSession
import com.apptolast.customlogin.presentation.screens.forgotpassword.ForgotPasswordScreen
import com.apptolast.customlogin.presentation.screens.login.LoginScreen
import com.apptolast.customlogin.presentation.screens.register.RegisterScreen
import com.apptolast.customlogin.presentation.screens.resetpassword.ResetPasswordScreen
import com.apptolast.customlogin.presentation.screens.welcome.WelcomeScreen
import com.apptolast.customlogin.presentation.theme.AuthScreenSlots

/**
 * Root navigation graph for the authentication flow.
 *
 * @param navController To handle navigation with the graph
 * @param startDestination The initial destination (default: WelcomeRoute)
 * @param slots Custom slots for all auth screens
 * @param onAuthSuccess Callback when login is successful
 */
fun NavGraphBuilder.authRoutesFlow(
    navController: NavHostController,
    startDestination: Any = WelcomeRoute,
    slots: AuthScreenSlots = AuthScreenSlots(),
    onAuthSuccess: (UserSession) -> Unit = {},
) {

    navigation<AuthRoutesFlow>(startDestination = startDestination) {

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
                loginSlots = slots.login,
                onAuthSuccess = onAuthSuccess,
                onNavigateToRegister = {
                    navController.navigate(RegisterRoute) {
                        popUpTo(LoginRoute) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToResetPassword = {
                    // Navigate to the new Forgot Password screen
                    navController.navigate(ForgotPasswordRoute) {
                        launchSingleTop = true
                    }
                },
            )
        }

        // ---------- REGISTER SCREEN ----------
        composable<RegisterRoute> {
            RegisterScreen(
                registerSlots = slots.register,
                onAuthSuccess = onAuthSuccess,
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
                }
            )
        }

        // ---------- RESET PASSWORD SCREEN ----------
        composable<ResetPasswordRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ResetPasswordRoute>()
            ResetPasswordScreen(
                resetCode = route.resetCode,
                resetPasswordSlots = slots.resetPassword,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.navigate(LoginRoute) {
                        popUpTo(AuthRoutesFlow) { // Pop up to the auth graph
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
