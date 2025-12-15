package com.apptolast.customlogin.presentation.theme

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import com.apptolast.customlogin.presentation.theme.defaultslots.DefaultAuthContainer
import com.apptolast.customlogin.presentation.theme.defaultslots.DefaultForgotPasswordDescription
import com.apptolast.customlogin.presentation.theme.defaultslots.DefaultForgotPasswordHeader
import com.apptolast.customlogin.presentation.theme.defaultslots.DefaultForgotPasswordLink
import com.apptolast.customlogin.presentation.theme.defaultslots.DefaultHeader
import com.apptolast.customlogin.presentation.theme.defaultslots.DefaultLoginLink
import com.apptolast.customlogin.presentation.theme.defaultslots.DefaultRegisterLink
import com.apptolast.customlogin.presentation.theme.defaultslots.DefaultResetPasswordDescription
import com.apptolast.customlogin.presentation.theme.defaultslots.DefaultResetPasswordHeader
import com.apptolast.customlogin.presentation.theme.defaultslots.DefaultSubmitButton
import com.apptolast.customlogin.presentation.theme.defaultslots.DefaultSuccessContent
import com.apptolast.customlogin.presentation.theme.defaultslots.DefaultTermsCheckbox
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.register_screen_confirm_password_label
import login.custom_login.generated.resources.reset_password_screen_confirm_new_password_label
import login.custom_login.generated.resources.reset_password_screen_new_password_label
import org.jetbrains.compose.resources.stringResource

/**
 * Defines the customizable composable slots for the Login screen.
 * Each property represents a specific UI component that can be replaced.
 * Default implementations are provided for convenience.
 */
data class LoginScreenSlots(
    val formContainer: @Composable (@Composable ColumnScope.() -> Unit) -> Unit = { content -> DefaultAuthContainer { content() } },
    val header: @Composable () -> Unit = { DefaultHeader() },
    val emailField: @Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit = { value, onValueChange, error, enabled ->
        DefaultEmailField(
            value = value,
            onValueChange = onValueChange,
            error = error,
            enabled = enabled
        )
    },
    val passwordField: @Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit = { value, onValueChange, error, enabled ->
        DefaultPasswordField(
            value = value,
            onValueChange = onValueChange,
            error = error,
            enabled = enabled
        )
    },
    val submitButton: @Composable (
        onClick: () -> Unit,
        isLoading: Boolean,
        enabled: Boolean,
        text: String
    ) -> Unit = { onClick, isLoading, enabled, text ->
        DefaultSubmitButton(
            onClick = onClick,
            isLoading = isLoading,
            enabled = enabled,
            text = text
        )
    },
    val socialProviders: (@Composable (onProviderClick: (String) -> Unit) -> Unit)? = null,
    val forgotPasswordLink: @Composable (onClick: () -> Unit) -> Unit = { onClick ->
        DefaultForgotPasswordLink(onForgotPasswordClick = onClick)
    },
    val registerLink: @Composable (onRegisterClick: () -> Unit) -> Unit = { onRegisterClick ->
        DefaultRegisterLink(onRegisterClick = onRegisterClick)
    },
    val footer: (@Composable () -> Unit)? = null
)

/**
 * Defines the customizable composable slots for the Register screen.
 * Default implementations are provided for convenience.
 */
