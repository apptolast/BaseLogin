package com.apptolast.customlogin.presentation.slots.defaultslots

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.presentation.screens.login.LoginLoadingState
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.google_icon
import org.jetbrains.compose.resources.painterResource

/**
 * A composable that arranges multiple social login buttons, handling loading states.
 * This is the default implementation for the `socialProviders` slot.
 */
@Composable
fun SocialLoginButtonsSection(
    loadingState: LoginLoadingState,
    onProviderClick: (IdentityProvider) -> Unit,
    providers: List<IdentityProvider> = listOf(IdentityProvider.Google, IdentityProvider.Apple, IdentityProvider.Phone)
) {
    val isAnyLoading = loadingState !is LoginLoadingState.Idle

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        providers.forEach { provider ->
            val isLoading = loadingState is LoginLoadingState.SocialSignIn && loadingState.provider == provider

            when (provider) {
                is IdentityProvider.Google -> DefaultSocialButton(
                    text = "Sign in with Google",
                    icon = painterResource(Res.drawable.google_icon),
                    onClick = { onProviderClick(provider) },
                    isLoading = isLoading,
                    enabled = !isAnyLoading
                )
                is IdentityProvider.Apple -> DefaultSocialButton(
                    text = "Sign in with Apple",
                    // TODO: Replace with a proper Apple icon resource
                    icon = rememberVectorPainter(image = Icons.Default.Phone),
                    onClick = { onProviderClick(provider) },
                    isLoading = isLoading,
                    enabled = !isAnyLoading,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                is IdentityProvider.Phone -> DefaultSocialButton(
                    text = "Sign in with Phone",
                    icon = rememberVectorPainter(image = Icons.Default.Phone),
                    onClick = { onProviderClick(provider) },
                    isLoading = isLoading,
                    enabled = !isAnyLoading,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                // TODO: Add other buttons like GitHub, etc. here following the same pattern
                else -> {}
            }
        }
    }
}

/**
 * A generic, styled button for social login providers.
 * It now supports a loading state.
 */
@Composable
internal fun DefaultSocialButton(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            Icon(
                painter = icon,
                contentDescription = text,
                modifier = Modifier.size(24.dp),
                tint = tint
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text)
        }
    }
}
