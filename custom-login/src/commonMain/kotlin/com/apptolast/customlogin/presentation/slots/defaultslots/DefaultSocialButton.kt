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
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.domain.model.IdentityProvider
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.google_icon
import org.jetbrains.compose.resources.painterResource

/**
 * A generic, styled button for social login providers.
 * Takes an optional tint parameter. Defaults to Unspecified to support multi-color icons.
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
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(
            painter = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            tint = tint // Use the tint parameter
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text)
    }
}

/**
 * A specific social login button for Google. Uses a painter resource for the multi-color icon.
 */
@Composable
fun GoogleSocialButton(onClick: () -> Unit) {
    DefaultSocialButton(
        text = "Sign in with Google",
        icon = painterResource(Res.drawable.google_icon),
        onClick = onClick,
        // Tint is defaulted to Unspecified, which is correct for this multi-color icon
    )
}

/**
 * A specific social login button for Phone. Uses a Material Vector Icon.
 */
@Composable
fun PhoneSocialButton(onClick: () -> Unit) {
    DefaultSocialButton(
        text = "Sign in with Phone",
        icon = rememberVectorPainter(image = Icons.Default.Phone),
        onClick = onClick,
        // We provide a tint color, so the vector icon is colored correctly.
        tint = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * A composable that arranges multiple social login buttons.
 * This is the default implementation for the `socialProviders` slot.
 */
@Composable
fun SocialLoginButtonsSection(onProviderClick: (IdentityProvider) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        GoogleSocialButton { onProviderClick(IdentityProvider.Google) }
        Spacer(Modifier.height(8.dp))
        PhoneSocialButton { onProviderClick(IdentityProvider.Phone) }
        // TODO: Add other buttons like GitHub, Apple, etc. here
    }
}
