package com.apptolast.customlogin.data.provider

import com.apptolast.customlogin.data.FirebaseAuthProvider
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.Credentials
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class FirebaseAuthProviderTest {

//    private lateinit var mockAuth: MockFirebaseAuth
    private lateinit var authProvider: FirebaseAuthProvider

    @BeforeTest
    fun setUp() {
//        mockAuth = MockFirebaseAuth()
//        authProvider = FirebaseAuthProvider(mockAuth)
    }

    @Test
    fun `signIn with email success returns AuthResult Success`() = runTest {
        // Arrange
        val credentials = Credentials.EmailPassword("test@test.com", "password")
//        mockAuth.signInResult = mockAuth.successResult // Simulate success

        // Act
        val result = authProvider.signIn(credentials)

        // Assert
        assertTrue(result is AuthResult.Success, "Result should be Success")
    }
}

// A simplified mock of FirebaseAuth for testing purposes
//class MockFirebaseAuth : FirebaseAuth by NotImplementedFirebaseAuth() {
//    var signInResult: AuthResult? = null
//    val successUser = object : FirebaseUser by NotImplementedFirebaseUser() {
//        override val uid: String = "12345"
//        override val email: String? = "test@test.com"
//    }
//    val successResult = AuthResult.Success(successUser.toUserSessionUnsafe())
//
//    override suspend fun signInWithEmailAndPassword(email: String, password: String): dev.gitlive.firebase.auth.AuthResult {
//        return object : dev.gitlive.firebase.auth.AuthResult {
//            override val user: FirebaseUser? = if (signInResult is AuthResult.Success) successUser else null
//        }
//    }
//}
//
//// Helper to avoid implementing all interface methods
//open class NotImplementedFirebaseAuth : FirebaseAuth {
//    override val currentUser: FirebaseUser? get() = TODO()
//    override suspend fun createUserWithEmailAndPassword(email: String, password: String): dev.gitlive.firebase.auth.AuthResult = TODO()
//    // ... other methods ...
//    override suspend fun signInWithEmailAndPassword(email: String, password: String): dev.gitlive.firebase.auth.AuthResult = TODO()
//}
//open class NotImplementedFirebaseUser: FirebaseUser {
//    override val uid: String get() = TODO()
//    override val email: String? get() = TODO()
//    // ... other properties ...
//}
fun FirebaseUser.toUserSessionUnsafe() = com.apptolast.customlogin.domain.model.UserSession(uid, email, null, null, false, "firebase", null, null, null)
