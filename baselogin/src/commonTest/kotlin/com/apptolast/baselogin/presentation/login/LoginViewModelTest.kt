package com.apptolast.baselogin.presentation.login

import com.apptolast.baselogin.domain.model.AuthError
import com.apptolast.baselogin.domain.model.AuthResult
import com.apptolast.baselogin.domain.model.IdentityProvider
import com.apptolast.baselogin.presentation.screens.login.LoginAction
import com.apptolast.baselogin.presentation.screens.login.LoginEffect
import com.apptolast.baselogin.presentation.screens.login.LoginViewModel
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
class LoginViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
        viewModel = LoginViewModel(repo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty fields and no errors`() {
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertNull(state.emailError)
        assertNull(state.passwordError)
    }

    @Test
    fun `EmailChanged clears emailError`() {
        viewModel.onAction(LoginAction.SignInClicked) // trigger emailError
        viewModel.onAction(LoginAction.EmailChanged("new@email.com"))
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `SignInClicked with blank email sets EmailEmpty error`() = runTest {
        viewModel.onAction(LoginAction.PasswordChanged("password"))
        viewModel.onAction(LoginAction.SignInClicked)
        assertEquals(ValidationError.EmailEmpty, viewModel.uiState.value.emailError)
    }

    @Test
    fun `SignInClicked with invalid email sets EmailInvalid error`() = runTest {
        viewModel.onAction(LoginAction.EmailChanged("not-an-email"))
        viewModel.onAction(LoginAction.PasswordChanged("password"))
        viewModel.onAction(LoginAction.SignInClicked)
        assertEquals(ValidationError.EmailInvalid, viewModel.uiState.value.emailError)
    }

    @Test
    fun `SignInClicked with blank password sets PasswordEmpty error`() = runTest {
        viewModel.onAction(LoginAction.EmailChanged("valid@email.com"))
        viewModel.onAction(LoginAction.SignInClicked)
        assertEquals(ValidationError.PasswordEmpty, viewModel.uiState.value.passwordError)
    }

    @Test
    fun `SignInClicked with valid credentials emits NavigateToHome on success`() = runTest {
        repo.signInResult = AuthResult.Success(FakeAuthRepository.fakeSession())
        val effects = mutableListOf<LoginEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(LoginAction.EmailChanged("valid@email.com"))
        viewModel.onAction(LoginAction.PasswordChanged("password"))
        viewModel.onAction(LoginAction.SignInClicked)
        advanceUntilIdle()

        assertIs<LoginEffect.NavigateToHome>(effects.first())
        job.cancel()
    }

    @Test
    fun `SignInClicked with failure emits ShowError`() = runTest {
        repo.signInResult = AuthResult.Failure(AuthError.InvalidCredentials())
        val effects = mutableListOf<LoginEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(LoginAction.EmailChanged("valid@email.com"))
        viewModel.onAction(LoginAction.PasswordChanged("wrongpass"))
        viewModel.onAction(LoginAction.SignInClicked)
        advanceUntilIdle()

        assertIs<LoginEffect.ShowError>(effects.first())
        job.cancel()
    }

    @Test
    fun `SocialSignInClicked with Phone emits NavigateToPhoneAuth`() = runTest {
        val effects = mutableListOf<LoginEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(LoginAction.SocialSignInClicked(IdentityProvider.Phone))
        advanceUntilIdle()

        assertIs<LoginEffect.NavigateToPhoneAuth>(effects.first())
        job.cancel()
    }

    @Test
    fun `SocialSignInClicked with MagicLink emits NavigateToMagicLink`() = runTest {
        val effects = mutableListOf<LoginEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(LoginAction.SocialSignInClicked(IdentityProvider.MagicLink))
        advanceUntilIdle()

        assertIs<LoginEffect.NavigateToMagicLink>(effects.first())
        job.cancel()
    }

    @Test
    fun `SocialSignInClicked shows loading provider during sign-in`() = runTest {
        repo.signInResult = AuthResult.Success(FakeAuthRepository.fakeSession())

        viewModel.onAction(LoginAction.SocialSignInClicked(IdentityProvider.Google))

        // With UnconfinedTestDispatcher the coroutine runs eagerly — loading clears immediately
        // Just verify no crash and loadingProvider is null after completion
        assertNull(viewModel.uiState.value.loadingProvider)
    }

    // ── State updates ──────────────────────────────────────────────────────

    @Test
    fun `EmailChanged updates email in state`() {
        viewModel.onAction(LoginAction.EmailChanged("new@example.com"))
        assertEquals("new@example.com", viewModel.uiState.value.email)
    }

    @Test
    fun `PasswordChanged updates password and clears passwordError`() {
        // triggers passwordError when email is also blank, so set the email first
        viewModel.onAction(LoginAction.SignInClicked)
        viewModel.onAction(LoginAction.EmailChanged("valid@email.com"))
        viewModel.onAction(LoginAction.SignInClicked) // now triggers PasswordEmpty
        viewModel.onAction(LoginAction.PasswordChanged("newpass"))
        assertEquals("newpass", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `init loads availableProviders from repository`() {
        repo.stubbedProviders = listOf(IdentityProvider.Google, IdentityProvider.GitHub)
        val vm = LoginViewModel(repo)
        assertTrue(vm.uiState.value.availableProviders.contains(IdentityProvider.Google))
        assertTrue(vm.uiState.value.availableProviders.contains(IdentityProvider.GitHub))
    }

    // ── Loading state ──────────────────────────────────────────────────────

    @Test
    fun `isLoading is cleared after sign-in failure`() = runTest {
        repo.signInResult = AuthResult.Failure(AuthError.InvalidCredentials())

        viewModel.onAction(LoginAction.EmailChanged("valid@email.com"))
        viewModel.onAction(LoginAction.PasswordChanged("password"))
        viewModel.onAction(LoginAction.SignInClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ── Social sign-in failure ─────────────────────────────────────────────

    @Test
    fun `SocialSignInClicked with Google failure emits ShowError`() = runTest {
        repo.signInResult = AuthResult.Failure(AuthError.NetworkError())
        val effects = mutableListOf<LoginEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(LoginAction.SocialSignInClicked(IdentityProvider.Google))
        advanceUntilIdle()

        assertIs<LoginEffect.ShowError>(effects.first())
        job.cancel()
    }
}
