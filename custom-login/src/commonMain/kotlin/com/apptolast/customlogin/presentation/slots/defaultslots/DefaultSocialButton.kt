package com.apptolast.customlogin.presentation.slots.defaultslots

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.domain.model.IdentityProvider
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.google_icon
import login.custom_login.generated.resources.login_apple_button
import login.custom_login.generated.resources.login_github_button
import login.custom_login.generated.resources.login_google_button
import login.custom_login.generated.resources.login_magic_link_button
import login.custom_login.generated.resources.login_microsoft_button
import login.custom_login.generated.resources.login_phone_button
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
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(
            painter = icon,
            contentDescription = null, // text is the label; icon is decorative
            modifier = Modifier.size(20.dp),
            tint = tint
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun GoogleSocialButton(onClick: () -> Unit) {
    DefaultSocialButton(
        text = stringResource(Res.string.login_google_button),
        icon = painterResource(Res.drawable.google_icon),
        onClick = onClick,
    )
}

@Composable
fun AppleSocialButton(onClick: () -> Unit) {
    DefaultSocialButton(
        text = stringResource(Res.string.login_apple_button),
        icon = rememberVectorPainter(Icons.Default.Smartphone),
        onClick = onClick,
        tint = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun GitHubSocialButton(onClick: () -> Unit) {
    DefaultSocialButton(
        text = stringResource(Res.string.login_github_button),
        icon = rememberVectorPainter(Icons.Default.Code),
        onClick = onClick,
        tint = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun MicrosoftSocialButton(onClick: () -> Unit) {
    DefaultSocialButton(
        text = stringResource(Res.string.login_microsoft_button),
        icon = rememberVectorPainter(Icons.Default.Business),
        onClick = onClick,
        tint = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun MagicLinkSocialButton(onClick: () -> Unit) {
    DefaultSocialButton(
        text = stringResource(Res.string.login_magic_link_button),
        icon = rememberVectorPainter(Icons.Default.Email),
        onClick = onClick,
        tint = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun PhoneSocialButton(onClick: () -> Unit) {
    DefaultSocialButton(
        text = stringResource(Res.string.login_phone_button),
        icon = rememberVectorPainter(image = Icons.Default.Phone),
        onClick = onClick,
        tint = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Renders social login buttons for the given [providers] list.
 * The order of buttons matches the order of [providers].
 */
@Composable
fun SocialLoginButtonsSection(
    providers: List<IdentityProvider>,
    onProviderClick: (IdentityProvider) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        providers.forEachIndexed { index, provider ->
            if (index > 0) Spacer(Modifier.height(10.dp))
            when (provider) {
                is IdentityProvider.Google -> GoogleSocialButton { onProviderClick(provider) }
                is IdentityProvider.Apple -> AppleSocialButton { onProviderClick(provider) }
                is IdentityProvider.GitHub -> GitHubSocialButton { onProviderClick(provider) }
                is IdentityProvider.Microsoft -> MicrosoftSocialButton { onProviderClick(provider) }
                is IdentityProvider.MagicLink -> MagicLinkSocialButton { onProviderClick(provider) }
                is IdentityProvider.Phone -> PhoneSocialButton { onProviderClick(provider) }
                else -> {} // Facebook, Custom: not shown in default UI
            }
        }
    }
}
