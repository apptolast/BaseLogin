package com.apptolast.customlogin.presentation.viewmodel

import app.cash.turbine.test
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.fakes.FakeAuthRepository
import com.apptolast.customlogin.presentation.screens.resetpassword.ResetPasswordAction
import com.apptolast.customlogin.presentation.screens.resetpassword.ResetPasswordEffect
import com.apptolast.customlogin.presentation.screens.resetpassword.ResetPasswordViewModel
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
class ResetPasswordViewModelTest {

    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var viewModel: ResetPasswordViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        viewModel = ResetPasswordViewModel(fakeRepository)
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
            assertEquals("", state.resetCode)
            assertEquals("", state.newPassword)
            assertEquals("", state.confirmPassword)
            assertNull(state.passwordError)
            assertNull(state.confirmPasswordError)
            assertFalse(state.isLoading)
        }
    }

    // endregion

    // region setResetCode Tests

    @Test
    fun `setResetCode updates resetCode in state`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.setResetCode("ABC123")

            val state = awaitItem()
            assertEquals("ABC123", state.resetCode)
        }
    }

    // endregion

    // region NewPasswordChanged Tests

    @Test
    fun `NewPasswordChanged updates newPassword in state`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onAction(ResetPasswordAction.NewPasswordChanged("newpassword123"))

            val state = awaitItem()
            assertEquals("newpassword123", state.newPassword)
        }
    }

    @Test
    fun `NewPasswordChanged clears password error`() = runTest {
        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val stateWithError = awaitItem()
            assertEquals("Password cannot be empty", stateWithError.passwordError)

            viewModel.onAction(ResetPasswordAction.NewPasswordChanged("newpassword123"))

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

            viewModel.onAction(ResetPasswordAction.ConfirmPasswordChanged("newpassword123"))

            val state = awaitItem()
            assertEquals("newpassword123", state.confirmPassword)
        }
    }

    @Test
    fun `ConfirmPasswordChanged clears confirmPassword error`() = runTest {
        viewModel.onAction(ResetPasswordAction.NewPasswordChanged("password123"))
        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val stateWithError = awaitItem()
            assertEquals("Please confirm your password", stateWithError.confirmPasswordError)

            viewModel.onAction(ResetPasswordAction.ConfirmPasswordChanged("password123"))

            val clearedState = awaitItem()
            assertNull(clearedState.confirmPasswordError)
        }
    }

    // endregion

    // region Validation Tests

    @Test
    fun `ResetPasswordClicked with empty password shows password error`() = runTest {
        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Password cannot be empty", state.passwordError)
        }
    }

    @Test
    fun `ResetPasswordClicked with short password shows length error`() = runTest {
        viewModel.onAction(ResetPasswordAction.NewPasswordChanged("12345"))
        viewModel.onAction(ResetPasswordAction.ConfirmPasswordChanged("12345"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Password must be at least 6 characters", state.passwordError)
        }
    }

    @Test
    fun `ResetPasswordClicked with empty confirm password shows error`() = runTest {
        viewModel.onAction(ResetPasswordAction.NewPasswordChanged("password123"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Please confirm your password", state.confirmPasswordError)
        }
    }

    @Test
    fun `ResetPasswordClicked with mismatched passwords shows mismatch error`() = runTest {
        viewModel.onAction(ResetPasswordAction.NewPasswordChanged("password123"))
        viewModel.onAction(ResetPasswordAction.ConfirmPasswordChanged("different456"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Passwords do not match", state.confirmPasswordError)
        }
    }

    @Test
    fun `ResetPasswordClicked with both fields empty shows both errors`() = runTest {
        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Password cannot be empty", state.passwordError)
            assertEquals("Please confirm your password", state.confirmPasswordError)
        }
    }

    @Test
    fun `valid passwords do not produce validation errors`() = runTest {
        fillValidResetData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.passwordError)
            assertNull(state.confirmPasswordError)
        }
    }

    @Test
    fun `password with exactly 6 characters passes validation`() = runTest {
        viewModel.setResetCode("ABC123")
        viewModel.onAction(ResetPasswordAction.NewPasswordChanged("123456"))
        viewModel.onAction(ResetPasswordAction.ConfirmPasswordChanged("123456"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.passwordError)
        }
    }

    // endregion

    // region Repository Interaction Tests

    @Test
    fun `ResetPasswordClicked with valid data calls repository confirmPasswordReset`() = runTest {
        fillValidResetData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeRepository.confirmPasswordResetCallCount)
        val passwordResetData = fakeRepository.confirmPasswordResetCalledWith!!
        assertEquals("RESET-CODE-123", passwordResetData.code)
        assertEquals("newpassword123", passwordResetData.newPassword)
    }

    @Test
    fun `ResetPasswordClicked does not call repository when validation fails`() = runTest {
        viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, fakeRepository.confirmPasswordResetCallCount)
    }

    // endregion

    // region Loading State Tests

    @Test
    fun `ResetPasswordClicked sets isLoading to true during request`() = runTest {
        fillValidResetData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem() // Initial state with data

            viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)

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
    fun `successful password reset emits NavigateToLogin effect`() = runTest {
        fakeRepository.confirmPasswordResetResult = AuthResult.PasswordResetSuccess

        fillValidResetData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<ResetPasswordEffect.NavigateToLogin>(effect)
        }
    }

    // endregion

    // region Effect Tests - Failure

    @Test
    fun `failed password reset emits ShowError effect`() = runTest {
        fakeRepository.confirmPasswordResetResult = AuthResult.Failure(AuthError.InvalidResetCode())

        fillValidResetData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<ResetPasswordEffect.ShowError>(effect)
            assertEquals("Invalid or expired password reset code", effect.message)
        }
    }

    @Test
    fun `network error shows appropriate message`() = runTest {
        fakeRepository.confirmPasswordResetResult =
            AuthResult.Failure(AuthError.NetworkError("Connection lost"))

        fillValidResetData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<ResetPasswordEffect.ShowError>(effect)
            assertEquals("Connection lost", effect.message)
        }
    }

    @Test
    fun `weak password error from repository shows appropriate message`() = runTest {
        fakeRepository.confirmPasswordResetResult = AuthResult.Failure(AuthError.WeakPassword())

        fillValidResetData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<ResetPasswordEffect.ShowError>(effect)
            assertEquals("Password is too weak", effect.message)
        }
    }

    @Test
    fun `expired reset code error shows appropriate message`() = runTest {
        fakeRepository.confirmPasswordResetResult =
            AuthResult.Failure(AuthError.InvalidResetCode("Reset code has expired"))

        fillValidResetData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(ResetPasswordAction.ResetPasswordClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<ResetPasswordEffect.ShowError>(effect)
            assertEquals("Reset code has expired", effect.message)
        }
    }

    // endregion

    // Helper method
    private fun fillValidResetData() {
        viewModel.setResetCode("RESET-CODE-123")
        viewModel.onAction(ResetPasswordAction.NewPasswordChanged("newpassword123"))
        viewModel.onAction(ResetPasswordAction.ConfirmPasswordChanged("newpassword123"))
    }
}
