package com.apptolast.customlogin.presentation.slots.defaultslots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.presentation.screens.components.HeaderContent
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.atl_isotipo_basic
import login.custom_login.generated.resources.common_app_subtitle
import login.custom_login.generated.resources.common_continue_button
import login.custom_login.generated.resources.forgot_password_screen_description
import login.custom_login.generated.resources.forgot_password_screen_success_description
import login.custom_login.generated.resources.forgot_password_screen_success_title
import login.custom_login.generated.resources.forgot_password_screen_title
import login.custom_login.generated.resources.register_screen_privacy_policy
import login.custom_login.generated.resources.register_screen_terms_and_conditions
import login.custom_login.generated.resources.register_screen_terms_conjunction
import login.custom_login.generated.resources.register_screen_terms_prefix
import login.custom_login.generated.resources.reset_password_screen_description
import login.custom_login.generated.resources.reset_password_screen_title
import org.jetbrains.compose.resources.stringResource

/**
 * Default implementation for a screen header.
 */
@Composable
fun DefaultHeader() {
    HeaderContent(
        drawableResource = Res.drawable.atl_isotipo_basic,
        appName = "AppToLast", // Brand name - not localized
        appSubtitle = stringResource(Res.string.common_app_subtitle),
    )
}

/**
 * Default implementation for a divider with text.
 */
@Composable
fun DefaultDivider(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HorizontalDivider()
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )
        HorizontalDivider()
    }
}

/**
 * Default implementation for a terms and conditions checkbox.
 *
 * @param checked The current checked state.
 * @param onCheckedChange The callback to be invoked when the checkbox is clicked.
 * @param onTermsClick The callback to be invoked when the terms link is clicked.
 * @param onPrivacyClick The callback to be invoked when the privacy link is clicked.
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
        textDecoration = TextDecoration.Underline
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
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}

/**
 * Default implementation for the header of the Forgot Password screen.
 */
@Composable
fun DefaultForgotPasswordHeader() {
    Icon(
        imageVector = Icons.Default.Email,
        contentDescription = null,
        modifier = Modifier.size(40.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(Res.string.forgot_password_screen_title),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Default implementation for the description text of the Forgot Password screen.
 */
@Composable
fun DefaultForgotPasswordDescription() {
    Text(
        text = stringResource(Res.string.forgot_password_screen_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * Default implementation for the header of the Reset Password screen.
 */
@Composable
fun DefaultResetPasswordHeader() {
    Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(Res.string.reset_password_screen_title),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Default implementation for the description text of the Reset Password screen.
 */
@Composable
fun DefaultResetPasswordDescription() {
    Text(
        text = stringResource(Res.string.reset_password_screen_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

/**
 * Default implementation for the success state of a screen.
 *
 * @param title The title text to display.
 * @param description The description text to display.
 * @param onContinue The callback to be invoked when the continue button is clicked, if any.
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
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null, // Decorative icon
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        if (onContinue != null) {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.common_continue_button))
            }
        }
    }
}