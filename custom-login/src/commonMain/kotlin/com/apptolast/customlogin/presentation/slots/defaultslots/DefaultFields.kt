package com.apptolast.customlogin.presentation.slots.defaultslots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.util.COMMON_COUNTRIES
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.cd_hide_password
import login.custom_login.generated.resources.cd_show_password
import login.custom_login.generated.resources.common_email_label
import login.custom_login.generated.resources.common_full_name_label
import login.custom_login.generated.resources.common_password_label
import login.custom_login.generated.resources.phone_auth_screen_otp_label
import login.custom_login.generated.resources.phone_auth_screen_phone_label
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

/**
 * Default implementation for a phone number input field.
 *
 * @param value The current text value of the field.
 * @param onValueChange The callback that is triggered when the input service updates the text.
 * @param error An optional error message to display below the field.
 * @param enabled Controls the enabled state of the field.
 * @param label The text to be displayed as a label for the field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    countryCode: String = "+1",
    onCountryCodeChange: (String) -> Unit = {},
    label: String = stringResource(Res.string.phone_auth_screen_phone_label)
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded },
            modifier = Modifier.width(110.dp),
        ) {
            OutlinedTextField(
                value = COMMON_COUNTRIES.find { it.dialCode == countryCode }?.displayLabel ?: countryCode,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .width(110.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = enabled,
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                COMMON_COUNTRIES.forEach { country ->
                    DropdownMenuItem(
                        text = { Text("${country.flag} ${country.dialCode}  ${country.name}") },
                        onClick = {
                            onCountryCodeChange(country.dialCode)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = label) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            modifier = Modifier.weight(1f),
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
        )
    }
}

/**
 * Default implementation for an OTP (One-Time Password) input field.
 *
 * @param value The current text value of the field.
 * @param onValueChange The callback that is triggered when the input service updates the text.
 * @param error An optional error message to display below the field.
 * @param enabled Controls the enabled state of the field.
 * @param label The text to be displayed as a label for the field.
 */
@Composable
fun DefaultOtpField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    label: String = stringResource(Res.string.phone_auth_screen_otp_label)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp)
    )
}