data class RegisterScreenSlots(
    val formContainer: @Composable (@Composable ColumnScope.() -> Unit) -> Unit = { content -> DefaultAuthContainer { content() } },
    val header: @Composable () -> Unit = { DefaultHeader() },
    val nameField: @Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit = { value, onValueChange, error, enabled ->
        DefaultNameField(
            value = value,
            onValueChange = onValueChange,
            error = error,
            enabled = enabled
        )
    },
    val emailField: @Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit = { value, onValueChange, error, enabled ->
        DefaultEmailField(
            value = value,
            onValueChange = onValueChange,
            error = error,
            enabled = enabled
        )
    },
    val passwordField: @Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit = { value, onValueChange, error, enabled ->
        DefaultPasswordField(
            value = value,
            onValueChange = onValueChange,
            error = error,
            enabled = enabled
        )
    },
    val confirmPasswordField: @Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit = { value, onValueChange, error, enabled ->
        DefaultPasswordField(
            value = value,
            onValueChange = onValueChange,
            error = error,
            enabled = enabled,
            label = stringResource(Res.string.register_screen_confirm_password_label)
        )
    },
    val termsCheckbox: @Composable (checked: Boolean, onCheckedChange: (Boolean) -> Unit) -> Unit = { checked, onCheckedChange ->
        DefaultTermsCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    },
    val submitButton: @Composable (
        onClick: () -> Unit,
        isLoading: Boolean,
        enabled: Boolean,
        text: String
    ) -> Unit = { onClick, isLoading, enabled, text ->
        DefaultSubmitButton(
            onClick = onClick,
            isLoading = isLoading,
            enabled = enabled,
            text = text
        )
    },
    val loginLink: @Composable (onClick: () -> Unit) -> Unit = { onClick -> DefaultLoginLink(onClick) },
    val logo: (@Composable () -> Unit)? = null,
    val footer: (@Composable () -> Unit)? = null
)

/**
 * Defines the customizable composable slots for the Forgot Password screen.
 * Default implementations are provided for convenience.
 */
data class ForgotPasswordScreenSlots(
    val formContainer: @Composable (@Composable ColumnScope.() -> Unit) -> Unit = { content -> DefaultAuthContainer { content() } },
    val header: @Composable () -> Unit = { DefaultForgotPasswordHeader() },
    val description: @Composable () -> Unit = { DefaultForgotPasswordDescription() },
    val emailField: @Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit = { value, onValueChange, error, enabled ->
        DefaultEmailField(
            value = value,
            onValueChange = onValueChange,
            error = error,
            enabled = enabled
        )
    },
    val submitButton: @Composable (
        onClick: () -> Unit,
        isLoading: Boolean,
        enabled: Boolean,
        text: String
    ) -> Unit = { onClick, isLoading, enabled, text ->
        DefaultSubmitButton(
            onClick = onClick,
            isLoading = isLoading,
            enabled = enabled,
            text = text
        )
    },
    val successContent: @Composable () -> Unit = { DefaultSuccessContent() }
)

/**
 * Defines the customizable composable slots for the Reset Password screen.
 * Default implementations are provided for convenience.
 */
data class ResetPasswordScreenSlots(
    val formContainer: @Composable (@Composable ColumnScope.() -> Unit) -> Unit = { content -> DefaultAuthContainer { content() } },
    val header: @Composable () -> Unit = { DefaultResetPasswordHeader() },
    val description: @Composable () -> Unit = { DefaultResetPasswordDescription() },
    val passwordField: @Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit = { value, onValueChange, error, enabled ->
        DefaultPasswordField(
            value = value,
            onValueChange = onValueChange,
            error = error,
            enabled = enabled,
            label = stringResource(Res.string.reset_password_screen_new_password_label)
        )
    },
    val confirmPasswordField: @Composable (
        value: String,
        onValueChange: (String) -> Unit,
        error: String?,
        enabled: Boolean
    ) -> Unit = { value, onValueChange, error, enabled ->
        DefaultPasswordField(
            value = value,
            onValueChange = onValueChange,
            error = error,
            enabled = enabled,
            label = stringResource(Res.string.reset_password_screen_confirm_new_password_label)
        )
    },
    val submitButton: @Composable (
        onClick: () -> Unit,
        isLoading: Boolean,
        enabled: Boolean,
        text: String
    ) -> Unit = { onClick, isLoading, enabled, text ->
        DefaultSubmitButton(
            onClick = onClick,
            isLoading = isLoading,
            enabled = enabled,
            text = text
        )
    },
    val successContent: @Composable (onContinue: () -> Unit) -> Unit = { onContinue ->
        DefaultSuccessContent(
            onContinue = onContinue
        )
    }
)

/**
 * A container for all authentication-related screen slots.
 * This allows passing all custom UI components in a single object.
 */
data class AuthScreenSlots(
    val login: LoginScreenSlots = LoginScreenSlots(),
    val register: RegisterScreenSlots = RegisterScreenSlots(),
    val forgotPassword: ForgotPasswordScreenSlots = ForgotPasswordScreenSlots(),
    val resetPassword: ResetPasswordScreenSlots = ResetPasswordScreenSlots(),
)
