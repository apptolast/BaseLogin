package com.apptolast.customlogin.data.repository

import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.User
import com.apptolast.customlogin.domain.repository.AuthRepository
import dev.gitlive.firebase.auth.FirebaseAuth

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
) : AuthRepository {

    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): AuthResult = runCatching {
        firebaseAuth.signInWithEmailAndPassword(email, password)
        firebaseAuth.currentUser?.let { user ->
            // Optionally set display name if needed using user.updateProfile...
            AuthResult.Success(user = User(user.uid, user.email))
        } ?: error("User not found after login.")
    }.getOrElse { AuthResult.Error(it.message.orEmpty(), it) }

    override suspend fun createUserWithEmail(
        email: String, password: String,
        displayName: String?
    ): AuthResult = runCatching {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
        firebaseAuth.currentUser?.let { user ->
            // Optionally set display name if needed using user.updateProfile...
            AuthResult.Success(user = User(user.uid, user.email))
        } ?: error("User not found after registration.")
    }.getOrElse { AuthResult.Error(it.message.orEmpty(), it) }

}
