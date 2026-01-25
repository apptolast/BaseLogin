package com.apptolast.customlogin.data

import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Wrapper interface for Firebase User to enable testing.
 * The Firebase SDK uses final classes that cannot be mocked directly.
 */
interface UserWrapper {
    val uid: String
    val email: String?
    val displayName: String?
    val photoURL: String?
    val isEmailVerified: Boolean

    suspend fun getIdToken(forceRefresh: Boolean): String?
    suspend fun updateProfile(displayName: String? = null, photoUrl: String? = null)
    suspend fun delete()
    suspend fun verifyBeforeUpdateEmail(newEmail: String)
    suspend fun updatePassword(newPassword: String)
    suspend fun sendEmailVerification()
}

/**
 * Wrapper interface for Firebase AuthResult to enable testing.
 */
interface AuthResultWrapper {
    val user: UserWrapper?
}

/**
 * Abstraction layer over Firebase Auth SDK to enable testing.
 * This interface wraps the Firebase SDK which uses final classes that cannot be mocked directly.
 */
interface FirebaseAuthService {

    /** The currently signed-in user, or null if none. */
    val currentUser: UserWrapper?

    /** Flow of auth state changes. */
    val authStateChanged: Flow<UserWrapper?>

    /** Signs in with email and password. */
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResultWrapper

    /** Signs in with OAuth credential. */
    suspend fun signInWithCredential(credential: AuthCredential): AuthResultWrapper

    /** Creates a new user with email and password. */
    suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResultWrapper

    /** Signs out the current user. */
    suspend fun signOut()

    /** Sends a password reset email. */
    suspend fun sendPasswordResetEmail(email: String)

    /** Confirms a password reset with the given code. */
    suspend fun confirmPasswordReset(code: String, newPassword: String)
}

/**
 * Production implementation of [UserWrapper] that delegates to the real Firebase SDK.
 */
class FirebaseUserWrapper(private val firebaseUser: FirebaseUser) : UserWrapper {

    override val uid: String
        get() = firebaseUser.uid

    override val email: String?
        get() = firebaseUser.email

    override val displayName: String?
        get() = firebaseUser.displayName

    override val photoURL: String?
        get() = firebaseUser.photoURL

    override val isEmailVerified: Boolean
        get() = firebaseUser.isEmailVerified

    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        return firebaseUser.getIdToken(forceRefresh)
    }

    override suspend fun updateProfile(displayName: String?, photoUrl: String?) {
        firebaseUser.updateProfile(displayName, photoUrl)
    }

    override suspend fun delete() {
        firebaseUser.delete()
    }

    override suspend fun verifyBeforeUpdateEmail(newEmail: String) {
        firebaseUser.verifyBeforeUpdateEmail(newEmail)
    }

    override suspend fun updatePassword(newPassword: String) {
        firebaseUser.updatePassword(newPassword)
    }

    override suspend fun sendEmailVerification() {
        firebaseUser.sendEmailVerification()
    }
}

/**
 * Production implementation of [AuthResultWrapper].
 */
class FirebaseAuthResultWrapper(
    private val authResult: dev.gitlive.firebase.auth.AuthResult
) : AuthResultWrapper {

    override val user: UserWrapper?
        get() = authResult.user?.let { FirebaseUserWrapper(it) }
}

/**
 * Production implementation of [FirebaseAuthService] that delegates to the real Firebase SDK.
 */
class FirebaseAuthServiceImpl(
    private val firebaseAuth: FirebaseAuth
) : FirebaseAuthService {

    override val currentUser: UserWrapper?
        get() = firebaseAuth.currentUser?.let { FirebaseUserWrapper(it) }

    override val authStateChanged: Flow<UserWrapper?>
        get() = firebaseAuth.authStateChanged.map { user ->
            user?.let { FirebaseUserWrapper(it) }
        }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResultWrapper {
        return FirebaseAuthResultWrapper(firebaseAuth.signInWithEmailAndPassword(email, password))
    }

    override suspend fun signInWithCredential(credential: AuthCredential): AuthResultWrapper {
        return FirebaseAuthResultWrapper(firebaseAuth.signInWithCredential(credential))
    }

    override suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResultWrapper {
        return FirebaseAuthResultWrapper(firebaseAuth.createUserWithEmailAndPassword(email, password))
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
    }

    override suspend fun confirmPasswordReset(code: String, newPassword: String) {
        firebaseAuth.confirmPasswordReset(code, newPassword)
    }
}
