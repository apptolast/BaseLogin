package com.apptolast.customlogin.presentation.viewmodel

import app.cash.turbine.test
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.fakes.FakeAuthRepository
import com.apptolast.customlogin.presentation.screens.forgotpassword.ForgotPasswordAction
import com.apptolast.customlogin.presentation.screens.forgotpassword.ForgotPasswordEffect
import com.apptolast.customlogin.presentation.screens.forgotpassword.ForgotPasswordViewModel
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
class ForgotPasswordViewModelTest {

    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var viewModel: ForgotPasswordViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        viewModel = ForgotPasswordViewModel(fakeRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Initial State Tests

    @Test
    fun `initial state has empty email and no errors`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.email)
            assertNull(state.emailError)
            assertFalse(state.isLoading)
        }
    }

    // endregion

    // region EmailChanged Tests

    @Test
    fun `EmailChanged updates email in state`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onAction(ForgotPasswordAction.EmailChanged("test@example.com"))

            val state = awaitItem()
            assertEquals("test@example.com", state.email)
        }
    }

    @Test
    fun `EmailChanged clears email error`() = runTest {
        viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val stateWithError = awaitItem()
            assertEquals("Email cannot be empty", stateWithError.emailError)

            viewModel.onAction(ForgotPasswordAction.EmailChanged("test@example.com"))

            val clearedState = awaitItem()
            assertNull(clearedState.emailError)
        }
    }

    // endregion

    // region Validation Tests

    @Test
    fun `SendResetEmailClicked with empty email shows email error`() = runTest {
        viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Email cannot be empty", state.emailError)
        }
    }

    @Test
    fun `SendResetEmailClicked with invalid email format shows format error`() = runTest {
        viewModel.onAction(ForgotPasswordAction.EmailChanged("invalid-email"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Invalid email format", state.emailError)
        }
    }

    @Test
    fun `SendResetEmailClicked with valid email does not produce validation error`() = runTest {
        viewModel.onAction(ForgotPasswordAction.EmailChanged("test@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.emailError)
        }
    }

    @Test
    fun `valid email with plus sign passes validation`() = runTest {
        viewModel.onAction(ForgotPasswordAction.EmailChanged("test+alias@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.emailError)
        }
    }

    @Test
    fun `email with dots and hyphens passes validation`() = runTest {
        viewModel.onAction(ForgotPasswordAction.EmailChanged("user.name-test@sub.example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.emailError)
        }
    }

    // endregion

    // region Repository Interaction Tests

    @Test
    fun `SendResetEmailClicked with valid email calls repository sendPasswordResetEmail`() = runTest {
        viewModel.onAction(ForgotPasswordAction.EmailChanged("test@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeRepository.sendPasswordResetEmailCallCount)
        assertEquals("test@example.com", fakeRepository.sendPasswordResetEmailCalledWith)
    }

    @Test
    fun `SendResetEmailClicked does not call repository when validation fails`() = runTest {
        viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, fakeRepository.sendPasswordResetEmailCallCount)
    }

    // endregion

    // region Loading State Tests

    @Test
    fun `SendResetEmailClicked sets isLoading to true during request`() = runTest {
        viewModel.onAction(ForgotPasswordAction.EmailChanged("test@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem() // Initial state with email

            viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)

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
    fun `successful password reset request emits ResetEmailSent effect`() = runTest {
        fakeRepository.sendPasswordResetEmailResult = AuthResult.PasswordResetSent

        viewModel.onAction(ForgotPasswordAction.EmailChanged("test@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<ForgotPasswordEffect.ResetEmailSent>(effect)
        }
    }

    // endregion

    // region Effect Tests - Failure

    @Test
    fun `failed password reset request emits ShowError effect`() = runTest {
        fakeRepository.sendPasswordResetEmailResult = AuthResult.Failure(AuthError.UserNotFound())

        viewModel.onAction(ForgotPasswordAction.EmailChanged("test@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<ForgotPasswordEffect.ShowError>(effect)
            assertEquals("User not found", effect.message)
        }
    }

    @Test
    fun `network error shows appropriate message`() = runTest {
        fakeRepository.sendPasswordResetEmailResult =
            AuthResult.Failure(AuthError.NetworkError("Network unavailable"))

        viewModel.onAction(ForgotPasswordAction.EmailChanged("test@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<ForgotPasswordEffect.ShowError>(effect)
            assertEquals("Network unavailable", effect.message)
        }
    }

    @Test
    fun `too many requests error shows appropriate message`() = runTest {
        fakeRepository.sendPasswordResetEmailResult = AuthResult.Failure(AuthError.TooManyRequests())

        viewModel.onAction(ForgotPasswordAction.EmailChanged("test@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<ForgotPasswordEffect.ShowError>(effect)
            assertEquals("Too many requests. Please try again later", effect.message)
        }
    }

    @Test
    fun `invalid email error from repository shows appropriate message`() = runTest {
        fakeRepository.sendPasswordResetEmailResult = AuthResult.Failure(AuthError.InvalidEmail())

        viewModel.onAction(ForgotPasswordAction.EmailChanged("test@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(ForgotPasswordAction.SendResetEmailClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<ForgotPasswordEffect.ShowError>(effect)
            assertEquals("Invalid email format", effect.message)
        }
    }

    // endregion
}
