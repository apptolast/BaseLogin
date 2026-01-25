package com.apptolast.customlogin.presentation.screens.forgotpassword

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
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
import com.apptolast.customlogin.presentation.slots.ForgotPasswordScreenSlots
import kotlinx.coroutines.flow.collectLatest
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.cd_navigate_back
import login.custom_login.generated.resources.forgot_password_screen_send_button
import login.custom_login.generated.resources.forgot_password_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * A composable function that represents the main entry point for the Forgot Password screen.
 * It connects the ViewModel to the UI content and handles MVI effects.
 *
 * @param viewModel The [ForgotPasswordViewModel] instance for this screen.
 * @param slots An instance of [ForgotPasswordScreenSlots] to customize the UI components.
 * @param onNavigateBack A callback to navigate to the previous screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = koinViewModel(),
    slots: ForgotPasswordScreenSlots = ForgotPasswordScreenSlots(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    // FIXME: The isSuccess logic should be in the ViewModel
    var isSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ForgotPasswordEffect.ResetEmailSent -> isSuccess = true
                is ForgotPasswordEffect.ShowError -> {
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
                title = {
                    Text(stringResource(Res.string.forgot_password_screen_title))
                },
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
        ForgotPasswordContent(
            slots = slots,
            state = uiState,
            modifier = Modifier.padding(paddingValues),
            isSuccess = isSuccess,
            onAction = viewModel::onAction
        )
    }
}

/**
 * A private composable that defines the layout and UI for the Forgot Password screen.
 * It is stateless and receives all data and callbacks as parameters.
 *
 * @param slots The [ForgotPasswordScreenSlots] defining the UI components.
 * @param state The current [ForgotPasswordUiState] of the screen.
 * @param isSuccess A flag indicating if a password reset email was sent successfully.
 * @param modifier The modifier to be applied to the root container, including padding from the Scaffold.
 * @param onAction A callback to send actions to the ViewModel.
 */
@Composable
private fun ForgotPasswordContent(
    slots: ForgotPasswordScreenSlots,
    state: ForgotPasswordUiState,
    isSuccess: Boolean,
    modifier: Modifier = Modifier,
    onAction: (ForgotPasswordAction) -> Unit,
) {
    Box(modifier = modifier) {
        AnimatedContent(
            targetState = isSuccess,
        ) { success ->
            if (success) {
                slots.successContent()
            } else {
                DefaultAuthContainer(
                    verticalArrangement = slots.layoutVerticalArrangement,
                ) {
                    slots.header()

                    Spacer(modifier = Modifier.height(8.dp))

                    slots.description()

                    Spacer(modifier = Modifier.height(16.dp))

                    slots.emailField(
                        state.email,
                        { onAction(ForgotPasswordAction.EmailChanged(it)) },
                        state.emailError,
                        !state.isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    slots.submitButton(
                        { onAction(ForgotPasswordAction.SendResetEmailClicked) },
                        state.isLoading,
                        state.email.isNotBlank() && !state.isLoading,
                        stringResource(Res.string.forgot_password_screen_send_button)
                    )
                }
            }
        }
    }
}
