package com.apptolast.customlogin.presentation.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.presentation.theme.DefaultDivider
import com.apptolast.customlogin.presentation.theme.DefaultEmailField
import com.apptolast.customlogin.presentation.theme.DefaultHeader
import com.apptolast.customlogin.presentation.theme.DefaultPasswordField
import com.apptolast.customlogin.presentation.theme.DefaultRegisterLink
import com.apptolast.customlogin.presentation.theme.DefaultSubmitButton
import com.apptolast.customlogin.presentation.theme.DefaultTextLink
import com.apptolast.customlogin.presentation.theme.LoginScreenSlots
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    loginSlots: LoginScreenSlots = LoginScreenSlots(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToResetPassword: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.user) {
//        uiState.user?.let(onLoginSuccess())
    }

    LoginContent(
        loginSlots = loginSlots,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onLoginClick = viewModel::signInWithEmail,
        onNavigateToRegister = onNavigateToRegister,
        onNavigateToForgotPassword = onNavigateToResetPassword
    )
}

@Composable
fun LoginContent(
    loginSlots: LoginScreenSlots,
    isLoading: Boolean,
    errorMessage: String?,
    onLoginClick: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        loginSlots.header ?: DefaultHeader()

        Spacer(modifier = Modifier.height(16.dp))

        (loginSlots.emailField ?: { _, _, _, _ ->
            DefaultEmailField(
                value = email,
                onValueChange = { email = it },
                error = errorMessage,
                enabled = !isLoading
            )
        }).invoke(email, { email = it }, errorMessage, !isLoading)

        Spacer(modifier = Modifier.height(8.dp))

        (loginSlots.passwordField ?: { _, _, _, _ ->
            DefaultPasswordField(
                value = password,
                onValueChange = { password = it },
                error = errorMessage,
                enabled = !isLoading
            )
        }).invoke(password, { password = it }, errorMessage, !isLoading)

        (loginSlots.forgotPasswordLink ?: {
            DefaultTextLink(
                text = "Forgot Password?",
                onClick = onNavigateToForgotPassword,
            )
        }).invoke(onNavigateToForgotPassword)

        Spacer(modifier = Modifier.height(16.dp))

        (loginSlots.submitButton ?: { _, _, _, text ->
            DefaultSubmitButton(
                onClick = { onLoginClick(email, password) },
                isLoading = isLoading,
                enabled = email.isNotBlank() && password.isNotBlank(),
                text = text
            )
        }).invoke(
            { onLoginClick(email, password) },
            isLoading,
            email.isNotBlank() && password.isNotBlank(),
            "Sign In"
        )

        (loginSlots.socialProviders ?: {
            DefaultDivider(text = "OR")
        }).invoke { /* onProviderClick */ }

        (loginSlots.registerLink ?: {
            DefaultRegisterLink(onRegisterClick = onNavigateToRegister)
        }).invoke(onNavigateToRegister)
    }
}
