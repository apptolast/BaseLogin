package com.apptolast.customlogin.data

import com.apptolast.customlogin.domain.AuthProvider
import com.apptolast.customlogin.domain.model.AuthRequest
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.domain.model.PhoneAuthResult
import kotlinx.coroutines.flow.Flow

/**
 * `expect` class for handling phone number authentication.
 * This class implements the [AuthProvider] interface and adds specific methods for phone verification.
 * Platform-specific implementations (`actual`) will provide the concrete logic.
 */
expect class PhoneAuthProvider() : AuthProvider {

    override val supportedProviders: List<IdentityProvider>

    override suspend fun signIn(request: AuthRequest): AuthResult

    override suspend fun signOut(): AuthResult

    /**
     * Starts the phone number verification process.
     * @param phoneNumber The phone number to verify.
     * @return A [Flow] emitting [PhoneAuthResult] states.
     */
    fun verifyPhoneNumber(phoneNumber: String): Flow<PhoneAuthResult>

    /**
     * Signs in the user using the verification ID and the code sent to the user's phone.
     * @param verificationId The ID of the verification process.
     * @param code The one-time code received by the user.
     * @return An [AuthResult] with the outcome of the sign-in operation.
     */
    suspend fun signInWithVerification(verificationId: String, code: String): AuthResult
}
