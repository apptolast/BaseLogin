package com.apptolast.customlogin.presentation.screens.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.login_register_link
import login.custom_login.generated.resources.login_screen_no_account_text
import org.jetbrains.compose.resources.stringResource

@Composable
fun RegisterLinkButtonContent(onNavigateToRegister: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(Res.string.login_screen_no_account_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onNavigateToRegister) {
            Text(stringResource(Res.string.login_register_link))
        }
    }
}