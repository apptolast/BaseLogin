package com.apptolast.customlogin.presentation.screens.magiclink

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
import com.apptolast.customlogin.presentation.slots.MagicLinkScreenSlots
import kotlinx.coroutines.flow.collectLatest
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.magic_link_screen_send_button
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MagicLinkScreen(
    viewModel: MagicLinkViewModel = koinViewModel(),
    slots: MagicLinkScreenSlots = MagicLinkScreenSlots(),
    onNavigateBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MagicLinkEffect.ShowError -> {
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
        MagicLinkContent(
            slots = slots,
            state = uiState,
            modifier = Modifier.padding(paddingValues),
            onAction = viewModel::onAction,
        )
    }
}

@Composable
private fun MagicLinkContent(
    slots: MagicLinkScreenSlots,
    state: MagicLinkUiState,
    modifier: Modifier = Modifier,
    onAction: (MagicLinkAction) -> Unit = {},
) {
    DefaultAuthContainer(modifier = modifier) {
        if (state.isLinkSent) {
            slots.successContent(state.email)
        } else {
            slots.header()

            Spacer(Modifier.height(8.dp))

            slots.description()

            Spacer(Modifier.height(16.dp))

            slots.emailField(
                state.email,
                { onAction(MagicLinkAction.EmailChanged(it)) },
                state.emailError,
                !state.isLoading
            )

            Spacer(Modifier.height(16.dp))

            slots.submitButton(
                { onAction(MagicLinkAction.SendLinkClicked) },
                state.isLoading,
                state.email.isNotBlank() && !state.isLoading,
                stringResource(Res.string.magic_link_screen_send_button)
            )
        }
    }
}