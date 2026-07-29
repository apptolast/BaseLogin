package com.apptolast.customlogin.presentation.slots.defaultslots

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.domain.model.IdentityProvider
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.apple_icon
import login.custom_login.generated.resources.facebook_icon
import login.custom_login.generated.resources.github_icon
import login.custom_login.generated.resources.google_icon
import login.custom_login.generated.resources.login_apple_button
import login.custom_login.generated.resources.login_facebook_button
import login.custom_login.generated.resources.login_github_button
import login.custom_login.generated.resources.login_google_button
import login.custom_login.generated.resources.login_magic_link_button
import login.custom_login.generated.resources.login_microsoft_button
import login.custom_login.generated.resources.login_phone_button
import login.custom_login.generated.resources.login_twitter_button
import login.custom_login.generated.resources.microsoft_icon
import login.custom_login.generated.resources.twitter_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * A circular icon-only button for a social authentication provider.
 *
 * Shows a [CircularProgressIndicator] while [isLoading] is true and disables
 * interaction when [enabled] is false. [contentDescription] is used for
 * accessibility; it is not rendered on screen.
 */
@Composable
internal fun DefaultIconSocialButton(
    contentDescription: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(52.dp),
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                Icon(
                    painter = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(24.dp),
                    tint = tint,
                )
            }
        }
    }
}

@Composable
fun GoogleIconButton(isLoading: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DefaultIconSocialButton(
        contentDescription = stringResource(Res.string.login_google_button),
        icon = painterResource(Res.drawable.google_icon),
        onClick = onClick,
        isLoading = isLoading,
        enabled = enabled,
    )
}

@Composable
fun AppleIconButton(isLoading: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DefaultIconSocialButton(
        contentDescription = stringResource(Res.string.login_apple_button),
        icon = painterResource(Res.drawable.apple_icon),
        onClick = onClick,
        tint = MaterialTheme.colorScheme.onSurface,
        isLoading = isLoading,
        enabled = enabled,
    )
}

@Composable
fun GitHubIconButton(isLoading: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DefaultIconSocialButton(
        contentDescription = stringResource(Res.string.login_github_button),
        icon = painterResource(Res.drawable.github_icon),
        onClick = onClick,
        tint = MaterialTheme.colorScheme.onSurface,
        isLoading = isLoading,
        enabled = enabled,
    )
}

@Composable
fun MicrosoftIconButton(isLoading: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DefaultIconSocialButton(
        contentDescription = stringResource(Res.string.login_microsoft_button),
        icon = painterResource(Res.drawable.microsoft_icon),
        onClick = onClick,
        isLoading = isLoading,
        enabled = enabled,
    )
}

@Composable
fun MagicLinkIconButton(isLoading: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DefaultIconSocialButton(
        contentDescription = stringResource(Res.string.login_magic_link_button),
        icon = rememberVectorPainter(Icons.Default.Email),
        onClick = onClick,
        tint = MaterialTheme.colorScheme.onSurface,
        isLoading = isLoading,
        enabled = enabled,
    )
}

@Composable
fun PhoneIconButton(isLoading: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DefaultIconSocialButton(
        contentDescription = stringResource(Res.string.login_phone_button),
        icon = rememberVectorPainter(Icons.Default.Phone),
        onClick = onClick,
        tint = MaterialTheme.colorScheme.onSurface,
        isLoading = isLoading,
        enabled = enabled,
    )
}

@Composable
fun TwitterIconButton(isLoading: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DefaultIconSocialButton(
        contentDescription = stringResource(Res.string.login_twitter_button),
        icon = painterResource(Res.drawable.twitter_icon),
        onClick = onClick,
        tint = MaterialTheme.colorScheme.onSurface,
        isLoading = isLoading,
        enabled = enabled,
    )
}

@Composable
fun FacebookIconButton(isLoading: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DefaultIconSocialButton(
        contentDescription = stringResource(Res.string.login_facebook_button),
        icon = painterResource(Res.drawable.facebook_icon),
        onClick = onClick,
        isLoading = isLoading,
        enabled = enabled,
    )
}

/**
 * Renders social login providers as circular icon-only buttons arranged in a
 * centred, automatically-wrapping row.
 *
 * This is the **default** slot used by [com.apptolast.customlogin.presentation.slots.LoginScreenSlots]
 * and [com.apptolast.customlogin.presentation.slots.RegisterScreenSlots].
 * For full-width text-labelled buttons, use [SocialLoginButtonsSection] instead.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IconSocialLoginButtonsSection(
    providers: List<IdentityProvider>,
    loadingProvider: IdentityProvider? = null,
    onProviderClick: (IdentityProvider) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val isEnabled = loadingProvider == null
        providers.forEach { provider ->
            val isThisLoading = loadingProvider == provider
            when (provider) {
                is IdentityProvider.Google -> GoogleIconButton(isThisLoading, isEnabled) { onProviderClick(provider) }
                is IdentityProvider.Apple -> AppleIconButton(isThisLoading, isEnabled) { onProviderClick(provider) }
                is IdentityProvider.GitHub -> GitHubIconButton(isThisLoading, isEnabled) { onProviderClick(provider) }
                is IdentityProvider.Microsoft -> MicrosoftIconButton(isThisLoading, isEnabled) {
                    onProviderClick(provider)
                }
                is IdentityProvider.MagicLink -> MagicLinkIconButton(isThisLoading, isEnabled) {
                    onProviderClick(provider)
                }
                is IdentityProvider.Phone -> PhoneIconButton(isThisLoading, isEnabled) { onProviderClick(provider) }
                is IdentityProvider.Twitter -> TwitterIconButton(isThisLoading, isEnabled) { onProviderClick(provider) }
                is IdentityProvider.Facebook -> FacebookIconButton(isThisLoading, isEnabled) {
                    onProviderClick(provider)
                }
                is IdentityProvider.Custom -> {}
            }
        }
    }
}
