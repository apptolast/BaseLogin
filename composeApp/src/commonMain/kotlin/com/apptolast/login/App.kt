package com.apptolast.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.apptolast.login.home.presentation.home.HomeScreen
import com.apptolast.login.theme.SampleAppTheme

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
                    slots = createCustomSlots(), // Pass our custom slots here
                    onAuthSuccess = { userSession ->
                        isAuthenticated = true
                        currentSession = userSession
                        navController.navigate(HomeRoutesFlow)
                    },
                )

                homeRoutesFlow(
                    userSession = currentSession,
                    onLogoutSuccess = {
                        isAuthenticated = false
                        currentSession = null
                        navController.navigate(AuthRoutesFlow)
                    }
                )
            }
        }
    }
}

private fun NavGraphBuilder.homeRoutesFlow(userSession: UserSession?, onLogoutSuccess: () -> Unit) {
    navigation<HomeRoutesFlow>(
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToAuth = onLogoutSuccess,
            )
        }
    }
}

/**
 * Creates a custom slot configuration for the authentication screens.
 */
private fun createCustomSlots() = AuthScreenSlots(
    login = LoginScreenSlots(
        // We override ONLY the submitButton slot.
        // All other slots (header, emailField, etc.) will use the default
        // implementation provided by the custom-login module.
        submitButton = { onClick, isLoading, enabled, text ->
            MyCustomSubmitButton(onClick, isLoading, enabled, text)
        },
        socialProviders = { onClick ->
            GoogleSignInButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
)

/**
 * A custom submit button with a different style.
 * It MUST have the same signature as the slot it replaces.
 */
@Composable
fun MyCustomSubmitButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean,
    text: String
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp), // Taller button
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp), // More rounded corners
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary, // Use secondary color
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onSecondary
            )
        } else {
            Text(
                text = text.uppercase(), // Uppercase text
                style = MaterialTheme.typography.titleMedium // Larger text
            )
        }
    }
}

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
            Box(
                modifier = Modifier.size(20.dp),
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
