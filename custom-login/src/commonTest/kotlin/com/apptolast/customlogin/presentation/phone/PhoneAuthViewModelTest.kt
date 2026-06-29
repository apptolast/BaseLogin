package com.apptolast.customlogin.presentation.phone

import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.PhoneAuthResult
import com.apptolast.customlogin.di.LoginLibraryConfig
import com.apptolast.customlogin.di.PhoneAuthConfig
import com.apptolast.customlogin.presentation.screens.phone.PhoneAuthAction
import com.apptolast.customlogin.presentation.screens.phone.PhoneAuthEffect
import com.apptolast.customlogin.presentation.screens.phone.PhoneAuthViewModel
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class PhoneAuthViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: PhoneAuthViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
        viewModel = PhoneAuthViewModel(repo, LoginLibraryConfig())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `SendCodeClicked with blank phone sets PhoneEmpty error`() = runTest {
        viewModel.onAction(PhoneAuthAction.SendCodeClicked)
        assertEquals(ValidationError.PhoneEmpty, viewModel.uiState.value.phoneError)
    }

    @Test
    fun `init uses configured default country code`() {
        val configuredViewModel = PhoneAuthViewModel(
            repo,
            LoginLibraryConfig(phoneAuthConfig = PhoneAuthConfig(defaultCountryCode = "+34"))
        )

        assertEquals("+34", configuredViewModel.uiState.value.countryCode)
    }

    @Test
    fun `SendCodeClicked with valid phone and CodeSent transitions to OTP step`() = runTest {
        repo.sendPhoneOtpResult = PhoneAuthResult.CodeSent("verification-id-123")

        viewModel.onAction(PhoneAuthAction.PhoneNumberChanged("6123456789"))
        viewModel.onAction(PhoneAuthAction.SendCodeClicked)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.verificationId)
        assertEquals("verification-id-123", viewModel.uiState.value.verificationId)
    }

    @Test
    fun `SendCodeClicked with AutoSignedIn emits NavigateToHome`() = runTest {
        repo.sendPhoneOtpResult = PhoneAuthResult.AutoSignedIn(FakeAuthRepository.fakeSession())
        val effects = mutableListOf<PhoneAuthEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(PhoneAuthAction.PhoneNumberChanged("6123456789"))
        viewModel.onAction(PhoneAuthAction.SendCodeClicked)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.verificationId)
        assertIs<PhoneAuthEffect.NavigateToHome>(effects.first())
        job.cancel()
    }

    @Test
    fun `SendCodeClicked on failure emits ShowError`() = runTest {
        repo.sendPhoneOtpResult = PhoneAuthResult.Failure(AuthError.TooManyRequests())
        val effects = mutableListOf<PhoneAuthEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(PhoneAuthAction.PhoneNumberChanged("6123456789"))
        viewModel.onAction(PhoneAuthAction.SendCodeClicked)
        advanceUntilIdle()

        assertIs<PhoneAuthEffect.ShowError>(effects.first())
        job.cancel()
    }

    @Test
    fun `VerifyCodeClicked with blank OTP sets OtpEmpty error`() = runTest {
        // Set verificationId to simulate being in OTP step
        repo.sendPhoneOtpResult = PhoneAuthResult.CodeSent("vid")
        viewModel.onAction(PhoneAuthAction.PhoneNumberChanged("6123456789"))
        viewModel.onAction(PhoneAuthAction.SendCodeClicked)
        advanceUntilIdle()

        viewModel.onAction(PhoneAuthAction.VerifyCodeClicked)
        assertEquals(ValidationError.OtpEmpty, viewModel.uiState.value.otpError)
    }

    @Test
    fun `VerifyCodeClicked with valid OTP on success emits NavigateToHome`() = runTest {
        repo.sendPhoneOtpResult = PhoneAuthResult.CodeSent("vid")
        repo.verifyPhoneOtpResult = AuthResult.Success(FakeAuthRepository.fakeSession())
        val effects = mutableListOf<PhoneAuthEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(PhoneAuthAction.PhoneNumberChanged("6123456789"))
        viewModel.onAction(PhoneAuthAction.SendCodeClicked)
        viewModel.onAction(PhoneAuthAction.OtpCodeChanged("123456"))
        viewModel.onAction(PhoneAuthAction.VerifyCodeClicked)
        advanceUntilIdle()

        assertIs<PhoneAuthEffect.NavigateToHome>(effects.first())
        job.cancel()
    }

    @Test
    fun `VerifyCodeClicked on failure emits ShowError`() = runTest {
        repo.sendPhoneOtpResult = PhoneAuthResult.CodeSent("vid")
        repo.verifyPhoneOtpResult = AuthResult.Failure(AuthError.InvalidCredentials())
        val effects = mutableListOf<PhoneAuthEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }

        viewModel.onAction(PhoneAuthAction.PhoneNumberChanged("6123456789"))
        viewModel.onAction(PhoneAuthAction.SendCodeClicked)
        viewModel.onAction(PhoneAuthAction.OtpCodeChanged("000000"))
        viewModel.onAction(PhoneAuthAction.VerifyCodeClicked)
        advanceUntilIdle()

        assertIs<PhoneAuthEffect.ShowError>(effects.first())
        job.cancel()
    }

    @Test
    fun `CountryCodeChanged updates countryCode and clears phoneError`() = runTest {
        viewModel.onAction(PhoneAuthAction.SendCodeClicked) // trigger phoneError
        viewModel.onAction(PhoneAuthAction.CountryCodeChanged("+34"))
        assertEquals("+34", viewModel.uiState.value.countryCode)
        assertNull(viewModel.uiState.value.phoneError)
    }

    @Test
    fun `OtpCodeChanged clears otpError`() = runTest {
        repo.sendPhoneOtpResult = PhoneAuthResult.CodeSent("vid")
        viewModel.onAction(PhoneAuthAction.PhoneNumberChanged("6123456789"))
        viewModel.onAction(PhoneAuthAction.SendCodeClicked)
        viewModel.onAction(PhoneAuthAction.VerifyCodeClicked) // trigger OtpEmpty
        viewModel.onAction(PhoneAuthAction.OtpCodeChanged("1"))
        assertNull(viewModel.uiState.value.otpError)
    }

    // ── State updates ──────────────────────────────────────────────────────

    @Test
    fun `PhoneNumberChanged updates phoneNumber in state`() {
        viewModel.onAction(PhoneAuthAction.PhoneNumberChanged("612345678"))
        assertEquals("612345678", viewModel.uiState.value.phoneNumber)
    }

    @Test
    fun `isLoading is cleared after SendCode failure`() = runTest {
        repo.sendPhoneOtpResult = PhoneAuthResult.Failure(AuthError.PhoneNumberInvalid())

        viewModel.onAction(PhoneAuthAction.PhoneNumberChanged("000"))
        viewModel.onAction(PhoneAuthAction.SendCodeClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `isLoading is cleared after VerifyCode failure`() = runTest {
        repo.sendPhoneOtpResult = PhoneAuthResult.CodeSent("vid")
        repo.verifyPhoneOtpResult = AuthResult.Failure(AuthError.InvalidVerificationCode())

        viewModel.onAction(PhoneAuthAction.PhoneNumberChanged("6123456789"))
        viewModel.onAction(PhoneAuthAction.SendCodeClicked)
        viewModel.onAction(PhoneAuthAction.OtpCodeChanged("000000"))
        viewModel.onAction(PhoneAuthAction.VerifyCodeClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }
}
