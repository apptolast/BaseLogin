package com.apptolast.login.home.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.customlogin.domain.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Profile screen using MVI pattern.
 * It fetches the current user session and handles user actions.
 */
class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ProfileEffect>()
    val effect = _effect.asSharedFlow()

    init {
        loadCurrentUser()
    }

    /**
     * The single entry point for all user actions from the UI.
     */
    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.SignOutClicked -> onSignOutClicked()
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val session = authRepository.getCurrentSession()
            _uiState.update { it.copy(isLoading = false, userSession = session) }
        }
    }

    /**
     * Calls the repository to sign out. The app's root UI will react to the
     * subsequent change in the global authentication state.
     */
    private fun onSignOutClicked() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
