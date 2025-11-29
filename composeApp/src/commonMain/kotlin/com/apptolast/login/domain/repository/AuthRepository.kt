package com.apptolast.login.domain.repository

import com.apptolast.login.data.model.AuthResult

interface AuthRepository {
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult
}