package com.apptolast.customlogin.presentation.screens.login

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.presentation.screens.components.CustomSnackBar
import com.apptolast.customlogin.presentation.screens.components.DefaultAuthContainer
import com.apptolast.customlogin.presentation.slots.LoginScreenSlots
import kotlinx.coroutines.flow.collectLatest
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.login_screen_sign_in_button
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * A composable function that represents the main entry point for the Login screen.
 * It connects the ViewModel to the UI content and handles MVI effects.
 *
 * @param viewModel The [LoginViewModel] instance for this screen.
 * @param loginSlots An instance of [LoginScreenSlots] to customize the UI components.
 * @param onNavigateToHome A callback invoked upon successful authentication.
 * @param onNavigateToRegister A callback to navigate to the registration screen.
 * @param onNavigateToResetPassword A callback to navigate to the reset password screen.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    loginSlots: LoginScreenSlots = LoginScreenSlots(),
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToResetPassword: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LoginEffect.NavigateToHome -> onNavigateToHome()
                is LoginEffect.ShowError -> {
                    snackBarHostState.showSnackbar(
                        message = effect.message,
                        withDismissAction = true,
                        duration = SnackbarDuration.Indefinite
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackBarHostState) { snackBarData ->
                CustomSnackBar(
                    snackBarText = snackBarData.visuals.message,
                    onDismiss = { snackBarHostState.currentSnackbarData?.dismiss() }
                )
            }
        }
    ) { paddingValues ->
        LoginContent(
            slots = loginSlots,
            state = uiState,
            modifier = Modifier.padding(paddingValues),
            onAction = viewModel::onAction,
            onNavigateToRegister = onNavigateToRegister,
            onNavigateToForgotPassword = onNavigateToResetPassword,
        )
    }
}

/**
 * A private composable that defines the layout and UI of the Login screen.
 * It is stateless regarding business logic and receives all data and callbacks as parameters.
 *
 * @param slots The [LoginScreenSlots] instance defining the UI components.
 * @param state The current [LoginUiState] of the screen.
 * @param onAction A callback to send actions to the ViewModel.
 * @param onNavigateToRegister A callback to navigate to the registration screen.
 * @param onNavigateToForgotPassword A callback to navigate to the reset password screen.
 */
@Composable
private fun LoginContent(
    slots: LoginScreenSlots,
    state: LoginUiState,
    modifier: Modifier = Modifier,
    onAction: (LoginAction) -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
) {
    DefaultAuthContainer(modifier = modifier) {
        slots.header()

        Spacer(modifier = Modifier.height(16.dp))

        slots.emailField(
            state.email,
            { onAction(LoginAction.EmailChanged(it)) },
            state.emailError,
            !state.isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        slots.passwordField(
            state.password,
            { onAction(LoginAction.PasswordChanged(it)) },
            state.passwordError,
            !state.isLoading
        )

        slots.forgotPasswordLink(onNavigateToForgotPassword)

        Spacer(modifier = Modifier.height(16.dp))

        val isFormValid = state.email.isNotBlank() && state.password.isNotBlank()

        slots.submitButton(
            stringResource(Res.string.login_screen_sign_in_button),
            isFormValid && !state.isLoading,
            state.isLoading,
        ) { onAction(LoginAction.SignInClicked) }

        Spacer(Modifier.height(8.dp))

        slots.socialProviders?.let { socialProviders ->
            socialProviders { provider ->
                onAction(LoginAction.SocialSignInClicked(provider))
            }
        }

        slots.registerLink(onNavigateToRegister)
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginContent(
        slots = LoginScreenSlots(),
        state = LoginUiState(
            email = "test@apptolast.com",
            password = "Password123",
            isLoading = false
        )
    )
}


@Preview
@Composable
private fun LoginScreenLoadingPreview() {
    LoginContent(
        slots = LoginScreenSlots(),
        state = LoginUiState(
            email = "test@apptolast.com",
            password = "Password123",
            isLoading = true
        )
    )
}
