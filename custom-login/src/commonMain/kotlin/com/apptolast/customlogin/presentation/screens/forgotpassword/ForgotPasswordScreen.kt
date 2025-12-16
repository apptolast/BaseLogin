package com.apptolast.customlogin.presentation.screens.forgotpassword

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.presentation.screens.components.DefaultAuthContainer
import com.apptolast.customlogin.presentation.theme.ForgotPasswordScreenSlots
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.cd_navigate_back
import login.custom_login.generated.resources.forgot_password_screen_send_button
import login.custom_login.generated.resources.forgot_password_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * A composable function that represents the main entry point for the Forgot Password screen.
 * It connects the ViewModel to the UI content and handles UI state.
 *
 * @param viewModel The [ForgotPasswordViewModel] instance for this screen.
 * @param slots An instance of [ForgotPasswordScreenSlots] to customize the UI components.
 * @param onNavigateBack A callback to navigate to the previous screen.
 */
@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = koinViewModel(),
    slots: ForgotPasswordScreenSlots = ForgotPasswordScreenSlots(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    with(uiState) {
        ForgotPasswordContent(
            slots = slots,
            email = email,
            emailError = emailError,
            isLoading = isLoading,
            isSuccess = isSuccessPasswordResetSent,
            errorMessage = errorMessage,
            onEmailChange = viewModel::onEmailChange,
            onSendClick = viewModel::sendPasswordResetEmail,
            onNavigateBack = onNavigateBack
        )
    }
}

/**
 * A private composable that defines the layout and UI for the Forgot Password screen.
 * It is stateless regarding business logic and field states, receiving all data and callbacks.
 *
 * @param slots The [ForgotPasswordScreenSlots] defining the UI components.
 * @param email The current email value.
 * @param emailError An optional error for the email field.
 * @param isLoading A flag indicating if the screen is currently loading.
 * @param isSuccess A flag indicating if a password reset email was sent successfully.
 * @param errorMessage The error message to be displayed, if any.
 * @param onEmailChange A callback for email input changes.
 * @param onSendClick A callback invoked when the send button is clicked.
 * @param onNavigateBack A callback to navigate to the previous screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForgotPasswordContent(
    slots: ForgotPasswordScreenSlots,
    email: String,
    emailError: String?,
    isLoading: Boolean,
    isSuccess: Boolean,
    errorMessage: String?,
    onEmailChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
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
        modifier = Modifier.consumeWindowInsets(TopAppBarDefaults.windowInsets)
    ) { paddingValues ->
        Box(modifier = Modifier.consumeWindowInsets(paddingValues)) {
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
                            email,
                            onEmailChange,
                            emailError ?: errorMessage,
                            !isLoading
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        slots.submitButton(
                            onSendClick,
                            isLoading,
                            email.isNotBlank() && !isLoading,
                            stringResource(Res.string.forgot_password_screen_send_button)
                        )
                    }
                }
            }
        }
    }
}
