package com.apptolast.baselogin.presentation.screens.register

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.apptolast.baselogin.presentation.screens.components.CustomSnackBar
import com.apptolast.baselogin.presentation.screens.components.DefaultAuthContainer
import com.apptolast.baselogin.presentation.slots.RegisterScreenSlots
import com.apptolast.baselogin.presentation.slots.defaultslots.DefaultDivider
import com.apptolast.baselogin.presentation.util.toStringRes
import com.apptolast.baselogin.util.toStringRes
import kotlinx.coroutines.flow.collectLatest
import login.baselogin.generated.resources.Res
import login.baselogin.generated.resources.divider_or
import login.baselogin.generated.resources.register_screen_register_button
import org.jetbrains.compose.resources.getString
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
    onNavigateToLogin: () -> Unit,
    onNavigateToPhoneAuth: () -> Unit = {},
    onNavigateToMagicLink: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is RegisterEffect.NavigateToHome -> onNavigateToHome()
                is RegisterEffect.NavigateToPhoneAuth -> onNavigateToPhoneAuth()
                is RegisterEffect.NavigateToMagicLink -> onNavigateToMagicLink()
                is RegisterEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = getString(effect.error.toStringRes()),
                        withDismissAction = true,
                    )
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { snackBarData ->
                CustomSnackBar(
                    snackBarText = snackBarData.visuals.message,
                    onDismiss = { snackbarHostState.currentSnackbarData?.dismiss() },
                )
            }
        },
    ) { paddingValues ->
        RegisterContent(
            slots = registerSlots,
            state = uiState,
            modifier = Modifier.padding(paddingValues).consumeWindowInsets(paddingValues),
            onAction = viewModel::onAction,
            onNavigateToLogin = onNavigateToLogin,
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
    onNavigateToLogin: () -> Unit,
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
            state.fullNameError?.let { stringResource(it.toStringRes()) },
            !state.isLoading,
        )

        Spacer(modifier = Modifier.height(8.dp))

        slots.emailField(
            state.email,
            { onAction(RegisterAction.EmailChanged(it)) },
            state.emailError?.let { stringResource(it.toStringRes()) },
            !state.isLoading,
        )

        Spacer(modifier = Modifier.height(8.dp))

        slots.passwordField(
            state.password,
            { onAction(RegisterAction.PasswordChanged(it)) },
            state.passwordError?.let { stringResource(it.toStringRes()) },
            !state.isLoading,
        )

        Spacer(modifier = Modifier.height(8.dp))

        slots.confirmPasswordField(
            state.confirmPassword,
            { onAction(RegisterAction.ConfirmPasswordChanged(it)) },
            state.confirmPasswordError?.let { stringResource(it.toStringRes()) },
            !state.isLoading,
        )

        Spacer(modifier = Modifier.height(8.dp))

        slots.termsCheckbox(
            state.termsAccepted,
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
            stringResource(Res.string.register_screen_register_button),
        )

        if (slots.socialProviders != null && state.availableProviders.isNotEmpty()) {
            DefaultDivider(stringResource(Res.string.divider_or))
            slots.socialProviders.invoke(state.availableProviders, state.loadingProvider) { provider ->
                onAction(RegisterAction.SignUpWithOAuth(provider))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        slots.loginLink(onNavigateToLogin)
    }
}
