package com.apptolast.customlogin.presentation.theme

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

/**
 * Slots for customizing the Login screen.
 */
data class LoginScreenSlots(
    /**
     * Custom header content (logo, app name, etc.)
     */
    val header: (@Composable () -> Unit)? = null,

    /**
     * Custom logo composable.
     */
    val logo: (@Composable () -> Unit)? = null,

    /**
     * Custom email input field.
     */
    val emailField: (@Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit)? = null,

    /**
     * Custom password input field.
     */
    val passwordField: (@Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit)? = null,

    /**
     * Custom submit button.
     */
    val submitButton: (@Composable (
        onClick: () -> Unit,
        isLoading: Boolean,
        enabled: Boolean,
        text: String
    ) -> Unit)? = null,

    /**
     * Social login providers section.
     */
    val socialProviders: (@Composable (onProviderClick: (String) -> Unit) -> Unit)? = null,

    /**
     * Forgot password link.
     */
    val forgotPasswordLink: (@Composable (onClick: () -> Unit) -> Unit)? = null,

    /**
     * Register link section.
     */
    val registerLink: (@Composable (
        onRegisterClick: () -> Unit
    ) -> Unit)? = null,

    /**
     * Custom footer content.
     */
    val footer: (@Composable () -> Unit)? = null
)

/**
 * Slots for customizing the Register screen.
 */
data class RegisterScreenSlots(
    val header: (@Composable () -> Unit)? = null,
    val logo: (@Composable () -> Unit)? = null,
    val nameField: (@Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit)? = null,
    val emailField: (@Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit)? = null,
    val passwordField: (@Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit)? = null,
    val confirmPasswordField: (@Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit)? = null,
    val termsCheckbox: (@Composable (
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) -> Unit)? = null,
    val submitButton: (@Composable (
        onClick: () -> Unit,
        isLoading: Boolean,
        enabled: Boolean,
        text: String
    ) -> Unit)? = null,
    val loginLink: (@Composable (onClick: () -> Unit) -> Unit)? = null,
    val footer: (@Composable () -> Unit)? = null
)

/**
 * Slots for customizing the Forgot Password screen.
 */
data class ForgotPasswordScreenSlots(
    val header: (@Composable () -> Unit)? = null,
    val description: (@Composable () -> Unit)? = null,
    val emailField: (@Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit)? = null,
    val submitButton: (@Composable (
        onClick: () -> Unit,
        isLoading: Boolean,
        enabled: Boolean,
        text: String
    ) -> Unit)? = null,
    val backToLoginLink: (@Composable (onClick: () -> Unit) -> Unit)? = null,
    val successContent: (@Composable (email: String) -> Unit)? = null
)

/**
 * Slots for customizing the Reset Password screen.
 */
data class ResetPasswordScreenSlots(
    val header: (@Composable () -> Unit)? = null,
    val description: (@Composable () -> Unit)? = null,
    val passwordField: (@Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit)? = null,
    val confirmPasswordField: (@Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit)? = null,
    val submitButton: (@Composable (
        onClick: () -> Unit,
        isLoading: Boolean,
        enabled: Boolean,
        text: String
    ) -> Unit)? = null,
    val successContent: (@Composable () -> Unit)? = null
)

/**
 * Combined slots for all authentication screens.
 */
data class AuthScreenSlots(
    val login: LoginScreenSlots = LoginScreenSlots(),
    val register: RegisterScreenSlots = RegisterScreenSlots(),
    val forgotPassword: ForgotPasswordScreenSlots = ForgotPasswordScreenSlots(),
    val resetPassword: ResetPasswordScreenSlots = ResetPasswordScreenSlots()
)

// ============== DEFAULT SLOT IMPLEMENTATIONS ==============

/**
 * Default email input field.
 */
@Composable
fun DefaultEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    label: String = "Email"
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
 * Default password input field.
 */
@Composable
fun DefaultPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    label: String = "Password"
) {
    var isVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = label) },
        trailingIcon = {
            IconButton(onClick = { isVisible = !isVisible }) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (isVisible) "Hide password" else "Show password"
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
 * Default name input field.
 */
@Composable
fun DefaultNameField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    label: String = "Full Name"
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
 * Default submit button.
 */
@Composable
fun DefaultSubmitButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean,
    text: String
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Default text link button.
 */
@Composable
fun DefaultTextLink(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
