package com.apptolast.customlogin.presentation.screens.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.apptolast.customlogin.presentation.theme.AuthScreenSlots
import com.apptolast.customlogin.presentation.theme.DefaultNameField
import com.apptolast.customlogin.presentation.theme.DefaultEmailField
import com.apptolast.customlogin.presentation.theme.DefaultHeader
import com.apptolast.customlogin.presentation.theme.DefaultPasswordField
import com.apptolast.customlogin.presentation.theme.DefaultSubmitButton
import com.apptolast.customlogin.presentation.theme.DefaultTermsCheckbox
import com.apptolast.customlogin.presentation.theme.RegisterScreenSlots
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = koinViewModel(),
    registerSlots: RegisterScreenSlots = RegisterScreenSlots(),
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.user) {
        if (uiState.user != null) {
            onRegisterSuccess()
        }
    }

    RegisterContent(
        registerSlots = registerSlots,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onRegisterClick = viewModel::createUserWithEmail,
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
fun RegisterContent(
    registerSlots: RegisterScreenSlots = RegisterScreenSlots(),
    isLoading: Boolean,
    errorMessage: String?,
    onRegisterClick: (String, String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var termsAccepted by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        registerSlots.header ?: DefaultHeader()

        Spacer(modifier = Modifier.height(16.dp))

        // Name Field
        (registerSlots.nameField?.invoke(
            name,
            { name = it },
            errorMessage,
            !isLoading
        ) ?: DefaultNameField(
            value = name,
            onValueChange = { name = it },
            error = errorMessage,
            enabled = !isLoading
        ))

        Spacer(modifier = Modifier.height(8.dp))

        // Email Field
        (registerSlots.emailField?.invoke(
            email,
            { email = it },
            errorMessage,
            !isLoading
        ) ?: DefaultEmailField(
            value = email,
            onValueChange = { email = it },
            error = errorMessage,
            enabled = !isLoading
        ))

        Spacer(modifier = Modifier.height(8.dp))

        // Password Field
        (registerSlots.passwordField?.invoke(
            password,
            { password = it },
            errorMessage,
            !isLoading
        ) ?: DefaultPasswordField(
            value = password,
            onValueChange = { password = it },
            error = errorMessage,
            enabled = !isLoading
        ))

        Spacer(modifier = Modifier.height(8.dp))

        // Confirm Password Field
        (registerSlots.confirmPasswordField?.invoke(
            confirmPassword,
            { confirmPassword = it },
            errorMessage,
            !isLoading,
        ) ?: DefaultPasswordField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            error = errorMessage,
            enabled = !isLoading,
        ))

        Spacer(modifier = Modifier.height(8.dp))

        // Terms and Conditions
        (registerSlots.termsCheckbox?.invoke(termsAccepted) { termsAccepted = it }
            ?: DefaultTermsCheckbox(
                checked = termsAccepted,
                onCheckedChange = { termsAccepted = it }
            ))

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button
//        FIXME: Refactor logic to viewmodel
        val canRegister =
            name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && password == confirmPassword && termsAccepted
        (registerSlots.submitButton ?: DefaultSubmitButton(
            onClick = { onRegisterClick(name, email, password, confirmPassword) },
            isLoading = isLoading,
            enabled = canRegister,
            text = "Register"
        ))

        Spacer(modifier = Modifier.height(8.dp))

        // Login Link
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Already have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToLogin) {
                Text("Sign In")
            }
        }
    }
}
