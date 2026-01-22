package com.apptolast.login.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.customlogin.domain.model.UserSession
import com.apptolast.customlogin.domain.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing splash screen state and authentication check.
 * Used to determine the initial navigation destination based on auth state.
 */
class SplashViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _splashState = MutableStateFlow<SplashState>(SplashState.Loading)
    val splashState: StateFlow<SplashState> = _splashState.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                val session = authRepository.getCurrentSession()
                val isSignedIn = authRepository.isSignedIn()

                _splashState.value = if (isSignedIn && session != null) {
                    SplashState.Authenticated(session)
                } else {
                    SplashState.Unauthenticated
                }
            } catch (e: Exception) {
                _splashState.value = SplashState.Unauthenticated
            } finally {
                _isReady.value = true
            }
        }
    }
}

/**
 * Represents the splash screen state.
 */
sealed interface SplashState {
    data object Loading : SplashState
    data object Unauthenticated : SplashState
    data class Authenticated(val session: UserSession) : SplashState
}
