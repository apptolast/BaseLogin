package com.apptolast.customlogin.presentation.theme

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.cd_hide_password
import login.custom_login.generated.resources.cd_show_password
import login.custom_login.generated.resources.common_email_label
import login.custom_login.generated.resources.common_full_name_label
import login.custom_login.generated.resources.common_password_label
import org.jetbrains.compose.resources.stringResource

/**
 * Default implementation for an email input field.
 *
 * @param value The current text value of the field.
 * @param onValueChange The callback that is triggered when the input service updates the text.
 * @param error An optional error message to display below the field.
 * @param enabled Controls the enabled state of the field.
 * @param label The text to be displayed as a label for the field.
 */
@Composable
fun DefaultEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    label: String = stringResource(Res.string.common_email_label)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Default implementation for a password input field.
 *
 * @param value The current text value of the field.
 * @param onValueChange The callback that is triggered when the input service updates the text.
 * @param error An optional error message to display below the field.
 * @param enabled Controls the enabled state of the field.
 * @param label The text to be displayed as a label for the field.
 */
@Composable
fun DefaultPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    label: String = stringResource(Res.string.common_password_label)
) {
    var isVisible by remember { mutableStateOf(false) }
    val showPasswordRes = stringResource(Res.string.cd_show_password)
    val hidePasswordRes = stringResource(Res.string.cd_hide_password)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = label) },
        trailingIcon = {
            IconButton(onClick = { isVisible = !isVisible }) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (isVisible) hidePasswordRes else showPasswordRes
                )
            }
        },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Default implementation for a user name input field.
 *
 * @param value The current text value of the field.
 * @param onValueChange The callback that is triggered when the input service updates the text.
 * @param error An optional error message to display below the field.
 * @param enabled Controls the enabled state of the field.
 * @param label The text to be displayed as a label for the field.
 */
@Composable
fun DefaultNameField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    label: String = stringResource(Res.string.common_full_name_label)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = label) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp)
    )
}
