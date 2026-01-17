package com.apptolast.customlogin.presentation.theme.defaultslots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.domain.model.SocialProvider

/**
 * Represents a social login button configuration.
 *
 * @param provider The social provider associated with this button.
 * @param content The composable content of the button.
 */
data class SocialLoginButton(
    val provider: SocialProvider,
    val content: @Composable (onClick: () -> Unit) -> Unit
)

/**
 * A default section that displays social login buttons in a lazy list.
 * This is an optional composable that you can use in your `socialProviders` slot.
 *
 * @param onProviderClick A callback invoked when a social provider button is clicked.
 */
@Composable
fun SocialLoginButtonsSection(onProviderClick: (SocialProvider) -> Unit) {
    val socialButtons = defaultSocialLoginButtons()

    LazyColumn(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(socialButtons) { button ->
            button.content {
                onProviderClick(button.provider)
            }
        }
    }
}

/**
 * Provides a default list of social login buttons (Google and Phone).
 */
@Composable
fun defaultSocialLoginButtons(): List<SocialLoginButton> = listOf(
    SocialLoginButton(
        provider = SocialProvider.Google,
        content = { onClick -> GoogleSocialButton(onClick = onClick) }
    ),
    SocialLoginButton(
        provider = SocialProvider.Phone,
        content = { onClick -> PhoneSocialButton(onClick = onClick) }
    )
)

/**
 * A default implementation of a Google social login button.
 */
@Composable
fun GoogleSocialButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text("Sign in with Google")
    }
}

/**
 * A default implementation of a phone social login button.
 */
@Composable
fun PhoneSocialButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text("Sign in with Phone")
    }
}
