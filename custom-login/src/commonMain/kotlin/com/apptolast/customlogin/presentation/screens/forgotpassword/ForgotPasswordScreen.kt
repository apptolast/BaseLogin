package com.apptolast.customlogin.presentation.screens.forgotpassword

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.presentation.theme.ForgotPasswordScreenSlots
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ForgotPasswordRoute(
    viewModel: ForgotPasswordViewModel = koinViewModel(),
    slots: ForgotPasswordScreenSlots = ForgotPasswordScreenSlots(),
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    ForgotPasswordScreen(
        email = uiState.email,
        onEmailChange = viewModel::onEmailChange,
        emailError = uiState.emailError,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        isEmailSent = uiState.isEmailSent,
        onSendClick = viewModel::sendPasswordResetEmail,
        onNavigateBack = onNavigateBack,
        onSuccess = onSuccess,
        slots = slots
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    isLoading: Boolean,
    errorMessage: String?,
    isEmailSent: Boolean,
    onSendClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit = {},
    slots: ForgotPasswordScreenSlots = ForgotPasswordScreenSlots()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = isEmailSent,
            modifier = Modifier.padding(paddingValues)
        ) { emailSent ->
            if (emailSent) {
                // Success state
                slots.successContent?.invoke(email) ?: SuccessContent(
                    email = email,
                    onBackToLogin = onNavigateBack
                )
            } else {
                // Form state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Header
                    slots.header?.invoke() ?: DefaultHeader()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    slots.description?.invoke() ?: DefaultDescription()

                    Spacer(modifier = Modifier.height(32.dp))

                    // Email field
                    slots.emailField?.invoke(email, onEmailChange, emailError, !isLoading)
                        ?: DefaultEmailField(
                            email = email,
                            onEmailChange = onEmailChange,
                            emailError = emailError,
                            enabled = !isLoading
                        )

                    // Error message
                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit button
                    slots.submitButton?.invoke(
                        onSendClick,
                        isLoading,
                        email.isNotBlank(),
                        "Send Reset Link"
                    ) ?: DefaultSubmitButton(
                        onClick = onSendClick,
                        isLoading = isLoading,
                        enabled = email.isNotBlank()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Back to login link
                    slots.backToLoginLink?.invoke(onNavigateBack) ?: BackToLoginLink(onNavigateBack)
                }
            }
        }
    }
}

@Composable
private fun DefaultHeader() {
    Icon(
        imageVector = Icons.Default.Email,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Forgot Password?",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun DefaultDescription() {
    Text(
        text = "Enter your email address and we'll send you a link to reset your password.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun DefaultEmailField(
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    enabled: Boolean
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        isError = emailError != null,
        supportingText = emailError?.let { { Text(it) } }
    )
}

@Composable
private fun DefaultSubmitButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Send Reset Link")
        }
    }
}

@Composable
private fun BackToLoginLink(onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Remember your password?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = onClick) {
            Text("Sign In")
        }
    }
}

@Composable
private fun SuccessContent(
    email: String,
    onBackToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Email Sent!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "We've sent a password reset link to:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please check your inbox and follow the instructions to reset your password.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Sign In")
        }
    }
}
