package com.apptolast.customlogin.domain

import com.apptolast.customlogin.domain.model.AuthRequest
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider

interface AuthProvider {
    val supportedProviders: List<IdentityProvider>
    suspend fun signIn(request: AuthRequest): AuthResult
    suspend fun signOut(): AuthResult
}
