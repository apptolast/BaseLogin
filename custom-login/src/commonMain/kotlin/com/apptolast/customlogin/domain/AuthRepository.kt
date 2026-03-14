package com.apptolast.customlogin.domain

import com.apptolast.customlogin.domain.model.AuthRequest
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.AuthState
import com.apptolast.customlogin.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signIn(request: AuthRequest): AuthResult
    suspend fun signUp(request: AuthRequest): AuthResult
    suspend fun signOut(): AuthResult
    suspend fun sendPasswordResetEmail(email: String): AuthResult
    suspend fun confirmPasswordReset(code: String, newPassword: String): AuthResult
    fun getCurrentSession(): UserSession?
    fun observeAuthState(): Flow<AuthState>
}