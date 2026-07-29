package com.apptolast.customlogin.presentation.slots.defaultslots

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
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
 * A generic, styled button for social login providers.
 * Takes an optional tint parameter. Defaults to Unspecified to support multi-color icons (e.g. Google).
 */
@Composable
internal fun DefaultSocialButton(
    text: String,
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
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = tint,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun GoogleSocialButton(isLoading: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DefaultSocialButton(
        text = stringResource(Res.string.login_google_button),
        icon = painterResource(Res.drawable.google_icon),
        onClick = onClick,
        isLoading = isLoading,
        enabled = enabled,
    )
}

@Composable
fun AppleSocialButton(isLoading: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DefaultSocialButton(
        text = stringResource(Res.string.login_apple_button),
        icon = painterResource(Res.drawable.apple_icon),
        onClick = onClick,
        tint = MaterialTheme.colorScheme.onSurface,
        isLoading = isLoading,
        enabled = enabled,
    )
}

@Composable
fun GitHubSocialButton(isLoading: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DefaultSocialButton(
        text = stringResource(Res.string.login_github_button),
        icon = painterResource(Res.drawable.github_icon),
        onClick = onClick,
        tint = MaterialTheme.colorScheme.onSurface,
        isLoading = isLoading,
        enabled = enabled,
    )
}

@Composable
fun MicrosoftSocialButton(isLoading: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DefaultSocialButton(
        text = stringResource(Res.string.login_microsoft_button),
        icon = painterResource(Res.drawable.microsoft_icon),
        onClick = onClick,
        isLoading = isLoading,
        enabled = enabled,
    )
}

@Composable
fun MagicLinkSocialButton(isLoading: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DefaultSocialButton(
        text = stringResource(Res.string.login_magic_link_button),
        icon = rememberVectorPainter(Icons.Default.Email),
        onClick = onClick,
        tint = MaterialTheme.colorScheme.onSurface,
        isLoading = isLoading,
        enabled = enabled,
    )
}

@Composable
fun PhoneSocialButton(isLoading: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DefaultSocialButton(
        text = stringResource(Res.string.login_phone_button),
        icon = rememberVectorPainter(image = Icons.Default.Phone),
        onClick = onClick,
        tint = MaterialTheme.colorScheme.onSurface,
        isLoading = isLoading,
        enabled = enabled,
    )
}

@Composable
fun TwitterSocialButton(isLoading: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DefaultSocialButton(
        text = stringResource(Res.string.login_twitter_button),
        icon = painterResource(Res.drawable.twitter_icon),
        onClick = onClick,
        tint = MaterialTheme.colorScheme.onSurface,
        isLoading = isLoading,
        enabled = enabled,
    )
}

@Composable
fun FacebookSocialButton(isLoading: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DefaultSocialButton(
        text = stringResource(Res.string.login_facebook_button),
        icon = painterResource(Res.drawable.facebook_icon),
        onClick = onClick,
        isLoading = isLoading,
        enabled = enabled,
    )
}

/**
 * Renders social login buttons for the given [providers] list.
 * [loadingProvider] is the provider currently signing in; its button shows a spinner and all
 * others are disabled to prevent concurrent auth requests.
 */
@Composable
fun SocialLoginButtonsSection(
    providers: List<IdentityProvider>,
    loadingProvider: IdentityProvider? = null,
    onProviderClick: (IdentityProvider) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        providers.forEachIndexed { index, provider ->
            if (index > 0) Spacer(Modifier.height(10.dp))
            val isThisLoading = loadingProvider == provider
            val isEnabled = loadingProvider == null
            when (provider) {
                is IdentityProvider.Google -> GoogleSocialButton(isThisLoading, isEnabled) { onProviderClick(provider) }
                is IdentityProvider.Apple -> AppleSocialButton(isThisLoading, isEnabled) { onProviderClick(provider) }
                is IdentityProvider.GitHub -> GitHubSocialButton(isThisLoading, isEnabled) { onProviderClick(provider) }
                is IdentityProvider.Microsoft -> MicrosoftSocialButton(isThisLoading, isEnabled) {
                    onProviderClick(provider)
                }
                is IdentityProvider.MagicLink -> MagicLinkSocialButton(isThisLoading, isEnabled) {
                    onProviderClick(provider)
                }
                is IdentityProvider.Phone -> PhoneSocialButton(isThisLoading, isEnabled) { onProviderClick(provider) }
                is IdentityProvider.Twitter -> TwitterSocialButton(isThisLoading, isEnabled) {
                    onProviderClick(provider)
                }
                is IdentityProvider.Facebook -> FacebookSocialButton(isThisLoading, isEnabled) {
                    onProviderClick(provider)
                }
                is IdentityProvider.Custom -> {} // Custom providers are not shown in the default UI
            }
        }
    }
}
