package com.apptolast.customlogin.data

import com.apptolast.customlogin.domain.AuthProvider
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthRequest
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.domain.model.PhoneAuthResult
import kotlinx.coroutines.flow.Flow

actual class PhoneAuthProvider : AuthProvider {
    actual override val supportedProviders: List<IdentityProvider> = listOf(IdentityProvider.Phone)

    actual override suspend fun signIn(request: AuthRequest): AuthResult {
        return AuthResult.Failure(AuthError.OperationNotAllowed("Use verifyPhoneNumber and signInWithVerification for phone auth."))
    }

    actual override suspend fun signOut(): AuthResult {
        return AuthResult.SignOutSuccess
    }

    actual fun verifyPhoneNumber(phoneNumber: String): Flow<PhoneAuthResult> {
        // Actual implementation will call the Firebase SDK
        TODO("Not yet implemented")
    }

    actual suspend fun signInWithVerification(verificationId: String, code: String): AuthResult {
        // Actual implementation will call the Firebase SDK
        TODO("Not yet implemented")
    }
}
