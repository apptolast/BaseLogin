package com.apptolast.customlogin.presentation.screens.resetpassword

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.presentation.screens.components.CustomSnackBar
import com.apptolast.customlogin.presentation.screens.components.DefaultAuthContainer
import com.apptolast.customlogin.presentation.slots.ResetPasswordScreenSlots
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.cd_navigate_back
import login.custom_login.generated.resources.reset_password_screen_reset_button
import login.custom_login.generated.resources.reset_password_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * A composable function that represents the main entry point for the Reset Password screen.
 * It handles the view model logic, side effects, and provides the UI content.
 *
 * @param resetCode The password reset code, typically from a deep link.
 * @param viewModel The [ResetPasswordViewModel] for this screen.
 * @param resetPasswordSlots An instance of [ResetPasswordScreenSlots] to customize the UI.
 * @param onNavigateBack A callback to navigate to the previous screen.
 * @param onNavigateToLogin A callback to navigate to the login screen after a successful reset.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    resetCode: String = "",
    viewModel: ResetPasswordViewModel = koinViewModel(),
    resetPasswordSlots: ResetPasswordScreenSlots = ResetPasswordScreenSlots(),
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(resetCode) {
        if (resetCode.isNotBlank()) {
            viewModel.setResetCode(resetCode)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ResetPasswordEffect.NavigateToLogin -> {
                    isSuccess = true
                    delay(2000) // Keep success message on screen for a moment
                    onNavigateToLogin()
                }
                is ResetPasswordEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        withDismissAction = true
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.reset_password_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.cd_navigate_back)
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { snackBarData ->
                CustomSnackBar(
                    snackBarText = snackBarData.visuals.message,
                    onDismiss = { snackbarHostState.currentSnackbarData?.dismiss() }
                )
            }
        },
        modifier = Modifier.consumeWindowInsets(TopAppBarDefaults.windowInsets)
    ) { paddingValues ->
        ResetPasswordContent(
            slots = resetPasswordSlots,
            state = uiState,
            isSuccess = isSuccess,
            onAction = viewModel::onAction,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * A private composable that defines the layout and UI for the Reset Password screen.
 * It is stateless regarding business logic and field states, receiving all data and callbacks.
 *
 * @param slots The [ResetPasswordScreenSlots] defining the UI components.
 * @param state The current [ResetPasswordUiState] of the screen.
 * @param isSuccess A flag indicating if the password reset is complete.
 * @param onAction A callback to send actions to the ViewModel.
 * @param onNavigateBack A callback to navigate to the previous screen.
 */
@Composable
private fun ResetPasswordContent(
    slots: ResetPasswordScreenSlots,
    state: ResetPasswordUiState,
    isSuccess: Boolean,
    onAction: (ResetPasswordAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = isSuccess,
        modifier = modifier
    ) { passwordReset ->
        if (passwordReset) {
            slots.successContent(onNavigateBack)
        } else {
            DefaultAuthContainer(
                verticalArrangement = slots.layoutVerticalArrangement,
            ) {
                slots.header()

                Spacer(modifier = Modifier.height(16.dp))

                slots.description()

                Spacer(modifier = Modifier.height(32.dp))

                slots.passwordField(
                    state.newPassword,
                    { onAction(ResetPasswordAction.NewPasswordChanged(it)) },
                    state.passwordError,
                    !state.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                slots.confirmPasswordField(
                    state.confirmPassword,
                    { onAction(ResetPasswordAction.ConfirmPasswordChanged(it)) },
                    state.confirmPasswordError,
                    !state.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                val isValid = state.newPassword.isNotBlank() &&
                        state.confirmPassword.isNotBlank() &&
                        state.passwordError == null &&
                        state.confirmPasswordError == null

                slots.submitButton(
                    { onAction(ResetPasswordAction.ResetPasswordClicked) },
                    state.isLoading,
                    isValid && !state.isLoading,
                    stringResource(Res.string.reset_password_screen_reset_button)
                )
            }
        }
    }
}
