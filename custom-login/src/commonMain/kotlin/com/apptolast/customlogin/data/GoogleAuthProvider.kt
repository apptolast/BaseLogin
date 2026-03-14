package com.apptolast.customlogin.data

import com.apptolast.customlogin.domain.AuthProvider
import com.apptolast.customlogin.domain.model.AuthRequest
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider

/**
 * `expect` class for handling Google Sign-In.
 * Implements the [AuthProvider] interface.
 * Platform-specific implementations will provide the concrete logic for interacting with Google's SDK.
 */
expect class GoogleAuthProvider() : AuthProvider {
    override val supportedProviders: List<IdentityProvider>
    override suspend fun signIn(request: AuthRequest): AuthResult
    override suspend fun signOut(): AuthResult
}
