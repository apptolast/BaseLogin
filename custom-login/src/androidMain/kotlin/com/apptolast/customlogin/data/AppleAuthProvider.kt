package com.apptolast.customlogin.data

import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthRequest
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider

actual class AppleAuthProvider {
    actual val supportedProviders: List<IdentityProvider> = listOf(IdentityProvider.Apple)

    actual suspend fun signIn(request: AuthRequest): AuthResult {
        // On Android, Apple Sign-In is typically handled via Firebase Auth, which opens a web view.
        // This can be triggered from the FirebaseAuthProvider or a dedicated web flow.
        // For now, we'll return a 'not implemented' error.
        return AuthResult.Failure(AuthError.OperationNotAllowed("Apple Sign-In is not implemented on Android yet."))
    }

    actual suspend fun signOut(): AuthResult {
        // No specific session to clear for Apple on Android in isolation.
        return AuthResult.SignOutSuccess
    }
}
