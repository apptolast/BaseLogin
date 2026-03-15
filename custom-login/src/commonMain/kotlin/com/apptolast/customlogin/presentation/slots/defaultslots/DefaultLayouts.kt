package com.apptolast.customlogin.presentation.slots.defaultslots

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.presentation.screens.components.HeaderContent
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.atl_isotipo_basic
import login.custom_login.generated.resources.common_continue_button
import login.custom_login.generated.resources.forgot_password_screen_description
import login.custom_login.generated.resources.forgot_password_screen_success_description
import login.custom_login.generated.resources.forgot_password_screen_success_title
import login.custom_login.generated.resources.forgot_password_screen_title
import login.custom_login.generated.resources.magic_link_screen_description
import login.custom_login.generated.resources.magic_link_screen_success_description
import login.custom_login.generated.resources.magic_link_screen_success_title
import login.custom_login.generated.resources.magic_link_screen_title
import login.custom_login.generated.resources.phone_auth_screen_otp_description
import login.custom_login.generated.resources.phone_auth_screen_otp_header
import login.custom_login.generated.resources.phone_auth_screen_phone_description
import login.custom_login.generated.resources.phone_auth_screen_phone_header
import login.custom_login.generated.resources.register_screen_privacy_policy
import login.custom_login.generated.resources.register_screen_terms_and_conditions
import login.custom_login.generated.resources.register_screen_terms_conjunction
import login.custom_login.generated.resources.register_screen_terms_prefix
import login.custom_login.generated.resources.reset_password_screen_description
import login.custom_login.generated.resources.reset_password_screen_title
import org.jetbrains.compose.resources.stringResource

// ── Shared helpers ─────────────────────────────────────────────────────────────

/**
 * A circle Surface container holding an icon. Used for screen headers to give
 * a polished, consistent visual anchor across all auth screens.
 */
@Composable
private fun IconInCircle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String? = null,
    size: Int = 64,
    iconSize: Int = 28,
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(size.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// ── Header ─────────────────────────────────────────────────────────────────────

/**
 * Default implementation for the login/register screen header (logo + brand name).
 */
@Composable
fun DefaultHeader() {
    HeaderContent(
        drawableResource = Res.drawable.atl_isotipo_basic,
        appName = "AppToLast",
        appSubtitle = "App to Last example auth",
    )
}

// ── Divider ────────────────────────────────────────────────────────────────────

/**
 * An elegant horizontal divider with centred label text (e.g. "OR").
 */
@Composable
fun DefaultDivider(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

// ── Terms checkbox ─────────────────────────────────────────────────────────────

/**
 * Default implementation for the terms and conditions checkbox on the Register screen.
 */
@Composable
fun DefaultTermsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {}
) {
    val linkStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
        fontWeight = FontWeight.Medium
    )

    val annotatedString = buildAnnotatedString {
        append(stringResource(Res.string.register_screen_terms_prefix))
        append(" ")

        pushLink(LinkAnnotation.Clickable("terms") { onTermsClick() })
        withStyle(style = linkStyle) {
            append(stringResource(Res.string.register_screen_terms_and_conditions))
        }
        pop()

        append(" ")
        append(stringResource(Res.string.register_screen_terms_conjunction))
        append(" ")

        pushLink(LinkAnnotation.Clickable("privacy") { onPrivacyClick() })
        withStyle(style = linkStyle) {
            append(stringResource(Res.string.register_screen_privacy_policy))
        }
        pop()
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Forgot Password ────────────────────────────────────────────────────────────

@Composable
fun DefaultForgotPasswordHeader() {
    IconInCircle(icon = Icons.Default.Email, size = 64, iconSize = 28)
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = stringResource(Res.string.forgot_password_screen_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )
}

@Composable
fun DefaultForgotPasswordDescription() {
    Text(
        text = stringResource(Res.string.forgot_password_screen_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

// ── Reset Password ─────────────────────────────────────────────────────────────

@Composable
fun DefaultResetPasswordHeader() {
    IconInCircle(icon = Icons.Default.Lock, size = 64, iconSize = 28)
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = stringResource(Res.string.reset_password_screen_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )
}

@Composable
fun DefaultResetPasswordDescription() {
    Text(
        text = stringResource(Res.string.reset_password_screen_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

// ── Magic Link ─────────────────────────────────────────────────────────────────

@Composable
fun DefaultMagicLinkHeader() {
    IconInCircle(icon = Icons.Default.Email, size = 64, iconSize = 28)
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = stringResource(Res.string.magic_link_screen_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )
}

@Composable
fun DefaultMagicLinkDescription() {
    Text(
        text = stringResource(Res.string.magic_link_screen_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
fun DefaultMagicLinkSuccessContent(email: String) {
    DefaultSuccessContent(
        title = stringResource(Res.string.magic_link_screen_success_title),
        description = stringResource(Res.string.magic_link_screen_success_description, email)
    )
}

// ── Phone Auth ─────────────────────────────────────────────────────────────────

@Composable
fun DefaultPhoneAuthHeader() {
    IconInCircle(icon = Icons.Default.Phone, size = 64, iconSize = 28)
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = stringResource(Res.string.phone_auth_screen_phone_header),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )
}

@Composable
fun DefaultPhoneAuthDescription() {
    Text(
        text = stringResource(Res.string.phone_auth_screen_phone_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
fun DefaultOtpHeader() {
    IconInCircle(icon = Icons.Default.Phone, size = 64, iconSize = 28)
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = stringResource(Res.string.phone_auth_screen_otp_header),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )
}

@Composable
fun DefaultOtpDescription(phoneNumber: String) {
    Text(
        text = stringResource(Res.string.phone_auth_screen_otp_description, phoneNumber),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

// ── Success content ────────────────────────────────────────────────────────────

/**
 * Generic success state for any auth screen.
 * The check icon is presented inside a tinted circle Surface for visual polish.
 *
 * @param title       Title text.
 * @param description Body text below the title.
 * @param onContinue  Optional callback; when non-null a "Continue" button is shown.
 */
@Composable
fun DefaultSuccessContent(
    title: String = stringResource(Res.string.forgot_password_screen_success_title),
    description: String = stringResource(Res.string.forgot_password_screen_success_description),
    onContinue: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon in a circle container
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null, // decorative; title conveys the message
                    modifier = Modifier.size(52.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (onContinue != null) {
            Spacer(modifier = Modifier.height(36.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.common_continue_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
