package com.apptolast.customlogin.presentation.slots.defaultslots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.login_screen_forgot_password_link
import login.custom_login.generated.resources.login_screen_no_account_text
import login.custom_login.generated.resources.login_screen_sign_up_link
import login.custom_login.generated.resources.register_screen_already_have_account_text
import login.custom_login.generated.resources.register_screen_sign_in_link
import org.jetbrains.compose.resources.stringResource

/**
 * Default implementation for a simple text link.
 *
 * @param text The text to be displayed.
 * @param onClick The callback to be invoked when this link is clicked.
 */
@Composable
fun DefaultTextLink(
    text: String,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Default implementation for the "Forgot Password" link.
 *
 * @param onForgotPasswordClick The callback to be invoked when this link is clicked.
 */
@Composable
fun DefaultForgotPasswordLink(onForgotPasswordClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        DefaultTextLink(
            text = stringResource(Res.string.login_screen_forgot_password_link),
            onClick = onForgotPasswordClick,
        )
    }
}

/**
 * Default implementation for the "Sign Up" link section.
 *
 * @param onRegisterClick The callback to be invoked when the link is clicked.
 */
@Composable
fun DefaultRegisterLink(onRegisterClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(Res.string.login_screen_no_account_text),
            style = MaterialTheme.typography.bodyMedium
        )
        DefaultTextLink(
            text = stringResource(Res.string.login_screen_sign_up_link),
            onClick = onRegisterClick,
        )
    }
}

/**
 * Default implementation for the "Sign In" link section.
 *
 * @param onLoginClick The callback to be invoked when the link is clicked.
 */
@Composable
fun DefaultLoginLink(onLoginClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(Res.string.register_screen_already_have_account_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DefaultTextLink(
            text = stringResource(Res.string.register_screen_sign_in_link),
            onClick = onLoginClick
        )
    }
}
