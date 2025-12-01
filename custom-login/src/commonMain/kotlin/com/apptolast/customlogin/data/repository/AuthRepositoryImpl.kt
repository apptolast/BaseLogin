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
        val user = firebaseAuth.currentUser ?: error("User not found after login.")
        AuthResult.Success(user = User(user.uid, user.email))
    }.getOrElse { AuthResult.Error(it.message.orEmpty(), it) }
}
