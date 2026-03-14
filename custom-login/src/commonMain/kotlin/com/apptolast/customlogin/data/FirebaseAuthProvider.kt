package com.apptolast.customlogin.data

import com.apptolast.customlogin.domain.AuthProvider
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthRequest
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.auth.auth

/**
 * AuthProvider implementation for Firebase Email/Password authentication.
 */
class FirebaseAuthProvider : AuthProvider {

    private val firebaseAuth: FirebaseAuth = Firebase.auth

    override val supportedProviders: List<IdentityProvider> = listOf(IdentityProvider.Email)

    override suspend fun signIn(request: AuthRequest): AuthResult {
        val email = request.email ?: return AuthResult.Failure(AuthError.InvalidRequest("Email is required."))
        val password = request.password ?: return AuthResult.Failure(AuthError.InvalidRequest("Password is required."))

        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password)
            DataMapper.toUserSession(result.user, IdentityProvider.Email)?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Failure(AuthError.Unknown("Firebase returned no user."))
        } catch (e: FirebaseAuthException) {
            AuthResult.Failure(DataMapper.toAuthError(e))
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.Unknown(e.message))
        }
    }

    suspend fun signUp(request: AuthRequest): AuthResult {
        val email = request.email ?: return AuthResult.Failure(AuthError.InvalidRequest("Email is required."))
        val password = request.password ?: return AuthResult.Failure(AuthError.InvalidRequest("Password is required."))

        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password)
            DataMapper.toUserSession(result.user, IdentityProvider.Email)?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Failure(AuthError.Unknown("Firebase returned no user."))
        } catch (e: FirebaseAuthException) {
            AuthResult.Failure(DataMapper.toAuthError(e))
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.Unknown(e.message))
        }
    }

    override suspend fun signOut(): AuthResult {
        return try {
            firebaseAuth.signOut()
            AuthResult.SignOutSuccess
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.Unknown(e.message))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            firebaseAuth.sendPasswordResetEmail(email)
            AuthResult.PasswordResetSent
        } catch (e: FirebaseAuthException) {
            AuthResult.Failure(DataMapper.toAuthError(e))
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.Unknown(e.message))
        }
    }

    suspend fun confirmPasswordReset(code: String, newPassword: String): AuthResult {
        return try {
            firebaseAuth.confirmPasswordReset(code, newPassword)
            AuthResult.PasswordResetSuccess
        } catch (e: FirebaseAuthException) {
            AuthResult.Failure(DataMapper.toAuthError(e))
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.Unknown(e.message))
        }
    }
}
