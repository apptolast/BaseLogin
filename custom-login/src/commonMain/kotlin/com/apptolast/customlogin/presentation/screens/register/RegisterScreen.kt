package com.apptolast.customlogin.presentation.screens.register

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import com.apptolast.customlogin.presentation.slots.RegisterScreenSlots
import kotlinx.coroutines.flow.collectLatest
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.register_screen_register_button
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * A composable function that represents the main entry point for the Register screen.
 * It connects the ViewModel to the UI content and handles MVI effects.
 *
 * @param viewModel The [RegisterViewModel] instance for this screen.
 * @param registerSlots An instance of [RegisterScreenSlots] to customize the UI components.
 * @param onNavigateToHome A callback invoked upon successful authentication.
 * @param onNavigateToLogin A callback to navigate back to the login screen.
 */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = koinViewModel(),
    registerSlots: RegisterScreenSlots = RegisterScreenSlots(),
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is RegisterEffect.NavigateToHome -> onNavigateToHome()
                is RegisterEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        withDismissAction = true
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { snackBarData ->
                CustomSnackBar(
                    snackBarText = snackBarData.visuals.message,
                    onDismiss = { snackbarHostState.currentSnackbarData?.dismiss() }
                )
            }
        }
    ) { paddingValues ->
        RegisterContent(
            slots = registerSlots,
            state = uiState,
            modifier = Modifier.padding(paddingValues),
            onAction = viewModel::onAction,
            onNavigateToLogin = onNavigateToLogin
        )
    }
}

/**
 * A private composable that defines the layout and UI of the Register screen.
 * It is stateless and receives all data and callbacks as parameters.
 *
 * @param slots The [RegisterScreenSlots] instance defining the UI components.
 * @param state The current [RegisterUiState] of the screen.
 * @param modifier The modifier to be applied to the root container, including padding from the Scaffold.
 * @param onAction A callback to send actions to the ViewModel.
 * @param onNavigateToLogin A callback to navigate to the login screen.
 */
@Composable
private fun RegisterContent(
    slots: RegisterScreenSlots,
    state: RegisterUiState,
    modifier: Modifier = Modifier,
    onAction: (RegisterAction) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    DefaultAuthContainer(
        modifier = modifier,
        verticalArrangement = slots.layoutVerticalArrangement,
    ) {
        slots.header()

        Spacer(modifier = Modifier.height(16.dp))

        slots.nameField(
            state.fullName,
            { onAction(RegisterAction.FullNameChanged(it)) },
            state.fullNameError,
            !state.isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        slots.emailField(
            state.email,
            { onAction(RegisterAction.EmailChanged(it)) },
            state.emailError,
            !state.isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        slots.passwordField(
            state.password,
            { onAction(RegisterAction.PasswordChanged(it)) },
            state.passwordError,
            !state.isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        slots.confirmPasswordField(
            state.confirmPassword,
            { onAction(RegisterAction.ConfirmPasswordChanged(it)) },
            state.confirmPasswordError,
            !state.isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        slots.termsCheckbox(
            state.termsAccepted
        ) { onAction(RegisterAction.TermsAcceptedChanged(it)) }

        Spacer(modifier = Modifier.height(16.dp))

        val isFormValid = state.fullName.isNotBlank() &&
                state.email.isNotBlank() &&
                state.password.isNotBlank() &&
                state.confirmPassword.isNotBlank() &&
                state.password == state.confirmPassword &&
                state.termsAccepted

        slots.submitButton(
            { onAction(RegisterAction.SignUpClicked) },
            state.isLoading,
            isFormValid && !state.isLoading,
            stringResource(Res.string.register_screen_register_button)
        )

        Spacer(modifier = Modifier.height(16.dp))

        slots.loginLink(onNavigateToLogin)
    }
}
