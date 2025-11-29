package com.apptolast.login.data.repository

import com.apptolast.login.data.model.AuthResult
import com.apptolast.login.domain.repository.AuthRepository
import cocoapods.FirebaseAuth.FIRAuth
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepositoryIos(private val firebaseAuth: FIRAuth) : AuthRepository {

    override suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult {
        return suspendCoroutine { continuation ->
            firebaseAuth.signInWithEmail(email, password) { authDataResult, error ->
                if (authDataResult != null) {
                    continuation.resume(AuthResult(isSuccess = true, userId = authDataResult.user.uid))
                } else {
                    val errorMessage = error?.localizedDescription ?: "Unknown error"
                    continuation.resume(AuthResult(isSuccess = false, errorMessage = errorMessage))
                }
            }
        }
    }
}