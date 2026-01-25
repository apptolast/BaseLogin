package com.apptolast.customlogin.presentation.viewmodel

import app.cash.turbine.test
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.fakes.FakeAuthRepository
import com.apptolast.customlogin.presentation.screens.register.RegisterAction
import com.apptolast.customlogin.presentation.screens.register.RegisterEffect
import com.apptolast.customlogin.presentation.screens.register.RegisterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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

    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var viewModel: RegisterViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        viewModel = RegisterViewModel(fakeRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Initial State Tests

    @Test
    fun `initial state has empty fields and no errors`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.fullName)
            assertEquals("", state.email)
            assertEquals("", state.password)
            assertEquals("", state.confirmPassword)
            assertFalse(state.termsAccepted)
            assertNull(state.fullNameError)
            assertNull(state.emailError)
            assertNull(state.passwordError)
            assertNull(state.confirmPasswordError)
            assertFalse(state.isLoading)
        }
    }

    // endregion

    // region FullNameChanged Tests

    @Test
    fun `FullNameChanged updates fullName in state`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onAction(RegisterAction.FullNameChanged("John Doe"))

            val state = awaitItem()
            assertEquals("John Doe", state.fullName)
        }
    }

    @Test
    fun `FullNameChanged clears fullName error`() = runTest {
        viewModel.onAction(RegisterAction.SignUpClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val stateWithError = awaitItem()
            assertEquals("Full name is required", stateWithError.fullNameError)

            viewModel.onAction(RegisterAction.FullNameChanged("John Doe"))

            val clearedState = awaitItem()
            assertNull(clearedState.fullNameError)
        }
    }

    // endregion

    // region EmailChanged Tests

    @Test
    fun `EmailChanged updates email in state`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onAction(RegisterAction.EmailChanged("test@example.com"))

            val state = awaitItem()
            assertEquals("test@example.com", state.email)
        }
    }

    @Test
    fun `EmailChanged clears email error`() = runTest {
        viewModel.onAction(RegisterAction.SignUpClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val stateWithError = awaitItem()
            assertEquals("Email cannot be empty", stateWithError.emailError)

            viewModel.onAction(RegisterAction.EmailChanged("test@example.com"))

            val clearedState = awaitItem()
            assertNull(clearedState.emailError)
        }
    }

    // endregion

    // region PasswordChanged Tests

    @Test
    fun `PasswordChanged updates password in state`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onAction(RegisterAction.PasswordChanged("password123"))

            val state = awaitItem()
            assertEquals("password123", state.password)
        }
    }

    @Test
    fun `PasswordChanged clears password error`() = runTest {
        viewModel.onAction(RegisterAction.SignUpClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val stateWithError = awaitItem()
            assertEquals("Password must be at least 6 characters", stateWithError.passwordError)

            viewModel.onAction(RegisterAction.PasswordChanged("password123"))

            val clearedState = awaitItem()
            assertNull(clearedState.passwordError)
        }
    }

    // endregion

    // region ConfirmPasswordChanged Tests

    @Test
    fun `ConfirmPasswordChanged updates confirmPassword in state`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onAction(RegisterAction.ConfirmPasswordChanged("password123"))

            val state = awaitItem()
            assertEquals("password123", state.confirmPassword)
        }
    }

    @Test
    fun `ConfirmPasswordChanged clears confirmPassword error`() = runTest {
        viewModel.onAction(RegisterAction.SignUpClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val stateWithError = awaitItem()
            assertEquals("Please confirm your password", stateWithError.confirmPasswordError)

            viewModel.onAction(RegisterAction.ConfirmPasswordChanged("password123"))

            val clearedState = awaitItem()
            assertNull(clearedState.confirmPasswordError)
        }
    }

    // endregion

    // region TermsAcceptedChanged Tests

    @Test
    fun `TermsAcceptedChanged updates termsAccepted to true`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onAction(RegisterAction.TermsAcceptedChanged(true))

            val state = awaitItem()
            assertTrue(state.termsAccepted)
        }
    }

    @Test
    fun `TermsAcceptedChanged updates termsAccepted to false`() = runTest {
        viewModel.onAction(RegisterAction.TermsAcceptedChanged(true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val stateAccepted = awaitItem()
            assertTrue(stateAccepted.termsAccepted)

            viewModel.onAction(RegisterAction.TermsAcceptedChanged(false))

            val stateUnaccepted = awaitItem()
            assertFalse(stateUnaccepted.termsAccepted)
        }
    }

    // endregion

    // region Validation Tests

    @Test
    fun `SignUpClicked with empty fullName shows fullName error`() = runTest {
        viewModel.onAction(RegisterAction.SignUpClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Full name is required", state.fullNameError)
        }
    }

    @Test
    fun `SignUpClicked with empty email shows email error`() = runTest {
        viewModel.onAction(RegisterAction.FullNameChanged("John Doe"))
        viewModel.onAction(RegisterAction.SignUpClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Email cannot be empty", state.emailError)
        }
    }

    @Test
    fun `SignUpClicked with invalid email format shows format error`() = runTest {
        viewModel.onAction(RegisterAction.FullNameChanged("John Doe"))
        viewModel.onAction(RegisterAction.EmailChanged("invalid-email"))
        viewModel.onAction(RegisterAction.PasswordChanged("password123"))
        viewModel.onAction(RegisterAction.ConfirmPasswordChanged("password123"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(RegisterAction.SignUpClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Invalid email format", state.emailError)
        }
    }

    @Test
    fun `SignUpClicked with short password shows length error`() = runTest {
        viewModel.onAction(RegisterAction.FullNameChanged("John Doe"))
        viewModel.onAction(RegisterAction.EmailChanged("test@example.com"))
        viewModel.onAction(RegisterAction.PasswordChanged("12345"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(RegisterAction.SignUpClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Password must be at least 6 characters", state.passwordError)
        }
    }

    @Test
    fun `SignUpClicked with empty confirm password shows error`() = runTest {
        viewModel.onAction(RegisterAction.FullNameChanged("John Doe"))
        viewModel.onAction(RegisterAction.EmailChanged("test@example.com"))
        viewModel.onAction(RegisterAction.PasswordChanged("password123"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(RegisterAction.SignUpClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Please confirm your password", state.confirmPasswordError)
        }
    }

    @Test
    fun `SignUpClicked with mismatched passwords shows mismatch error`() = runTest {
        viewModel.onAction(RegisterAction.FullNameChanged("John Doe"))
        viewModel.onAction(RegisterAction.EmailChanged("test@example.com"))
        viewModel.onAction(RegisterAction.PasswordChanged("password123"))
        viewModel.onAction(RegisterAction.ConfirmPasswordChanged("different456"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(RegisterAction.SignUpClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Passwords do not match", state.confirmPasswordError)
        }
    }

    @Test
    fun `SignUpClicked with all empty fields shows all errors`() = runTest {
        viewModel.onAction(RegisterAction.SignUpClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Full name is required", state.fullNameError)
            assertEquals("Email cannot be empty", state.emailError)
            assertEquals("Password must be at least 6 characters", state.passwordError)
            assertEquals("Please confirm your password", state.confirmPasswordError)
        }
    }

    @Test
    fun `valid data does not produce validation errors`() = runTest {
        fillValidRegistrationData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(RegisterAction.SignUpClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.fullNameError)
            assertNull(state.emailError)
            assertNull(state.passwordError)
            assertNull(state.confirmPasswordError)
        }
    }

    // endregion

    // region Repository Interaction Tests

    @Test
    fun `SignUpClicked with valid data calls repository signUp`() = runTest {
        fillValidRegistrationData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(RegisterAction.SignUpClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeRepository.signUpCallCount)
        val signUpData = fakeRepository.signUpCalledWith!!
        assertEquals("test@example.com", signUpData.email)
        assertEquals("password123", signUpData.password)
        assertEquals("John Doe", signUpData.displayName)
    }

    @Test
    fun `SignUpClicked does not call repository when validation fails`() = runTest {
        viewModel.onAction(RegisterAction.SignUpClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, fakeRepository.signUpCallCount)
    }

    // endregion

    // region Loading State Tests

    @Test
    fun `SignUpClicked sets isLoading to true during sign up`() = runTest {
        fillValidRegistrationData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem() // Initial state with data

            viewModel.onAction(RegisterAction.SignUpClicked)

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            testDispatcher.scheduler.advanceUntilIdle()

            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
        }
    }

    // endregion

    // region Effect Tests - Success

    @Test
    fun `successful sign up emits NavigateToHome effect`() = runTest {
        fakeRepository.signUpResult = AuthResult.Success(FakeAuthRepository.createFakeUserSession())

        fillValidRegistrationData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(RegisterAction.SignUpClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<RegisterEffect.NavigateToHome>(effect)
        }
    }

    // endregion

    // region Effect Tests - Failure

    @Test
    fun `failed sign up emits ShowError effect with error message`() = runTest {
        fakeRepository.signUpResult = AuthResult.Failure(AuthError.EmailAlreadyInUse())

        fillValidRegistrationData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(RegisterAction.SignUpClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<RegisterEffect.ShowError>(effect)
            assertEquals("Email is already registered", effect.message)
        }
    }

    @Test
    fun `weak password error shows appropriate message`() = runTest {
        fakeRepository.signUpResult = AuthResult.Failure(AuthError.WeakPassword())

        fillValidRegistrationData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(RegisterAction.SignUpClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<RegisterEffect.ShowError>(effect)
            assertEquals("Password is too weak", effect.message)
        }
    }

    @Test
    fun `network error shows appropriate message`() = runTest {
        fakeRepository.signUpResult = AuthResult.Failure(AuthError.NetworkError("No internet connection"))

        fillValidRegistrationData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(RegisterAction.SignUpClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<RegisterEffect.ShowError>(effect)
            assertEquals("No internet connection", effect.message)
        }
    }

    // endregion

    // region RequiresEmailVerification Tests

    @Test
    fun `RequiresEmailVerification result emits ShowError with verification message`() = runTest {
        fakeRepository.signUpResult = AuthResult.RequiresEmailVerification

        fillValidRegistrationData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(RegisterAction.SignUpClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<RegisterEffect.ShowError>(effect)
            assertEquals("Please check your email to verify your account", effect.message)
        }
    }

    // endregion

    // region ErrorMessageDismissed Tests

    @Test
    fun `ErrorMessageDismissed does not affect state`() = runTest {
        fillValidRegistrationData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val stateBefore = awaitItem()

            viewModel.onAction(RegisterAction.ErrorMessageDismissed)
            testDispatcher.scheduler.advanceUntilIdle()

            // No state change expected
            expectNoEvents()
        }
    }

    // endregion

    // Helper method
    private fun fillValidRegistrationData() {
        viewModel.onAction(RegisterAction.FullNameChanged("John Doe"))
        viewModel.onAction(RegisterAction.EmailChanged("test@example.com"))
        viewModel.onAction(RegisterAction.PasswordChanged("password123"))
        viewModel.onAction(RegisterAction.ConfirmPasswordChanged("password123"))
    }
}
