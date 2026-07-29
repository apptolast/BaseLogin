package com.apptolast.customlogin.presentation.reauth

import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.presentation.screens.reauth.ReauthAction
import com.apptolast.customlogin.presentation.screens.reauth.ReauthEffect
import com.apptolast.customlogin.presentation.screens.reauth.ReauthViewModel
import com.apptolast.customlogin.test.FakeAuthRepository
import com.apptolast.customlogin.util.ValidationError
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
class ReauthViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: ReauthViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
        viewModel = ReauthViewModel(repo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ─────────────────────────────────────────────────────

    @Test
    fun `initial state has empty fields and no errors`() {
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.authError)
    }

    @Test
    fun `init filters out Phone and MagicLink from available providers`() {
        repo.stubbedProviders = listOf(
            IdentityProvider.Google,
            IdentityProvider.Phone,
            IdentityProvider.MagicLink,
            IdentityProvider.GitHub,
        )
        val vm = ReauthViewModel(repo)

        val providers = vm.uiState.value.availableProviders
        assertTrue(providers.contains(IdentityProvider.Google))
        assertTrue(providers.contains(IdentityProvider.GitHub))
        assertTrue(!providers.contains(IdentityProvider.Phone))
        assertTrue(!providers.contains(IdentityProvider.MagicLink))
    }

    // ── Field changes ─────────────────────────────────────────────────────

    @Test
    fun `EmailChanged updates email and clears emailError`() {
        viewModel.onAction(ReauthAction.SubmitEmailPassword) // trigger error
        viewModel.onAction(ReauthAction.EmailChanged("user@test.com"))
        assertEquals("user@test.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `PasswordChanged updates password and clears passwordError`() {
        viewModel.onAction(ReauthAction.SubmitEmailPassword) // trigger error
        viewModel.onAction(ReauthAction.PasswordChanged("secret"))
        assertEquals("secret", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.passwordError)
    }

    // ── Validation ────────────────────────────────────────────────────────

    @Test
    fun `SubmitEmailPassword with blank email sets EmailEmpty error`() = runTest {
        viewModel.onAction(ReauthAction.PasswordChanged("password"))
        viewModel.onAction(ReauthAction.SubmitEmailPassword)
        assertEquals(ValidationError.EmailEmpty, viewModel.uiState.value.emailError)
    }

    @Test
    fun `SubmitEmailPassword with invalid email sets EmailInvalid error`() = runTest {
        viewModel.onAction(ReauthAction.EmailChanged("not-an-email"))
        viewModel.onAction(ReauthAction.PasswordChanged("password"))
        viewModel.onAction(ReauthAction.SubmitEmailPassword)
        assertEquals(ValidationError.EmailInvalid, viewModel.uiState.value.emailError)
    }

    @Test
    fun `SubmitEmailPassword with blank password sets PasswordEmpty error`() = runTest {
        viewModel.onAction(ReauthAction.EmailChanged("valid@email.com"))
        viewModel.onAction(ReauthAction.SubmitEmailPassword)
        assertEquals(ValidationError.PasswordEmpty, viewModel.uiState.value.passwordError)
    }

    // ── Email/password reauth ─────────────────────────────────────────────

    @Test
    fun `SubmitEmailPassword with valid credentials emits Success on success`() = runTest {
        repo.reauthenticateResult = AuthResult.Success(FakeAuthRepository.fakeSession())
        val effects = mutableListOf<ReauthEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(ReauthAction.EmailChanged("valid@email.com"))
        viewModel.onAction(ReauthAction.PasswordChanged("password"))
        viewModel.onAction(ReauthAction.SubmitEmailPassword)
        advanceUntilIdle()

        assertIs<ReauthEffect.Success>(effects.first())
        job.cancel()
    }

    @Test
    fun `SubmitEmailPassword with failure sets authError in state`() = runTest {
        repo.reauthenticateResult = AuthResult.Failure(AuthError.InvalidCredentials())

        viewModel.onAction(ReauthAction.EmailChanged("valid@email.com"))
        viewModel.onAction(ReauthAction.PasswordChanged("wrongpass"))
        viewModel.onAction(ReauthAction.SubmitEmailPassword)
        advanceUntilIdle()

        assertIs<AuthError.InvalidCredentials>(viewModel.uiState.value.authError)
    }

    @Test
    fun `SubmitEmailPassword clears isLoading after completion`() = runTest {
        repo.reauthenticateResult = AuthResult.Success(FakeAuthRepository.fakeSession())

        viewModel.onAction(ReauthAction.EmailChanged("valid@email.com"))
        viewModel.onAction(ReauthAction.PasswordChanged("password"))
        viewModel.onAction(ReauthAction.SubmitEmailPassword)
        advanceUntilIdle()

        assertTrue(!viewModel.uiState.value.isLoading)
    }

    // ── OAuth reauth ──────────────────────────────────────────────────────

    @Test
    fun `SubmitOAuth with success emits Success effect`() = runTest {
        repo.reauthenticateResult = AuthResult.Success(FakeAuthRepository.fakeSession())
        val effects = mutableListOf<ReauthEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(ReauthAction.SubmitOAuth(IdentityProvider.Google))
        advanceUntilIdle()

        assertIs<ReauthEffect.Success>(effects.first())
        job.cancel()
    }

    @Test
    fun `SubmitOAuth with failure sets authError in state`() = runTest {
        repo.reauthenticateResult = AuthResult.Failure(AuthError.UserNotFound())

        viewModel.onAction(ReauthAction.SubmitOAuth(IdentityProvider.GitHub))
        advanceUntilIdle()

        assertIs<AuthError.UserNotFound>(viewModel.uiState.value.authError)
    }

    @Test
    fun `SubmitOAuth clears loadingProvider after completion`() = runTest {
        repo.reauthenticateResult = AuthResult.Success(FakeAuthRepository.fakeSession())

        viewModel.onAction(ReauthAction.SubmitOAuth(IdentityProvider.Google))
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.loadingProvider)
    }
}
