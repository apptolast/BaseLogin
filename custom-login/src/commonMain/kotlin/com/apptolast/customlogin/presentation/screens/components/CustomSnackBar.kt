package com.apptolast.customlogin.presentation.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.cd_dismiss
import org.jetbrains.compose.resources.stringResource

@Composable
fun CustomSnackBar(snackBarText: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer),
    ) {
        Snackbar(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            action = {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.cd_dismiss),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Error,
                    contentDescription = null, // error conveyed by text
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = snackBarText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}
