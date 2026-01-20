package com.apptolast.customlogin.presentation.screens.resetpassword

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.presentation.screens.components.DefaultAuthContainer
import com.apptolast.customlogin.presentation.theme.ResetPasswordScreenSlots
import kotlinx.coroutines.delay
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
 * @param onSuccess A callback invoked upon successful password reset.
 */
@Composable
fun ResetPasswordScreen(
    resetCode: String = "",
    viewModel: ResetPasswordViewModel = koinViewModel(),
    resetPasswordSlots: ResetPasswordScreenSlots = ResetPasswordScreenSlots(),
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit = onNavigateBack
) {
    val uiState by viewModel.uiState.collectAsState()

    with(uiState) {

        LaunchedEffect(resetCode) {
            if (resetCode.isNotBlank()) {
                viewModel.setResetCode(resetCode)
            }
        }

        LaunchedEffect(isPasswordReset) {
            if (isPasswordReset) {
                delay(2000)
                onSuccess()
            }
        }

        ResetPasswordContent(
            slots = resetPasswordSlots,
            isPasswordReset = isPasswordReset,
            isLoading = isLoading,
            newPassword = newPassword,
            confirmPassword = confirmPassword,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            onNewPasswordChange = viewModel::onNewPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onResetClick = viewModel::resetPassword,
            onNavigateBack = onNavigateBack
        )
    }
}

/**
 * A private composable that defines the layout and UI for the Reset Password screen.
 * It is stateless regarding business logic and field states, receiving all data and callbacks.
 *
 * @param slots The [ResetPasswordScreenSlots] defining the UI components.
 * @param isPasswordReset A flag indicating if the password reset is complete.
 * @param isLoading A flag indicating if the screen is currently loading.
 * @param newPassword The current password input value.
 * @param confirmPassword The current confirm password input value.
 * @param passwordError An optional error message for the password input.
 * @param confirmPasswordError An optional error message for the confirm password input.
 * @param onNewPasswordChange A callback for new password input changes.
 * @param onConfirmPasswordChange A callback for confirm password input changes.
 * @param onResetClick A callback invoked when the reset button is clicked.
 * @param onNavigateBack A callback to navigate to the previous screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResetPasswordContent(
    slots: ResetPasswordScreenSlots,
    isPasswordReset: Boolean,
    isLoading: Boolean,
    newPassword: String,
    confirmPassword: String,
    passwordError: String?,
    confirmPasswordError: String?,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onResetClick: () -> Unit,
    onNavigateBack: () -> Unit,
) {
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
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = isPasswordReset,
            modifier = Modifier.padding(paddingValues)
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
                        newPassword,
                        onNewPasswordChange,
                        passwordError,
                        !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    slots.confirmPasswordField(
                        confirmPassword,
                        onConfirmPasswordChange,
                        confirmPasswordError,
                        !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val isValid = newPassword.isNotBlank() &&
                            confirmPassword.isNotBlank() &&
                            passwordError == null &&
                            confirmPasswordError == null

                    slots.submitButton(
                        onResetClick,
                        isLoading,
                        isValid,
                        stringResource(Res.string.reset_password_screen_reset_button)
                    )
                }
            }
        }
    }
}
