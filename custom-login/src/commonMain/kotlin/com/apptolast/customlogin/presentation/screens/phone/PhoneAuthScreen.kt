package com.apptolast.customlogin.presentation.screens.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    viewModel: PhoneAuthViewModel = koinViewModel(),
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    // TODO: Handle effects for navigation and errors

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign in with Phone") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (!uiState.isCodeSent) {
                PhoneNumberInput(uiState, viewModel::onAction)
            } else {
                VerificationCodeInput(uiState, viewModel::onAction)
            }

            uiState.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            if (uiState.isLoading) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun PhoneNumberInput(uiState: PhoneAuthUiState, onAction: (PhoneAuthAction) -> Unit) {
    OutlinedTextField(
        value = uiState.phoneNumber,
        onValueChange = { onAction(PhoneAuthAction.PhoneNumberChanged(it)) },
        label = { Text("Phone Number") },
        modifier = Modifier.fillMaxWidth(),
        isError = uiState.error != null,
        enabled = !uiState.isLoading
    )

    Spacer(Modifier.height(16.dp))

    Button(
        onClick = { onAction(PhoneAuthAction.SendVerificationCode) },
        modifier = Modifier.fillMaxWidth(),
        enabled = uiState.phoneNumber.isNotBlank() && !uiState.isLoading
    ) {
        Text("Send Verification Code")
    }
}

@Composable
private fun VerificationCodeInput(uiState: PhoneAuthUiState, onAction: (PhoneAuthAction) -> Unit) {
    Text("Enter the code sent to ${uiState.phoneNumber}")

    Spacer(Modifier.height(16.dp))

    OutlinedTextField(
        value = uiState.verificationCode,
        onValueChange = { onAction(PhoneAuthAction.VerificationCodeChanged(it)) },
        label = { Text("Verification Code") },
        modifier = Modifier.fillMaxWidth(),
        isError = uiState.error != null,
        enabled = !uiState.isLoading
    )

    Spacer(Modifier.height(16.dp))

    Button(
        onClick = { onAction(PhoneAuthAction.SignInWithCode) },
        modifier = Modifier.fillMaxWidth(),
        enabled = uiState.verificationCode.isNotBlank() && !uiState.isLoading
    ) {
        Text("Sign In")
    }
}
