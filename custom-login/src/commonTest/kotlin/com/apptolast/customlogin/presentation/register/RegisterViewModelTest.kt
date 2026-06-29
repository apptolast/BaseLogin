package com.apptolast.customlogin.presentation.register

import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.di.LoginLibraryConfig
import com.apptolast.customlogin.di.PasswordPolicyConfig
import com.apptolast.customlogin.presentation.screens.register.RegisterAction
import com.apptolast.customlogin.presentation.screens.register.RegisterEffect
import com.apptolast.customlogin.presentation.screens.register.RegisterViewModel
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
class RegisterViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: RegisterViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
        viewModel = RegisterViewModel(repo, LoginLibraryConfig())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fillValidForm() {
        viewModel.onAction(RegisterAction.FullNameChanged("John Doe"))
        viewModel.onAction(RegisterAction.EmailChanged("john@example.com"))
        viewModel.onAction(RegisterAction.PasswordChanged("secret123"))
        viewModel.onAction(RegisterAction.ConfirmPasswordChanged("secret123"))
        viewModel.onAction(RegisterAction.TermsAcceptedChanged(true))
    }

    @Test
    fun `SignUpClicked with blank name sets NameRequired error`() = runTest {
        viewModel.onAction(RegisterAction.EmailChanged("a@b.com"))
        viewModel.onAction(RegisterAction.PasswordChanged("secret123"))
        viewModel.onAction(RegisterAction.ConfirmPasswordChanged("secret123"))
        viewModel.onAction(RegisterAction.SignUpClicked)
        assertEquals(ValidationError.NameRequired, viewModel.uiState.value.fullNameError)
    }

    @Test
    fun `SignUpClicked with blank email sets EmailEmpty error`() = runTest {
        viewModel.onAction(RegisterAction.FullNameChanged("John"))
        viewModel.onAction(RegisterAction.PasswordChanged("secret123"))
        viewModel.onAction(RegisterAction.ConfirmPasswordChanged("secret123"))
        viewModel.onAction(RegisterAction.SignUpClicked)
        assertEquals(ValidationError.EmailEmpty, viewModel.uiState.value.emailError)
    }

    @Test
    fun `SignUpClicked with invalid email sets EmailInvalid error`() = runTest {
        viewModel.onAction(RegisterAction.FullNameChanged("John"))
        viewModel.onAction(RegisterAction.EmailChanged("bad-email"))
        viewModel.onAction(RegisterAction.PasswordChanged("secret123"))
        viewModel.onAction(RegisterAction.ConfirmPasswordChanged("secret123"))
        viewModel.onAction(RegisterAction.SignUpClicked)
        assertEquals(ValidationError.EmailInvalid, viewModel.uiState.value.emailError)
    }

    @Test
    fun `SignUpClicked with short password sets PasswordTooShort error`() = runTest {
        viewModel.onAction(RegisterAction.FullNameChanged("John"))
        viewModel.onAction(RegisterAction.EmailChanged("a@b.com"))
        viewModel.onAction(RegisterAction.PasswordChanged("123"))
        viewModel.onAction(RegisterAction.ConfirmPasswordChanged("123"))
        viewModel.onAction(RegisterAction.SignUpClicked)
        assertEquals(ValidationError.PasswordTooShort, viewModel.uiState.value.passwordError)
    }

    @Test
    fun `SignUpClicked uses configured password minimum length`() = runTest {
        viewModel = RegisterViewModel(
            repo,
            LoginLibraryConfig(passwordPolicy = PasswordPolicyConfig(minLength = 10))
        )

        viewModel.onAction(RegisterAction.FullNameChanged("John"))
        viewModel.onAction(RegisterAction.EmailChanged("a@b.com"))
        viewModel.onAction(RegisterAction.PasswordChanged("secret12"))
        viewModel.onAction(RegisterAction.ConfirmPasswordChanged("secret12"))
        viewModel.onAction(RegisterAction.SignUpClicked)

        assertEquals(ValidationError.PasswordTooShort, viewModel.uiState.value.passwordError)
    }

    @Test
    fun `SignUpClicked with mismatched passwords sets PasswordsDoNotMatch error`() = runTest {
        viewModel.onAction(RegisterAction.FullNameChanged("John"))
        viewModel.onAction(RegisterAction.EmailChanged("a@b.com"))
        viewModel.onAction(RegisterAction.PasswordChanged("secret123"))
        viewModel.onAction(RegisterAction.ConfirmPasswordChanged("different"))
        viewModel.onAction(RegisterAction.SignUpClicked)
        assertEquals(ValidationError.PasswordsDoNotMatch, viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun `SignUpClicked with valid form and success emits NavigateToHome`() = runTest {
        repo.signUpResult = AuthResult.Success(FakeAuthRepository.fakeSession())
        val effects = mutableListOf<RegisterEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        fillValidForm()
        viewModel.onAction(RegisterAction.SignUpClicked)
        advanceUntilIdle()

        assertIs<RegisterEffect.NavigateToHome>(effects.first())
        job.cancel()
    }

    @Test
    fun `SignUpClicked on failure emits ShowError`() = runTest {
        repo.signUpResult = AuthResult.Failure(AuthError.EmailAlreadyInUse())
        val effects = mutableListOf<RegisterEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        fillValidForm()
        viewModel.onAction(RegisterAction.SignUpClicked)
        advanceUntilIdle()

        assertIs<RegisterEffect.ShowError>(effects.first())
        job.cancel()
    }

    @Test
    fun `FullNameChanged clears fullNameError`() = runTest {
        viewModel.onAction(RegisterAction.SignUpClicked) // triggers errors
        viewModel.onAction(RegisterAction.FullNameChanged("New Name"))
        assertNull(viewModel.uiState.value.fullNameError)
    }

    @Test
    fun `PasswordChanged clears passwordError`() = runTest {
        viewModel.onAction(RegisterAction.SignUpClicked)
        viewModel.onAction(RegisterAction.PasswordChanged("newpass123"))
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `ConfirmPasswordChanged clears confirmPasswordError`() = runTest {
        viewModel.onAction(RegisterAction.SignUpClicked)
        viewModel.onAction(RegisterAction.ConfirmPasswordChanged("confirm"))
        assertNull(viewModel.uiState.value.confirmPasswordError)
    }

    // ── Additional validation ──────────────────────────────────────────────

    @Test
    fun `SignUpClicked with blank confirmPassword sets ConfirmPasswordEmpty error`() = runTest {
        viewModel.onAction(RegisterAction.FullNameChanged("John"))
        viewModel.onAction(RegisterAction.EmailChanged("a@b.com"))
        viewModel.onAction(RegisterAction.PasswordChanged("secret123"))
        // confirmPassword intentionally left blank
        viewModel.onAction(RegisterAction.SignUpClicked)
        assertEquals(ValidationError.ConfirmPasswordEmpty, viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun `SignUpClicked with RequiresEmailVerification result emits ShowError`() = runTest {
        repo.signUpResult = AuthResult.RequiresEmailVerification
        val effects = mutableListOf<RegisterEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        fillValidForm()
        viewModel.onAction(RegisterAction.SignUpClicked)
        advanceUntilIdle()

        assertIs<RegisterEffect.ShowError>(effects.first())
        job.cancel()
    }

    // ── State updates ──────────────────────────────────────────────────────

    @Test
    fun `TermsAcceptedChanged updates termsAccepted in state`() {
        viewModel.onAction(RegisterAction.TermsAcceptedChanged(true))
        assertTrue(viewModel.uiState.value.termsAccepted)
        viewModel.onAction(RegisterAction.TermsAcceptedChanged(false))
        assertFalse(viewModel.uiState.value.termsAccepted)
    }

    @Test
    fun `isLoading is cleared after sign-up failure`() = runTest {
        repo.signUpResult = AuthResult.Failure(AuthError.EmailAlreadyInUse())

        fillValidForm()
        viewModel.onAction(RegisterAction.SignUpClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ── Social sign-up navigation ──────────────────────────────────────────

    @Test
    fun `SignUpWithOAuth with Phone emits NavigateToPhoneAuth`() = runTest {
        val effects = mutableListOf<RegisterEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(RegisterAction.SignUpWithOAuth(IdentityProvider.Phone))
        advanceUntilIdle()

        assertIs<RegisterEffect.NavigateToPhoneAuth>(effects.first())
        job.cancel()
    }

    @Test
    fun `SignUpWithOAuth with MagicLink emits NavigateToMagicLink`() = runTest {
        val effects = mutableListOf<RegisterEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(RegisterAction.SignUpWithOAuth(IdentityProvider.MagicLink))
        advanceUntilIdle()

        assertIs<RegisterEffect.NavigateToMagicLink>(effects.first())
        job.cancel()
    }
}
