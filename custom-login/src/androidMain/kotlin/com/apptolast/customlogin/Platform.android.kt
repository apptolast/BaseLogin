package com.apptolast.customlogin

import android.content.Context
import com.apptolast.customlogin.config.GoogleSignInConfig
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.domain.model.PhoneAuthResult
import com.apptolast.customlogin.domain.model.UserSession
import com.apptolast.customlogin.platform.ActivityHolder
import com.apptolast.customlogin.provider.GoogleSignInProviderAndroid
import com.apptolast.customlogin.util.Logger
import com.google.firebase.Firebase
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * Android-specific implementation of the common `expect` declarations.
 */
actual fun platform(): String = "Android ${android.os.Build.VERSION.SDK_INT}"

/**
 * The Android context, required for many platform-specific operations.
 * This must be initialized at app startup.
 */
lateinit var appContext: Context

/**
 * Helper object for Koin dependency injection in platform code.
 */
private object PlatformKoinHelper : KoinComponent {
    val googleSignInConfig: GoogleSignInConfig? by lazy {
        try {
            val config: GoogleSignInConfig by inject()
            config
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Actual implementation for getting a social ID token on Android.
 */
actual suspend fun getSocialIdToken(provider: IdentityProvider): String? {
    return when (provider) {
        is IdentityProvider.Google -> {
            val config = PlatformKoinHelper.googleSignInConfig
            if (config == null) {
                Logger.w("Platform", "Google Sign-In is not configured. Provide GoogleSignInConfig in LoginLibraryConfig.")
                return null
            }

            val googleProvider = GoogleSignInProviderAndroid(
                config = config,
                context = appContext
            )
            googleProvider.signIn()
        }
        is IdentityProvider.Apple -> {
            Logger.w("Platform", "Apple Sign-In is not available on Android (iOS only).")
            null
        }
        is IdentityProvider.GitHub -> {
            // TODO: Implement GitHub OAuth flow for Android.
            Logger.w("Platform", "GitHub Sign-In for Android is not implemented yet.")
            null
        }
        else -> {
            Logger.w("Platform", "Social sign-in for ${provider.id} is not implemented on Android yet.")
            null
        }
    }
}

/**
 * Android actual implementation: uses native Firebase PhoneAuthProvider with SIM-based
 * instant verification support via [PhoneAuthProvider.OnVerificationStateChangedCallbacks].
 */
actual suspend fun sendPhoneVerificationCode(phoneNumber: String): PhoneAuthResult =
    suspendCancellableCoroutine { cont ->
        val activity = try {
            ActivityHolder.requireActivity()
        } catch (e: IllegalStateException) {
            cont.resume(PhoneAuthResult.Failure(AuthError.Unknown("Activity not available for phone auth")))
            return@suspendCancellableCoroutine
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Android instant verification — sign in automatically
                Firebase.auth.signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        val user = result.user
                        if (user != null && cont.isActive) {
                            cont.resume(
                                PhoneAuthResult.AutoSignedIn(
                                    UserSession(
                                        userId = user.uid,
                                        email = user.email,
                                        displayName = user.displayName,
                                        photoUrl = user.photoUrl?.toString(),
                                        isEmailVerified = user.isEmailVerified,
                                        providerId = "firebase",
                                    )
                                )
                            )
                        } else if (cont.isActive) {
                            cont.resume(PhoneAuthResult.Failure(AuthError.Unknown("Auto sign-in returned no user")))
                        }
                    }
                    .addOnFailureListener { e ->
                        if (cont.isActive) {
                            cont.resume(PhoneAuthResult.Failure(AuthError.Unknown(e.message ?: "Auto verification failed")))
                        }
                    }
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                if (cont.isActive) {
                    cont.resume(PhoneAuthResult.Failure(AuthError.Unknown(e.message ?: "Phone verification failed")))
                }
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                if (cont.isActive) {
                    cont.resume(PhoneAuthResult.CodeSent(verificationId))
                }
            }
        }

        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

/**
 * Android actual implementation: creates a [PhoneAuthCredential] from the given IDs and
 * signs in using the native Firebase SDK.
 */
actual suspend fun verifyPhoneCode(verificationId: String, otpCode: String): AuthResult =
    suspendCancellableCoroutine { cont ->
        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
        Firebase.auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null && cont.isActive) {
                    cont.resume(
                        AuthResult.Success(
                            UserSession(
                                userId = user.uid,
                                email = user.email,
                                displayName = user.displayName,
                                photoUrl = user.photoUrl?.toString(),
                                isEmailVerified = user.isEmailVerified,
                                providerId = "firebase",
                            )
                        )
                    )
                } else if (cont.isActive) {
                    cont.resume(AuthResult.Failure(AuthError.Unknown("No user returned after phone sign in")))
                }
            }
            .addOnFailureListener { e ->
                if (cont.isActive) {
                    cont.resume(AuthResult.Failure(AuthError.Unknown(e.message ?: "Phone OTP verification failed")))
                }
            }
    }
