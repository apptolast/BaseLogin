package com.apptolast.customlogin.presentation.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import com.apptolast.customlogin.domain.model.SocialProvider
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
import com.apptolast.customlogin.presentation.theme.defaultslots.SocialLoginButtonsSection
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
        text: String,
        enabled: Boolean,
        isLoading: Boolean,
        onClick: () -> Unit,
    ) -> Unit = { text, enabled, isLoading, onClick ->
        DefaultSubmitButton(
            text = text,
            enabled = enabled,
            isLoading = isLoading,
            onClick = onClick,
        )
    },
    val socialProviders: (@Composable (onProviderClick: (SocialProvider) -> Unit) -> Unit)? = { onProviderClick ->
        SocialLoginButtonsSection(onProviderClick = onProviderClick)
    },
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
    val layoutVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
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
    val layoutVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
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
    val layoutVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
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
