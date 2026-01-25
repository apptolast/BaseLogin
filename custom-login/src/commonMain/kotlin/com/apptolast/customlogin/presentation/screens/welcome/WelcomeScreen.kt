package com.apptolast.customlogin.presentation.screens.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.welcome_screen_create_account_button
import login.custom_login.generated.resources.welcome_screen_sign_in_button
import login.custom_login.generated.resources.welcome_screen_subtitle
import login.custom_login.generated.resources.welcome_screen_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    var headerVisible by remember { mutableStateOf(false) }
    var buttonsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150) // Faster animation
        headerVisible = true
        delay(150) // Faster animation
        buttonsVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        // ---------- TITLE ----------
        AnimatedVisibility(
            visible = headerVisible,
            enter = fadeIn(
                animationSpec = tween(durationMillis = 500) // Faster animation
            ) + slideInHorizontally(
                initialOffsetX = { it / 4 }, // Less offset
                animationSpec = tween(durationMillis = 500) // Faster animation
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(Res.string.welcome_screen_title) + " \uD83D\uDE04",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(Res.string.welcome_screen_subtitle),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        AnimatedVisibility(
            visible = buttonsVisible,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            enter = fadeIn(
                animationSpec = tween(durationMillis = 400) // Faster animation
            ) + slideInHorizontally(
                initialOffsetX = { it / 5 }, // Less offset
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Primary button - Create Account
                Button(
                    onClick = onNavigateToRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.welcome_screen_create_account_button),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary button - Sign In
                OutlinedButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.welcome_screen_sign_in_button),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(
        onNavigateToLogin = {},
        onNavigateToRegister = {},
    )
}