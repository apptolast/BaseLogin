package com.apptolast.customlogin.presentation.resetpassword

import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.di.LoginLibraryConfig
import com.apptolast.customlogin.presentation.screens.resetpassword.ResetPasswordAction
import com.apptolast.customlogin.presentation.screens.resetpassword.ResetPasswordEffect
import com.apptolast.customlogin.presentation.screens.resetpassword.ResetPasswordViewModel
import com.apptolast.customlogin.test.FakeAuthRepository
import com.apptolast.customlogin.util.ValidationError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ResetPasswordViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: ResetPasswordViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
        viewModel = ResetPasswordViewModel(repo, LoginLibraryConfig())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setResetCode updates resetCode in state`() {
        viewModel.setResetCode("abc123")
        assertEquals("abc123", viewModel.uiState.value.resetCode)
    }

    @Test
    fun `ResetPasswordClicked with blank password sets PasswordEmpty error`() = runTest {
        viewModel.onAction(ResetPasswordAction.ConfirmPasswordChanged("confirm"))
        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        assertEquals(ValidationError.PasswordEmpty, viewModel.uiState.value.passwordError)
    }

    @Test
    fun `ResetPasswordClicked with short password sets PasswordTooShort error`() = runTest {
        viewModel.onAction(ResetPasswordAction.NewPasswordChanged("123"))
        viewModel.onAction(ResetPasswordAction.ConfirmPasswordChanged("123"))
        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        assertEquals(ValidationError.PasswordTooShort, viewModel.uiState.value.passwordError)
    }

    @Test
    fun `ResetPasswordClicked with blank confirm sets ConfirmPasswordEmpty error`() = runTest {
        viewModel.onAction(ResetPasswordAction.NewPasswordChanged("validPass"))
        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        assertEquals(ValidationError.ConfirmPasswordEmpty, viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun `ResetPasswordClicked with mismatched passwords sets PasswordsDoNotMatch error`() = runTest {
        viewModel.onAction(ResetPasswordAction.NewPasswordChanged("validPass"))
        viewModel.onAction(ResetPasswordAction.ConfirmPasswordChanged("different"))
        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        assertEquals(ValidationError.PasswordsDoNotMatch, viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun `ResetPasswordClicked on success sets isSuccess and emits NavigateToLogin`() = runTest {
        repo.confirmPasswordResetResult = AuthResult.PasswordResetSuccess
        val effects = mutableListOf<ResetPasswordEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.setResetCode("code123")
        viewModel.onAction(ResetPasswordAction.NewPasswordChanged("newSecret"))
        viewModel.onAction(ResetPasswordAction.ConfirmPasswordChanged("newSecret"))
        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertIs<ResetPasswordEffect.NavigateToLogin>(effects.first())
        job.cancel()
    }

    @Test
    fun `ResetPasswordClicked on failure emits ShowError`() = runTest {
        repo.confirmPasswordResetResult = AuthResult.Failure(AuthError.InvalidResetCode())
        val effects = mutableListOf<ResetPasswordEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.setResetCode("bad-code")
        viewModel.onAction(ResetPasswordAction.NewPasswordChanged("newSecret"))
        viewModel.onAction(ResetPasswordAction.ConfirmPasswordChanged("newSecret"))
        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSuccess)
        assertIs<ResetPasswordEffect.ShowError>(effects.first())
        job.cancel()
    }

    @Test
    fun `NewPasswordChanged clears passwordError`() = runTest {
        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked) // trigger errors
        viewModel.onAction(ResetPasswordAction.NewPasswordChanged("newpass"))
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `ConfirmPasswordChanged clears confirmPasswordError`() = runTest {
        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked) // trigger errors
        viewModel.onAction(ResetPasswordAction.ConfirmPasswordChanged("confirm"))
        assertNull(viewModel.uiState.value.confirmPasswordError)
    }
}
