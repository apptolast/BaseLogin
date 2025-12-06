package com.apptolast.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import com.apptolast.customlogin.CustomLogin
import com.apptolast.customlogin.domain.model.UserSession
import com.apptolast.customlogin.presentation.theme.AuthColors
import com.apptolast.customlogin.presentation.theme.AuthScreenSlots
import com.apptolast.customlogin.presentation.theme.AuthShapes
import com.apptolast.customlogin.presentation.theme.AuthSpacing
import com.apptolast.customlogin.presentation.theme.AuthTheme
import com.apptolast.customlogin.presentation.theme.AuthTypography
import com.apptolast.customlogin.presentation.theme.LoginScreenSlots
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

        if (isAuthenticated && currentSession != null) {
            // User is logged in - show home screen
            HomeScreen(
                session = currentSession!!,
                onLogout = {
                    isAuthenticated = false
                    currentSession = null
                }
            )
        } else {
            // Show authentication flow
            CustomLogin.AuthFlow(
                theme = createCustomTheme(),
                slots = createCustomSlots(
                    onGoogleSignIn = {
                        // TODO: Implement Google Sign-In
                        println("Google Sign-In clicked")
                    }
                ),
                showWelcome = false,
                onAuthSuccess = {
                    isAuthenticated = true
                    // In a real app, get the session from the repository
                    currentSession = UserSession(
                        userId = "demo-user",
                        email = "user@example.com",
                        displayName = "Demo User"
                    )
                }
            )
        }
    }
}

/**
 * Create a custom theme for the auth screens.
 */
@Composable
private fun createCustomTheme(): AuthTheme {
    return AuthTheme(
        colors = AuthColors.Light.copy(
            primary = MaterialTheme.colorScheme.primary,
            onPrimary = Color.White,
            secondary = Color(0xFF03DAC6),
            link = Color(0xFF1976D2)
        ),
        typography = AuthTypography.Default,
        shapes = AuthShapes.Default,
        spacing = AuthSpacing.Default
    )
}

/**
 * Create custom slots with Google Sign-In button.
 */
private fun createCustomSlots(
    onGoogleSignIn: () -> Unit
): AuthScreenSlots {
    return AuthScreenSlots(
        login = LoginScreenSlots(
            // Custom logo
            logo = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Image(
                        painter = painterResource(Res.drawable.compose_multiplatform),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(80.dp)
                    )
                }
            },
            // Social providers with Google button
            socialProviders = { onProviderClick ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Google Sign-In Button
                    GoogleSignInButton(
                        onClick = {
                            onGoogleSignIn()
                            onProviderClick("google")
                        }
                    )
                }
            }
        )
    )
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

/**
 * Home screen shown after successful authentication.
 */
@Composable
fun HomeScreen(
    session: UserSession,
    onLogout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎉 Welcome!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = session.displayName ?: "User",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = session.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "You are now logged in!",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(0.6f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Sign Out")
            }
        }
    }
}