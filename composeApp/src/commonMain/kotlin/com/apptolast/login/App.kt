package com.apptolast.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.apptolast.customlogin.domain.model.UserSession
import com.apptolast.customlogin.presentation.navigation.AuthRoutesFlow
import com.apptolast.customlogin.presentation.navigation.LoginRoute
import com.apptolast.customlogin.presentation.navigation.NavTransitions
import com.apptolast.customlogin.presentation.navigation.authRoutesFlow
import com.apptolast.customlogin.presentation.theme.AuthScreenSlots
import com.apptolast.customlogin.presentation.theme.LoginScreenSlots
import com.apptolast.login.home.navigation.HomeRoute
import com.apptolast.login.home.navigation.HomeRoutesFlow
import com.apptolast.login.home.presentation.screens.HomeScreen
import com.apptolast.login.theme.SampleAppTheme
import login.composeapp.generated.resources.Res
import login.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource

/**
 * Main App composable demonstrating the CustomLogin library usage.
 */
@Composable
fun App() {
    SampleAppTheme {

        var isAuthenticated by remember { mutableStateOf(false) }
        var currentSession by remember { mutableStateOf<UserSession?>(null) }

        val startDestination =
            if (isAuthenticated && currentSession != null) HomeRoutesFlow else AuthRoutesFlow


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
                authRoutesFlow(
                    navController = navController,
                    startDestination = LoginRoute,
//                    slots = createCustomSlots(
//                        onGoogleSignIn = {
//                            // TODO: Implement Google Sign-In
//                            println("Google Sign-In clicked")
//                        }
//                    ),
                    onAuthSuccess = { navController.navigate(HomeRoutesFlow) },
                )

                homeRoutesFlow(onLogoutSuccess = { navController.navigate(AuthRoutesFlow) })
            }
        }
    }
}

private fun NavGraphBuilder.homeRoutesFlow(onLogoutSuccess: () -> Unit) {
    navigation<HomeRoutesFlow>(
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeScreen(onLogout = onLogoutSuccess)
        }
    }
}

/**
 * Create custom slots with Google Sign-In button.
 */
//private fun createCustomSlots(
//    onGoogleSignIn: () -> Unit
//) = AuthScreenSlots(
//    login = LoginScreenSlots(
//        // Custom logo
//        header = {
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                modifier = Modifier.padding(vertical = 16.dp)
//            ) {
//                Image(
//                    painter = painterResource(Res.drawable.compose_multiplatform),
//                    contentDescription = "App Logo",
//                    modifier = Modifier.size(80.dp)
//                )
//            }
//        },
//        // Social providers with Google button
//        socialProviders = { onProviderClick ->
//            Column(
//                modifier = Modifier.fillMaxWidth(),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                // Google Sign-In Button
//                GoogleSignInButton(
//                    onClick = {
//                        onGoogleSignIn()
//                        onProviderClick("google")
//                    }
//                )
//            }
//        }
//    ),
//)

/**
 * Google Sign-In button following Google's branding guidelines.
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF1F1F1F)
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Google "G" logo placeholder (you would use actual Google icon)
            Box(
                modifier = Modifier
                    .size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4285F4) // Google Blue
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Continue with Google",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
