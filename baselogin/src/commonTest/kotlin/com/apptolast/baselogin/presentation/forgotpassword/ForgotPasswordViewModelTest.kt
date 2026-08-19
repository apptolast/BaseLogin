package com.apptolast.baselogin.presentation.forgotpassword

import com.apptolast.baselogin.domain.model.AuthError
import com.apptolast.baselogin.domain.model.AuthResult
import com.apptolast.baselogin.presentation.screens.forgotpassword.ForgotPasswordAction
import com.apptolast.baselogin.presentation.screens.forgotpassword.ForgotPasswordEffect
import com.apptolast.baselogin.presentation.screens.forgotpassword.ForgotPasswordViewModel
import com.apptolast.baselogin.test.FakeAuthRepository
import com.apptolast.baselogin.util.ValidationError
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: ForgotPasswordViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
        viewModel = ForgotPasswordViewModel(repo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `SendResetEmailClicked with blank email sets EmailEmpty error`() = runTest {
        viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
        assertEquals(ValidationError.EmailEmpty, viewModel.uiState.value.emailError)
    }

    @Test
    fun `SendResetEmailClicked with invalid email sets EmailInvalid error`() = runTest {
        viewModel.onAction(ForgotPasswordAction.EmailChanged("bad-email"))
        viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
        assertEquals(ValidationError.EmailInvalid, viewModel.uiState.value.emailError)
    }

    @Test
    fun `EmailChanged clears emailError`() = runTest {
        viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
        viewModel.onAction(ForgotPasswordAction.EmailChanged("valid@email.com"))
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `SendResetEmailClicked with valid email and success sets isSuccess true`() = runTest {
        repo.sendPasswordResetEmailResult = AuthResult.PasswordResetSent

        viewModel.onAction(ForgotPasswordAction.EmailChanged("user@example.com"))
        viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `SendResetEmailClicked on failure emits ShowError`() = runTest {
        repo.sendPasswordResetEmailResult = AuthResult.Failure(AuthError.UserNotFound())
        val effects = mutableListOf<ForgotPasswordEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(ForgotPasswordAction.EmailChanged("user@example.com"))
        viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
        advanceUntilIdle()

        assertIs<ForgotPasswordEffect.ShowError>(effects.first())
        job.cancel()
    }

    @Test
    fun `SuccessDismissed emits NavigateBack`() = runTest {
        val effects = mutableListOf<ForgotPasswordEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(ForgotPasswordAction.SuccessDismissed)
        advanceUntilIdle()

        assertIs<ForgotPasswordEffect.NavigateBack>(effects.first())
        job.cancel()
    }

    @Test
    fun `loading is false initially`() {
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `isSuccess is false initially`() {
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `SendResetEmailClicked does not call repo when email is blank`() = runTest {
        viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
        // If repo was called, it would set isSuccess (default is PasswordResetSent)
        assertFalse(viewModel.uiState.value.isSuccess)
    }
}
