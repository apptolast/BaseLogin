package com.apptolast.customlogin.data

import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthRequest
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider

actual class AppleAuthProvider {
    actual val supportedProviders: List<IdentityProvider> = listOf(IdentityProvider.Apple)

    actual suspend fun signIn(request: AuthRequest): AuthResult {
        // This is where the native iOS Sign in with Apple logic will be implemented.
        // For now, we'll return a 'not implemented' error.
        return AuthResult.Failure(AuthError.OperationNotAllowed("Apple Sign-In is not implemented on iOS yet."))
    }

    actual suspend fun signOut(): AuthResult {
        // This would clear any local state related to the Apple session.
        return AuthResult.SignOutSuccess
    }
}
