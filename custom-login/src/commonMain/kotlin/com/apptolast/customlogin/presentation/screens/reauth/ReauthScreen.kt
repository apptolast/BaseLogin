package com.apptolast.customlogin.presentation.screens.reauth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
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
import com.apptolast.customlogin.presentation.slots.ReauthScreenSlots
import com.apptolast.customlogin.presentation.util.toStringRes
import com.apptolast.customlogin.util.toStringRes
import kotlinx.coroutines.flow.collectLatest
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.reauth_submit_button
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Standalone re-authentication screen.
 *
 * NOT part of [com.apptolast.customlogin.presentation.navigation.RootNavGraph] — the consumer
 * places it in their own navigation graph wherever a sensitive operation requires identity
 * confirmation (e.g. before deleting an account or changing email/password).
 *
 * @param onSuccess Called after the user has been successfully re-authenticated.
 * @param onCancel Called when the user dismisses or backs out of the screen.
 * @param modifier Optional modifier for the root composable.
 * @param slots Slots for customizing individual UI components.
 * @param viewModel Koin-injected [ReauthViewModel]. Override in tests.
 */
@Composable
fun ReauthScreen(
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    slots: ReauthScreenSlots = ReauthScreenSlots(),
    viewModel: ReauthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ReauthEffect.Success -> onSuccess()
                is ReauthEffect.Error -> snackBarHostState.showSnackbar(
                    message = effect.message,
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite
                )
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
        DefaultAuthContainer(modifier = modifier.padding(paddingValues)) {
            slots.header()

            Spacer(Modifier.height(8.dp))

            slots.description()

            Spacer(Modifier.height(16.dp))

            slots.emailField(
                uiState.email,
                { viewModel.onAction(ReauthAction.EmailChanged(it)) },
                uiState.emailError?.let { stringResource(it.toStringRes()) },
                !uiState.isLoading,
            )

            Spacer(Modifier.height(8.dp))

            slots.passwordField(
                uiState.password,
                { viewModel.onAction(ReauthAction.PasswordChanged(it)) },
                uiState.passwordError?.let { stringResource(it.toStringRes()) },
                !uiState.isLoading,
            )

            // Show auth-level error (e.g. wrong password) if present
            uiState.authError?.let { error ->
                Spacer(Modifier.height(4.dp))
                slots.errorMessage(stringResource(error.toStringRes()))
            }

            Spacer(Modifier.height(16.dp))

            val isFormValid = uiState.email.isNotBlank() && uiState.password.isNotBlank()
            slots.submitButton(
                { viewModel.onAction(ReauthAction.SubmitEmailPassword) },
                uiState.isLoading,
                isFormValid && !uiState.isLoading && uiState.loadingProvider == null,
                stringResource(Res.string.reauth_submit_button),
            )

            if (uiState.availableProviders.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                slots.socialSection(
                    uiState.availableProviders,
                    uiState.loadingProvider,
                ) { viewModel.onAction(ReauthAction.SubmitOAuth(it)) }
            }
        }
    }
}
