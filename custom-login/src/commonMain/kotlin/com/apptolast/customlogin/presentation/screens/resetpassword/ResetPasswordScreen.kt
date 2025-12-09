package com.apptolast.customlogin.presentation.screens.resetpassword

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apptolast.customlogin.presentation.theme.ResetPasswordScreenSlots
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ResetPasswordRoute(
    resetCode: String = "",
    viewModel: ResetPasswordViewModel = koinViewModel(),
    resetPasswordSlots: ResetPasswordScreenSlots = ResetPasswordScreenSlots(),
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(resetCode) {
        if (resetCode.isNotBlank()) {
            viewModel.setResetCode(resetCode)
        }
    }

    LaunchedEffect(uiState.isPasswordReset) {
        if (uiState.isPasswordReset) {
            // Delay before navigating to give user time to see success
            kotlinx.coroutines.delay(2000)
            onSuccess()
        }
    }

    ResetPasswordScreen(
        resetPasswordSlots = resetPasswordSlots,
        newPassword = uiState.newPassword,
        confirmPassword = uiState.confirmPassword,
        passwordError = uiState.passwordError,
        confirmPasswordError = uiState.confirmPasswordError,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        isPasswordReset = uiState.isPasswordReset,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onResetClick = viewModel::resetPassword,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    resetPasswordSlots: ResetPasswordScreenSlots = ResetPasswordScreenSlots(),
    newPassword: String,
    confirmPassword: String,
    passwordError: String?,
    confirmPasswordError: String?,
    isLoading: Boolean,
    errorMessage: String?,
    isPasswordReset: Boolean,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onResetClick: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Password") },
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
            targetState = isPasswordReset,
            modifier = Modifier.padding(paddingValues)
        ) { passwordReset ->
            if (passwordReset) {
                // Success state
                resetPasswordSlots.successContent ?: SuccessContent(
                    onContinue = onNavigateBack
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

                    resetPasswordSlots.header ?: DefaultHeader()

                    Spacer(modifier = Modifier.height(16.dp))

                    resetPasswordSlots.description?.invoke() ?: DefaultDescription()

                    Spacer(modifier = Modifier.height(32.dp))

                    resetPasswordSlots.passwordField?.invoke(
                        newPassword,
                        onNewPasswordChange,
                        passwordError,
                        !isLoading
                    ) ?: DefaultPasswordField(
                        value = newPassword,
                        onValueChange = onNewPasswordChange,
                        error = passwordError,
                        enabled = !isLoading,
                        label = "New Password"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    resetPasswordSlots.confirmPasswordField?.invoke(
                        confirmPassword,
                        onConfirmPasswordChange,
                        confirmPasswordError,
                        !isLoading
                    ) ?: DefaultPasswordField(
                        value = confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        error = confirmPasswordError,
                        enabled = !isLoading,
                        label = "Confirm New Password"
                    )

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Password requirements hint
                    PasswordRequirements()

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit button
                    val isValid = newPassword.isNotBlank() &&
                            confirmPassword.isNotBlank() &&
                            passwordError == null &&
                            confirmPasswordError == null

                    resetPasswordSlots.submitButton?.invoke(
                        onResetClick,
                        isLoading,
                        isValid,
                        "Reset Password"
                    ) ?: DefaultSubmitButton(
                        onClick = onResetClick,
                        isLoading = isLoading,
                        enabled = isValid
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultHeader() {
    Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Create New Password",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun DefaultDescription() {
    Text(
        text = "Your new password must be different from previously used passwords.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun DefaultPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    label: String
) {
    var isVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = label) },
        trailingIcon = {
            IconButton(onClick = { isVisible = !isVisible }) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (isVisible) "Hide password" else "Show password"
                )
            }
        },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        isError = error != null,
        supportingText = error?.let { { Text(it) } }
    )
}

@Composable
private fun PasswordRequirements() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Password must contain:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        RequirementItem("At least 6 characters")
        RequirementItem("One uppercase letter")
        RequirementItem("One lowercase letter")
        RequirementItem("One number")
    }
}

@Composable
private fun RequirementItem(text: String) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            Text("Reset Password")
        }
    }
}

@Composable
private fun SuccessContent(onContinue: () -> Unit) {
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
            text = "Password Reset!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your password has been successfully reset. You can now sign in with your new password.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue to Sign In")
        }
    }
}
