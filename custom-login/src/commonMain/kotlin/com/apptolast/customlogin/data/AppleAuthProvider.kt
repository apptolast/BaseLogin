package com.apptolast.customlogin.data

import com.apptolast.customlogin.domain.AuthProvider
import com.apptolast.customlogin.domain.model.AuthRequest
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider

expect class AppleAuthProvider() : AuthProvider {
    override val supportedProviders: List<IdentityProvider>
    override suspend fun signIn(request: AuthRequest): AuthResult
    override suspend fun signOut(): AuthResult
}
