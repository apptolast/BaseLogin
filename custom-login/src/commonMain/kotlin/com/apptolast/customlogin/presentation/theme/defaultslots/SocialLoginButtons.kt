package com.apptolast.customlogin.presentation.theme.defaultslots

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.domain.model.SocialProvider
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.google_icon
import login.custom_login.generated.resources.login_google_button
import login.custom_login.generated.resources.login_loading_text
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
 * Google Sign-In button following Google's branding guidelines.
 */
@Composable
fun GoogleSocialButton(
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

/**
 * A default implementation of a phone social login button.
 */
@Composable
fun PhoneSocialButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text("Sign in with Phone")
    }
}
