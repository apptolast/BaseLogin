package com.apptolast.customlogin.presentation.viewmodel

import app.cash.turbine.test
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.Credentials
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.fakes.FakeAuthRepository
import com.apptolast.customlogin.presentation.screens.login.LoginAction
import com.apptolast.customlogin.presentation.screens.login.LoginEffect
import com.apptolast.customlogin.presentation.screens.login.LoginViewModel
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
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        viewModel = LoginViewModel(fakeRepository)
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
            assertEquals("", state.email)
            assertEquals("", state.password)
            assertNull(state.emailError)
            assertNull(state.passwordError)
            assertNull(state.loadingProvider)
        }
    }

    // endregion

    // region EmailChanged Tests

    @Test
    fun `EmailChanged updates email in state`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onAction(LoginAction.EmailChanged("test@example.com"))

            val state = awaitItem()
            assertEquals("test@example.com", state.email)
        }
    }

    @Test
    fun `EmailChanged clears email error`() = runTest {
        // First trigger an error by attempting sign in with empty email
        viewModel.onAction(LoginAction.SignInClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val stateWithError = awaitItem()
            assertEquals("Email cannot be empty", stateWithError.emailError)

            viewModel.onAction(LoginAction.EmailChanged("test@example.com"))

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

            viewModel.onAction(LoginAction.PasswordChanged("password123"))

            val state = awaitItem()
            assertEquals("password123", state.password)
        }
    }

    @Test
    fun `PasswordChanged clears password error`() = runTest {
        // Trigger an error with invalid email format but no password
        viewModel.onAction(LoginAction.EmailChanged("valid@email.com"))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onAction(LoginAction.SignInClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val stateWithError = awaitItem()
            assertEquals("Password cannot be empty", stateWithError.passwordError)

            viewModel.onAction(LoginAction.PasswordChanged("newpassword"))

            val clearedState = awaitItem()
            assertNull(clearedState.passwordError)
        }
    }

    // endregion

    // region Validation Tests

    @Test
    fun `SignInClicked with empty email shows email error`() = runTest {
        viewModel.onAction(LoginAction.SignInClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Email cannot be empty", state.emailError)
        }
    }

    @Test
    fun `SignInClicked with invalid email format shows format error`() = runTest {
        viewModel.onAction(LoginAction.EmailChanged("invalid-email"))
        viewModel.onAction(LoginAction.PasswordChanged("password123"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(LoginAction.SignInClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Invalid email format", state.emailError)
        }
    }

    @Test
    fun `SignInClicked with empty password shows password error`() = runTest {
        viewModel.onAction(LoginAction.EmailChanged("valid@email.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(LoginAction.SignInClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Password cannot be empty", state.passwordError)
        }
    }

    @Test
    fun `SignInClicked with both fields empty shows both errors`() = runTest {
        viewModel.onAction(LoginAction.SignInClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Email cannot be empty", state.emailError)
            assertEquals("Password cannot be empty", state.passwordError)
        }
    }

    @Test
    fun `valid credentials do not produce validation errors`() = runTest {
        viewModel.onAction(LoginAction.EmailChanged("valid@email.com"))
        viewModel.onAction(LoginAction.PasswordChanged("password123"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(LoginAction.SignInClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.emailError)
            assertNull(state.passwordError)
        }
    }

    // endregion

    // region Repository Interaction Tests

    @Test
    fun `SignInClicked with valid credentials calls repository signIn`() = runTest {
        viewModel.onAction(LoginAction.EmailChanged("test@example.com"))
        viewModel.onAction(LoginAction.PasswordChanged("password123"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(LoginAction.SignInClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeRepository.signInCallCount)
        assertIs<Credentials.EmailPassword>(fakeRepository.signInCalledWith)
        val credentials = fakeRepository.signInCalledWith as Credentials.EmailPassword
        assertEquals("test@example.com", credentials.email)
        assertEquals("password123", credentials.password)
    }

    @Test
    fun `SignInClicked does not call repository when validation fails`() = runTest {
        viewModel.onAction(LoginAction.SignInClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, fakeRepository.signInCallCount)
    }

    // endregion

    // region Loading State Tests

    @Test
    fun `SignInClicked sets loading provider to email during sign in`() = runTest {
        viewModel.onAction(LoginAction.EmailChanged("test@example.com"))
        viewModel.onAction(LoginAction.PasswordChanged("password123"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem() // Initial state with credentials

            viewModel.onAction(LoginAction.SignInClicked)

            val loadingState = awaitItem()
            assertEquals("email", loadingState.loadingProvider)

            testDispatcher.scheduler.advanceUntilIdle()

            val finalState = awaitItem()
            assertNull(finalState.loadingProvider)
        }
    }

    @Test
    fun `SocialSignInClicked sets loading provider to provider id`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onAction(LoginAction.SocialSignInClicked(IdentityProvider.Google))

            val loadingState = awaitItem()
            assertEquals("google.com", loadingState.loadingProvider)

            testDispatcher.scheduler.advanceUntilIdle()

            val finalState = awaitItem()
            assertNull(finalState.loadingProvider)
        }
    }

    // endregion

    // region Effect Tests - Success

    @Test
    fun `successful sign in emits NavigateToHome effect`() = runTest {
        fakeRepository.signInResult = AuthResult.Success(FakeAuthRepository.createFakeUserSession())

        viewModel.onAction(LoginAction.EmailChanged("test@example.com"))
        viewModel.onAction(LoginAction.PasswordChanged("password123"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(LoginAction.SignInClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<LoginEffect.NavigateToHome>(effect)
        }
    }

    @Test
    fun `successful social sign in emits NavigateToHome effect`() = runTest {
        fakeRepository.signInResult = AuthResult.Success(FakeAuthRepository.createFakeUserSession())

        viewModel.effect.test {
            viewModel.onAction(LoginAction.SocialSignInClicked(IdentityProvider.Google))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<LoginEffect.NavigateToHome>(effect)
        }
    }

    // endregion

    // region Effect Tests - Failure

    @Test
    fun `failed sign in emits ShowError effect with error message`() = runTest {
        fakeRepository.signInResult = AuthResult.Failure(AuthError.InvalidCredentials())

        viewModel.onAction(LoginAction.EmailChanged("test@example.com"))
        viewModel.onAction(LoginAction.PasswordChanged("password123"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(LoginAction.SignInClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<LoginEffect.ShowError>(effect)
            assertEquals("Invalid email or password", effect.message)
        }
    }

    @Test
    fun `failed social sign in emits ShowError effect`() = runTest {
        fakeRepository.signInResult = AuthResult.Failure(AuthError.NetworkError())

        viewModel.effect.test {
            viewModel.onAction(LoginAction.SocialSignInClicked(IdentityProvider.Apple))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<LoginEffect.ShowError>(effect)
        }
    }

    @Test
    fun `network error shows appropriate message`() = runTest {
        fakeRepository.signInResult = AuthResult.Failure(AuthError.NetworkError("Connection failed"))

        viewModel.onAction(LoginAction.EmailChanged("test@example.com"))
        viewModel.onAction(LoginAction.PasswordChanged("password123"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(LoginAction.SignInClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<LoginEffect.ShowError>(effect)
            assertEquals("Connection failed", effect.message)
        }
    }

    @Test
    fun `user not found error shows appropriate message`() = runTest {
        fakeRepository.signInResult = AuthResult.Failure(AuthError.UserNotFound())

        viewModel.onAction(LoginAction.EmailChanged("test@example.com"))
        viewModel.onAction(LoginAction.PasswordChanged("password123"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(LoginAction.SignInClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<LoginEffect.ShowError>(effect)
            assertEquals("User not found", effect.message)
        }
    }

    // endregion

    // region Social Sign-In Tests

    @Test
    fun `SocialSignInClicked with Google calls repository with correct provider`() = runTest {
        viewModel.onAction(LoginAction.SocialSignInClicked(IdentityProvider.Google))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeRepository.signInCallCount)
        assertIs<Credentials.OAuthToken>(fakeRepository.signInCalledWith)
        val credentials = fakeRepository.signInCalledWith as Credentials.OAuthToken
        assertEquals(IdentityProvider.Google, credentials.provider)
    }

    @Test
    fun `SocialSignInClicked with Apple calls repository with correct provider`() = runTest {
        viewModel.onAction(LoginAction.SocialSignInClicked(IdentityProvider.Apple))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeRepository.signInCallCount)
        assertIs<Credentials.OAuthToken>(fakeRepository.signInCalledWith)
        val credentials = fakeRepository.signInCalledWith as Credentials.OAuthToken
        assertEquals(IdentityProvider.Apple, credentials.provider)
    }

    // endregion
}
