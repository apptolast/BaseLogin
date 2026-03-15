package com.apptolast.customlogin.presentation.screens.phone

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.presentation.screens.components.CustomSnackBar
import com.apptolast.customlogin.presentation.screens.components.DefaultAuthContainer
import com.apptolast.customlogin.presentation.slots.PhoneAuthScreenSlots
import kotlinx.coroutines.flow.collectLatest
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.cd_navigate_back
import login.custom_login.generated.resources.phone_auth_screen_send_button
import login.custom_login.generated.resources.phone_auth_screen_title
import login.custom_login.generated.resources.phone_auth_screen_verify_button
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Entry-point composable for the Phone Authentication screen.
 * Connects the [PhoneAuthViewModel] to the UI and handles one-time effects.
 *
 * @param viewModel The [PhoneAuthViewModel] instance.
 * @param slots Customisable UI components for this screen.
 * @param onNavigateToHome Callback invoked when the user is successfully signed in.
 * @param onNavigateBack Callback to go back to the previous screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    viewModel: PhoneAuthViewModel = koinViewModel(),
    slots: PhoneAuthScreenSlots = PhoneAuthScreenSlots(),
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PhoneAuthEffect.NavigateToHome -> onNavigateToHome()
                is PhoneAuthEffect.ShowError -> {
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
                title = { Text(stringResource(Res.string.phone_auth_screen_title)) },
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
            SnackbarHost(snackbarHostState) { data ->
                CustomSnackBar(
                    snackBarText = data.visuals.message,
                    onDismiss = { snackbarHostState.currentSnackbarData?.dismiss() }
                )
            }
        },
        modifier = Modifier.consumeWindowInsets(TopAppBarDefaults.windowInsets)
    ) { paddingValues ->
        PhoneAuthContent(
            slots = slots,
            state = uiState,
            modifier = Modifier.padding(paddingValues),
            onAction = viewModel::onAction,
        )
    }
}

@Composable
private fun PhoneAuthContent(
    slots: PhoneAuthScreenSlots,
    state: PhoneAuthUiState,
    modifier: Modifier = Modifier,
    onAction: (PhoneAuthAction) -> Unit,
) {
    Box(modifier = modifier) {
        AnimatedContent(targetState = state.verificationId != null) { isOtpStep ->
            if (isOtpStep) {
                // --- Step 2: OTP entry ---
                DefaultAuthContainer(verticalArrangement = slots.layoutVerticalArrangement) {
                    slots.otpHeader()

                    Spacer(modifier = Modifier.height(8.dp))

                    slots.otpDescription(state.phoneNumber)

                    Spacer(modifier = Modifier.height(16.dp))

                    slots.otpField(
                        state.otpCode,
                        { onAction(PhoneAuthAction.OtpCodeChanged(it)) },
                        state.otpError,
                        !state.isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    slots.verifyButton(
                        { onAction(PhoneAuthAction.VerifyCodeClicked) },
                        state.isLoading,
                        state.otpCode.isNotBlank() && !state.isLoading,
                        stringResource(Res.string.phone_auth_screen_verify_button)
                    )
                }
            } else {
                // --- Step 1: Phone number entry ---
                DefaultAuthContainer(verticalArrangement = slots.layoutVerticalArrangement) {
                    slots.phoneHeader()

                    Spacer(modifier = Modifier.height(8.dp))

                    slots.phoneDescription()

                    Spacer(modifier = Modifier.height(16.dp))

                    slots.phoneField(
                        state.phoneNumber,
                        { onAction(PhoneAuthAction.PhoneNumberChanged(it)) },
                        state.phoneError,
                        !state.isLoading,
                        state.countryCode,
                        { onAction(PhoneAuthAction.CountryCodeChanged(it)) },
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    slots.sendCodeButton(
                        { onAction(PhoneAuthAction.SendCodeClicked) },
                        state.isLoading,
                        state.phoneNumber.isNotBlank() && !state.isLoading,
                        stringResource(Res.string.phone_auth_screen_send_button)
                    )
                }
            }
        }
    }
}
