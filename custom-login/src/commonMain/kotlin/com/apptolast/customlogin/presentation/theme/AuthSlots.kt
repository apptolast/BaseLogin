package com.apptolast.customlogin.presentation.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.presentation.screens.components.HeaderContent
import login.custom_login.generated.resources.ATL_isotipo_round
import login.custom_login.generated.resources.Res
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

// ============== SLOT DATA CLASSES ==============

/**
 * Slots for customizing the Login screen.
 */
data class LoginScreenSlots(
    val header: (@Composable (appName: String, appSubtitle: String, logo: DrawableResource?) -> Unit)? = null,
    val emailField: (@Composable (value: String, onValueChange: (String) -> Unit, error: String?, enabled: Boolean) -> Unit)? = null,
    val passwordField: (@Composable (value: String, onValueChange: (String) -> Unit, error: String?, enabled: Boolean) -> Unit)? = null,
    val submitButton: (@Composable (onClick: () -> Unit, isLoading: Boolean, enabled: Boolean, text: String) -> Unit)? = null,
    val socialProviders: (@Composable (onProviderClick: (String) -> Unit) -> Unit)? = null,
    val forgotPasswordLink: (@Composable (onClick: () -> Unit) -> Unit)? = null,
    val registerLink: (@Composable (onRegisterClick: () -> Unit) -> Unit)? = null,
    val footer: (@Composable () -> Unit)? = null
)

/**
 * Slots for customizing the Register screen.
 */
data class RegisterScreenSlots(
    val header: (@Composable () -> Unit)? = null,
    val logo: (@Composable () -> Unit)? = null,
    val nameField: (@Composable (value: String, onValueChange: (String) -> Unit, error: String?, enabled: Boolean) -> Unit)? = null,
    val emailField: (@Composable (value: String, onValueChange: (String) -> Unit, error: String?, enabled: Boolean) -> Unit)? = null,
    val passwordField: (@Composable (value: String, onValueChange: (String) -> Unit, error: String?, enabled: Boolean) -> Unit)? = null,
    val confirmPasswordField: (@Composable (value: String, onValueChange: (String) -> Unit, error: String?, enabled: Boolean) -> Unit)? = null,
    val termsCheckbox: (@Composable (checked: Boolean, onCheckedChange: (Boolean) -> Unit) -> Unit)? = null,
    val submitButton: (@Composable (onClick: () -> Unit, isLoading: Boolean, enabled: Boolean, text: String) -> Unit)? = null,
    val loginLink: (@Composable (onClick: () -> Unit) -> Unit)? = null,
    val footer: (@Composable () -> Unit)? = null
)

/**
 * Slots for customizing the Reset Password screen.
 */
data class ResetPasswordScreenSlots(
    val header: (@Composable () -> Unit)? = null,
    val description: (@Composable () -> Unit)? = null,
    val passwordField: (@Composable (value: String, onValueChange: (String) -> Unit, error: String?, enabled: Boolean) -> Unit)? = null,
    val confirmPasswordField: (@Composable (value: String, onValueChange: (String) -> Unit, error: String?, enabled: Boolean) -> Unit)? = null,
    val submitButton: (@Composable (onClick: () -> Unit, isLoading: Boolean, enabled: Boolean, text: String) -> Unit)? = null,
    val successContent: (@Composable () -> Unit)? = null
)

/**
 * Combined slots for all authentication screens.
 */
data class AuthScreenSlots(
    val login: LoginScreenSlots = LoginScreenSlots(),
    val register: RegisterScreenSlots = RegisterScreenSlots(),
    val resetPassword: ResetPasswordScreenSlots = ResetPasswordScreenSlots(),
)

// ============== DEFAULT SLOT IMPLEMENTATIONS ==============

@Composable
fun DefaultHeader() {
    HeaderContent(
        drawableResource = Res.drawable.ATL_isotipo_round,
        appName = "AppToLast",
        appSubtitle = "App to Last example auth",
    )
}

@Composable
fun DefaultEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    label: String = "Email"
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true, isError = error != null, supportingText = error?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(), enabled = enabled, shape = RoundedCornerShape(12.dp)
    )
}

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
        value = value, onValueChange = onValueChange, label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = label) },
        trailingIcon = {
            IconButton(onClick = { !isVisible }) {
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
        modifier = Modifier.fillMaxWidth(), enabled = enabled, shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun DefaultNameField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    label: String = "Full Name"
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = label) },
        singleLine = true, isError = error != null, supportingText = error?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(), enabled = enabled, shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun DefaultSubmitButton(
    onClick: () -> Unit, isLoading: Boolean, enabled: Boolean, text: String
) {
    Button(
        onClick = onClick, modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = enabled && !isLoading, shape = RoundedCornerShape(12.dp),
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
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun DefaultTextLink(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun DefaultDivider(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HorizontalDivider()
        Text(text, style = MaterialTheme.typography.labelMedium)
        HorizontalDivider()
    }
}

@Composable
fun DefaultRegisterLink(onRegisterClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Don't have an account?", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(4.dp))
        DefaultTextLink(
            text = "Sign Up",
            onClick = onRegisterClick,
        )
    }
}

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
        append("I agree to the ")

        pushLink(LinkAnnotation.Clickable("terms") { onTermsClick() })
        withStyle(style = linkStyle) {
            append("Terms & Conditions")
        }
        pop()

        append(" and ")

        pushLink(LinkAnnotation.Clickable("privacy") { onPrivacyClick() })
        withStyle(style = linkStyle) {
            append("Privacy Policy")
        }
        pop()
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(8.dp))
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}
