package com.apptolast.login.data.repository

import com.apptolast.login.data.model.AuthResult
import com.apptolast.login.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(private val firebaseAuth: FirebaseAuth) : AuthRepository {

    override suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            AuthResult(isSuccess = true, userId = user?.uid)
        } catch (e: Exception) {
            AuthResult(isSuccess = false, errorMessage = e.message)
        }
    }
}