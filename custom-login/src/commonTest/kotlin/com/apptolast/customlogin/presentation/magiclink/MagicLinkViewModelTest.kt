package com.apptolast.customlogin.presentation.magiclink

import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.presentation.screens.magiclink.MagicLinkAction
import com.apptolast.customlogin.presentation.screens.magiclink.MagicLinkEffect
import com.apptolast.customlogin.presentation.screens.magiclink.MagicLinkViewModel
import com.apptolast.customlogin.test.FakeAuthRepository
import com.apptolast.customlogin.util.ValidationError
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
class MagicLinkViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: MagicLinkViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
        viewModel = MagicLinkViewModel(repo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty email and no error`() {
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertNull(state.emailError)
        assertFalse(state.isLinkSent)
        assertFalse(state.isLoading)
    }

    @Test
    fun `SendLinkClicked with blank email sets EmailEmpty error`() = runTest {
        viewModel.onAction(MagicLinkAction.SendLinkClicked)
        assertEquals(ValidationError.EmailEmpty, viewModel.uiState.value.emailError)
    }

    @Test
    fun `SendLinkClicked with invalid email sets EmailInvalid error`() = runTest {
        viewModel.onAction(MagicLinkAction.EmailChanged("not-an-email"))
        viewModel.onAction(MagicLinkAction.SendLinkClicked)
        assertEquals(ValidationError.EmailInvalid, viewModel.uiState.value.emailError)
    }

    @Test
    fun `EmailChanged clears emailError`() = runTest {
        viewModel.onAction(MagicLinkAction.SendLinkClicked) // trigger error
        viewModel.onAction(MagicLinkAction.EmailChanged("valid@email.com"))
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `SendLinkClicked with valid email and success sets isLinkSent true`() = runTest {
        repo.sendMagicLinkResult = AuthResult.MagicLinkSent

        viewModel.onAction(MagicLinkAction.EmailChanged("user@example.com"))
        viewModel.onAction(MagicLinkAction.SendLinkClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLinkSent)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `SendLinkClicked on failure emits ShowError`() = runTest {
        repo.sendMagicLinkResult = AuthResult.Failure(AuthError.NetworkError())
        val effects = mutableListOf<MagicLinkEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(MagicLinkAction.EmailChanged("user@example.com"))
        viewModel.onAction(MagicLinkAction.SendLinkClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLinkSent)
        assertIs<MagicLinkEffect.ShowError>(effects.first())
        job.cancel()
    }

    @Test
    fun `SendLinkClicked does not call repo when email is blank`() = runTest {
        viewModel.onAction(MagicLinkAction.SendLinkClicked)
        // isLinkSent would be true if repo was called (default result is MagicLinkSent)
        assertFalse(viewModel.uiState.value.isLinkSent)
    }

    @Test
    fun `SendLinkClicked trims whitespace from email`() = runTest {
        repo.sendMagicLinkResult = AuthResult.MagicLinkSent

        viewModel.onAction(MagicLinkAction.EmailChanged("  user@example.com  "))
        viewModel.onAction(MagicLinkAction.SendLinkClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLinkSent)
    }

    @Test
    fun `isLoading is false after successful send`() = runTest {
        repo.sendMagicLinkResult = AuthResult.MagicLinkSent

        viewModel.onAction(MagicLinkAction.EmailChanged("user@example.com"))
        viewModel.onAction(MagicLinkAction.SendLinkClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }
}
