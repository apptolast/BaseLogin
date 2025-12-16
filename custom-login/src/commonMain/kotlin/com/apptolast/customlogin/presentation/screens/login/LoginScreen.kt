package com.apptolast.customlogin.presentation.screens.login

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
import com.apptolast.customlogin.presentation.theme.LoginScreenSlots
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.login_screen_sign_in_button
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * A composable function that represents the main entry point for the Login screen.
 * It connects the ViewModel to the UI content and handles authentication success navigation.
 *
 * @param viewModel The [LoginViewModel] instance for this screen.
 * @param loginSlots An instance of [LoginScreenSlots] to customize the UI components.
 * @param onAuthSuccess A callback invoked upon successful authentication, providing the [UserSession].
 * @param onNavigateToRegister A callback to navigate to the registration screen.
 * @param onNavigateToResetPassword A callback to navigate to the reset password screen.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    loginSlots: LoginScreenSlots = LoginScreenSlots(),
    onAuthSuccess: (UserSession) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToResetPassword: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    with(uiState) {
        LaunchedEffect(user) {
            user?.let { onAuthSuccess(it) }
        }

        LoginContent(
            slots = loginSlots,
            email = email,
            password = password,
            isLoading = isLoading,
            emailError = emailError,
            passwordError = passwordError,
            errorMessage = errorMessage,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onLoginClick = viewModel::signInWithEmail,
            onNavigateToRegister = onNavigateToRegister,
            onNavigateToForgotPassword = onNavigateToResetPassword
        )
    }
}

/**
 * A private composable that defines the layout and UI of the Login screen.
 * It is stateless regarding business logic and receives all data and callbacks as parameters.
 *
 * @param slots The [LoginScreenSlots] instance defining the UI components.
 * @param email The current email value.
 * @param password The current password value.
 * @param emailError An optional error for the email field.
 * @param passwordError An optional error for the password field.
 * @param errorMessage A general error message to display.
 * @param isLoading A boolean indicating if a loading operation is in progress.
 * @param onEmailChange A callback for email input changes.
 * @param onPasswordChange A callback for password input changes.
 * @param onLoginClick A callback invoked when the login button is clicked.
 * @param onNavigateToRegister A callback to navigate to the registration screen.
 * @param onNavigateToForgotPassword A callback to navigate to the reset password screen.
 */
@Composable
private fun LoginContent(
    slots: LoginScreenSlots = LoginScreenSlots(),
    email: String,
    password: String,
    isLoading: Boolean,
    emailError: String? = null,
    passwordError: String? = null,
    errorMessage: String? = null,
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onLoginClick: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
) {

    DefaultAuthContainer(
        verticalArrangement = slots.layoutVerticalArrangement,
    ) {

        slots.header()

        Spacer(modifier = Modifier.height(16.dp))

        slots.emailField(
            email,
            onEmailChange,
            emailError ?: errorMessage,
            !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        slots.passwordField(
            password,
            onPasswordChange,
            passwordError,
            !isLoading
        )

        slots.forgotPasswordLink(onNavigateToForgotPassword)

        Spacer(modifier = Modifier.height(16.dp))

        val isFormValid = email.isNotBlank() && password.isNotBlank()

        slots.submitButton(
            stringResource(Res.string.login_screen_sign_in_button),
            isFormValid && !isLoading,
            isLoading,
            onLoginClick,
        )

        slots.socialProviders?.invoke { /* onProviderClick */ }

        slots.registerLink(onNavigateToRegister)
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginContent(
        email = "test@apptolast.com",
        password = "Password123",
        isLoading = false,
    )
}


@Preview
@Composable
private fun LoginScreenLoadingPreview() {
    LoginContent(
        email = "test@apptolast.com",
        password = "Password123",
        isLoading = true,
    )
}
