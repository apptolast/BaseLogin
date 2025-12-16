package com.apptolast.customlogin.presentation.screens.register

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.domain.model.UserSession
import com.apptolast.customlogin.presentation.screens.components.DefaultAuthContainer
import com.apptolast.customlogin.presentation.theme.RegisterScreenSlots
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.register_screen_register_button
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * A composable function that represents the main entry point for the Register screen.
 * It connects the ViewModel to the UI content and handles authentication success navigation.
 *
 * @param viewModel The [RegisterViewModel] instance for this screen.
 * @param registerSlots An instance of [RegisterScreenSlots] to customize the UI components.
 * @param onAuthSuccess A callback invoked upon successful authentication, providing the [UserSession].
 * @param onNavigateToLogin A callback to navigate back to the login screen.
 */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = koinViewModel(),
    registerSlots: RegisterScreenSlots = RegisterScreenSlots(),
    onAuthSuccess: (UserSession) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    with(uiState) {
        LaunchedEffect(user) {
            user?.let { onAuthSuccess(it) }
        }

        RegisterContent(
            slots = registerSlots,
            fullName = fullName,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            termsAccepted = termsAccepted,
            fullNameError = fullNameError,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            errorMessage = errorMessage,
            isLoading = isLoading,
            onFullNameChange = viewModel::onFullNameChange,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onTermsAcceptedChange = viewModel::onTermsAcceptedChange,
            onRegisterClick = viewModel::createUserWithEmail,
            onNavigateToLogin = onNavigateToLogin
        )
    }
}

/**
 * A private composable that defines the layout and UI of the Register screen.
 * It is stateless regarding business logic and receives all data and callbacks as parameters.
 *
 * @param slots The [RegisterScreenSlots] instance defining the UI components.
 * @param fullName The current full name value.
 * @param email The current email value.
 * @param password The current password value.
 * @param confirmPassword The current confirm password value.
 * @param termsAccepted The current terms accepted value.
 * @param fullNameError An optional error for the full name field.
 * @param emailError An optional error for the email field.
 * @param passwordError An optional error for the password field.
 * @param confirmPasswordError An optional error for the confirm password field.
 * @param errorMessage A general error message to display.
 * @param isLoading A boolean indicating if a loading operation is in progress.
 * @param onFullNameChange A callback for full name input changes.
 * @param onEmailChange A callback for email input changes.
 * @param onPasswordChange A callback for password input changes.
 * @param onConfirmPasswordChange A callback for confirm password input changes.
 * @param onTermsAcceptedChange A callback for terms accepted input changes.
 * @param onRegisterClick A callback invoked when the register button is clicked.
 * @param onNavigateToLogin A callback to navigate to the login screen.
 */
@Composable
private fun RegisterContent(
    slots: RegisterScreenSlots,
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String,
    termsAccepted: Boolean,
    fullNameError: String?,
    emailError: String?,
    passwordError: String?,
    confirmPasswordError: String?,
    errorMessage: String?,
    isLoading: Boolean,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {

    DefaultAuthContainer(
        verticalArrangement = slots.layoutVerticalArrangement,
    ) {
        slots.header()

        Spacer(modifier = Modifier.height(16.dp))

        slots.nameField(
            fullName,
            onFullNameChange,
            fullNameError ?: errorMessage,
            !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        slots.emailField(
            email,
            onEmailChange,
            emailError,
            !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        slots.passwordField(
            password,
            onPasswordChange,
            passwordError,
            !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        slots.confirmPasswordField(
            confirmPassword,
            onConfirmPasswordChange,
            confirmPasswordError,
            !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        slots.termsCheckbox(
            termsAccepted,
            onTermsAcceptedChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        val isFormValid = fullName.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                password == confirmPassword &&
                termsAccepted

        slots.submitButton(
            onRegisterClick,
            isLoading,
            isFormValid && !isLoading,
            stringResource(Res.string.register_screen_register_button)
        )

        Spacer(modifier = Modifier.height(16.dp))

        slots.loginLink(onNavigateToLogin)
    }
}
