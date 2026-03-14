package com.apptolast.customlogin.data

import com.apptolast.customlogin.domain.AuthProvider
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthRequest
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider

actual class GoogleAuthProvider : AuthProvider {
    actual override val supportedProviders: List<IdentityProvider> = listOf(IdentityProvider.Google)

    actual override suspend fun signIn(request: AuthRequest): AuthResult {
        // Actual implementation will use the Google Sign-In SDK for iOS
        return AuthResult.Failure(AuthError.OperationNotAllowed("Google Sign-In is not implemented on iOS yet."))
    }

    actual override suspend fun signOut(): AuthResult {
        // Actual implementation will sign out from the Google SDK for iOS
        return AuthResult.SignOutSuccess
    }
}
