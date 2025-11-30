package com.apptolast.customlogin.domain.repository

import com.apptolast.customlogin.domain.model.AuthResult

/**
 * Interface for authentication-related operations.
 * This contract is implemented by the data layer.
 */
interface AuthRepository {
    /**
     * Signs in a user with the given email and password.
     * @return AuthResult indicating success or failure.
     */
    suspend fun signInWithEmail(email: String, password: String): AuthResult
}
