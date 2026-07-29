package com.apptolast.customlogin.presentation.screens.magiclink

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.presentation.screens.components.CustomSnackBar
import com.apptolast.customlogin.presentation.screens.components.DefaultAuthContainer
import com.apptolast.customlogin.presentation.slots.MagicLinkScreenSlots
import com.apptolast.customlogin.presentation.util.toStringRes
import com.apptolast.customlogin.util.toStringRes
import kotlinx.coroutines.flow.collectLatest
import login.custom_login.generated.resources.Res
import login.custom_login.generated.resources.cd_navigate_back
import login.custom_login.generated.resources.magic_link_screen_send_button
import login.custom_login.generated.resources.magic_link_screen_title
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
                        message = getString(effect.error.toStringRes()),
                        withDismissAction = true,
                        duration = SnackbarDuration.Indefinite,
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.magic_link_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.cd_navigate_back),
                        )
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState) { snackBarData ->
                CustomSnackBar(
                    snackBarText = snackBarData.visuals.message,
                    onDismiss = { snackBarHostState.currentSnackbarData?.dismiss() },
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
    ) { paddingValues ->
        MagicLinkContent(
            slots = slots,
            state = uiState,
            modifier = Modifier.padding(paddingValues).consumeWindowInsets(paddingValues),
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
                state.emailError?.let { stringResource(it.toStringRes()) },
                !state.isLoading,
            )

            Spacer(Modifier.height(16.dp))

            slots.submitButton(
                { onAction(MagicLinkAction.SendLinkClicked) },
                state.isLoading,
                state.email.isNotBlank() && !state.isLoading,
                stringResource(Res.string.magic_link_screen_send_button),
            )
        }
    }
}
