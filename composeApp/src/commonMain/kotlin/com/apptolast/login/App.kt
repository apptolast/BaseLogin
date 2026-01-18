package com.apptolast.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.apptolast.customlogin.domain.model.SocialProvider
import com.apptolast.customlogin.domain.model.UserSession
import com.apptolast.customlogin.presentation.navigation.AuthRoutesFlow
import com.apptolast.customlogin.presentation.navigation.LoginRoute
import com.apptolast.customlogin.presentation.navigation.NavTransitions
import com.apptolast.customlogin.presentation.navigation.authRoutesFlow
import com.apptolast.customlogin.presentation.theme.AuthScreenSlots
import com.apptolast.customlogin.presentation.theme.LoginScreenSlots
import com.apptolast.customlogin.presentation.theme.defaultslots.GoogleSocialButton
import com.apptolast.customlogin.presentation.theme.defaultslots.PhoneSocialButton
import com.apptolast.login.home.navigation.HomeRoute
import com.apptolast.login.home.navigation.HomeRoutesFlow
import com.apptolast.login.home.presentation.home.ProfileScreen
import com.apptolast.login.splash.SplashState
import com.apptolast.login.splash.SplashViewModel
import com.apptolast.login.theme.SampleAppTheme
import login.composeapp.generated.resources.Res
import login.composeapp.generated.resources.google_icon
import login.composeapp.generated.resources.login_google_button
import login.composeapp.generated.resources.login_loading_text
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main App composable demonstrating the CustomLogin library usage.
 * @param splashViewModel Optional ViewModel for splash screen integration (Android only).
 */
@Composable
fun App(splashViewModel: SplashViewModel? = koinViewModel()) {

    SampleAppTheme {

        val splashState by splashViewModel?.splashState?.collectAsStateWithLifecycle()
            ?: remember { mutableStateOf(SplashState.Unauthenticated) }

        // Don't render anything until we know the actual auth state
        // This prevents the "flash" of login screen when user is authenticated
        if (splashState is SplashState.Loading) return@SampleAppTheme

        // Initialize auth state from splash screen check (now we have definitive state)
        var isAuthenticated by remember(splashState) {
            mutableStateOf(splashState is SplashState.Authenticated)
        }
        var currentSession by remember(splashState) {
            mutableStateOf((splashState as? SplashState.Authenticated)?.session)
        }

        val startDestination = if (isAuthenticated) HomeRoutesFlow else AuthRoutesFlow

        val navController = rememberNavController()

        Surface {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = { NavTransitions.enter },
                exitTransition = { NavTransitions.exit },
                popEnterTransition = { NavTransitions.popEnter },
                popExitTransition = { NavTransitions.popExit },
            ) {

                authRoutesFlow(
                    navController = navController,
                    startDestination = LoginRoute,
//                    slots = createCustomSlots(),
                    onAuthSuccess = { userSession ->
                        currentSession = userSession
                        navController.navigate(HomeRoutesFlow) {
                            popUpTo(AuthRoutesFlow) { inclusive = true }
                        }
                    },
                )

                homeRoutesFlow(
                    userSession = currentSession,
                    onLogoutSuccess = {
                        navController.navigate(AuthRoutesFlow) {
                            popUpTo(HomeRoutesFlow) { inclusive = true }
                        }
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
            ProfileScreen(
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
        socialProviders = { onProviderClick ->
            Column {
                SignInWithGoogleButton { onProviderClick(SocialProvider.Google) }
                Spacer(Modifier.height(8.dp))
                PhoneSocialButton { onProviderClick(SocialProvider.Phone) }
            }
        }
    )
)

/**
 * A custom submit button with a different style.
 * It MUST have the same signature as the slot it replaces.
 */
@Composable
fun MyCustomSubmitButton(
    text: String,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .clickable(
                enabled = !isLoading,
                onClick = onClick,
            )
            .padding(vertical = 16.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(0.7f),
            enabled = enabled && !isLoading,
            shape = MaterialTheme.shapes.small,
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
}

/**
 * Google Sign-In button following Google's branding guidelines.
 */
@Composable
fun SignInWithGoogleButton(
    text: String = stringResource(Res.string.login_google_button),
    loadingText: String = stringResource(Res.string.login_loading_text),
    icon: Painter = painterResource(Res.drawable.google_icon),
    isLoading: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .clickable(
                enabled = !isLoading,
                onClick = onClick,
            ),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(
                start = 12.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = icon,
                contentDescription = "Google Button",
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isLoading) loadingText else text,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
