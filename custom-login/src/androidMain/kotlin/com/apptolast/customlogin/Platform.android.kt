package com.apptolast.customlogin

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.apptolast.customlogin.SocialTokenResult
import com.apptolast.customlogin.config.GoogleSignInConfig
import com.apptolast.customlogin.di.LoginLibraryConfig
import com.apptolast.customlogin.di.OAuthProviderConfig
import com.apptolast.customlogin.domain.model.AuthError
import com.apptolast.customlogin.domain.model.AuthResult
import com.apptolast.customlogin.domain.model.IdentityProvider
import com.apptolast.customlogin.domain.model.PhoneAuthResult
import com.apptolast.customlogin.domain.model.UserSession
import com.apptolast.customlogin.platform.ActivityHolder
import com.apptolast.customlogin.provider.GoogleSignInProviderAndroid
import com.apptolast.customlogin.provider.WebOAuthProviderAndroid
import com.apptolast.customlogin.util.Logger
import com.google.firebase.Firebase
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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
    val loginConfig: LoginLibraryConfig by lazy {
        try {
            val config: LoginLibraryConfig by inject()
            config
        } catch (e: Exception) {
            LoginLibraryConfig()
        }
    }

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
actual suspend fun getSocialIdToken(provider: IdentityProvider): SocialTokenResult? {
    return when (provider) {
        is IdentityProvider.Google -> {
            val config = PlatformKoinHelper.googleSignInConfig
            if (config == null) {
                Logger.w(
                    "Platform",
                    "Google Sign-In is not configured. Provide GoogleSignInConfig in LoginLibraryConfig.",
                )
                return null
            }

            val googleProvider = GoogleSignInProviderAndroid(
                config = config,
                context = appContext,
            )
            googleProvider.signIn()?.let { SocialTokenResult.Token(it) }
        }
        is IdentityProvider.Apple ->
            WebOAuthProviderAndroid.signIn(
                providerId = "apple.com",
                scopes = PlatformKoinHelper.loginConfig.appleSignInConfig?.scopes ?: listOf("email", "name"),
            )
        is IdentityProvider.GitHub ->
            signInWithWebOAuth("github.com", PlatformKoinHelper.loginConfig.githubOAuthConfig)
        is IdentityProvider.Microsoft ->
            signInWithWebOAuth("microsoft.com", PlatformKoinHelper.loginConfig.microsoftOAuthConfig)
        is IdentityProvider.Twitter ->
            signInWithWebOAuth("twitter.com", PlatformKoinHelper.loginConfig.twitterOAuthConfig)
        is IdentityProvider.Facebook ->
            signInWithWebOAuth("facebook.com", PlatformKoinHelper.loginConfig.facebookOAuthConfig)
        else -> {
            Logger.w("Platform", "Social sign-in for ${provider.id} is not implemented on Android yet.")
            null
        }
    }
}

private suspend fun signInWithWebOAuth(providerId: String, config: OAuthProviderConfig): SocialTokenResult? =
    WebOAuthProviderAndroid.signIn(
        providerId = providerId,
        scopes = config.scopes,
        customParams = config.customParameters,
    )

/**
 * Android actual implementation: uses native Firebase PhoneAuthProvider with SIM-based
 * instant verification support via [PhoneAuthProvider.OnVerificationStateChangedCallbacks].
 */
actual suspend fun sendPhoneVerificationCode(phoneNumber: String, timeoutSeconds: Long): PhoneAuthResult =
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
                                    ),
                                ),
                            )
                        } else if (cont.isActive) {
                            cont.resume(PhoneAuthResult.Failure(AuthError.Unknown("Auto sign-in returned no user")))
                        }
                    }
                    .addOnFailureListener { e ->
                        if (cont.isActive) {
                            cont.resume(
                                PhoneAuthResult.Failure(
                                    AuthError.Unknown(
                                        e.message ?: "Auto verification failed",
                                    ),
                                ),
                            )
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
            .setTimeout(timeoutSeconds.coerceIn(0L, 120L), TimeUnit.SECONDS)
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
                            ),
                        ),
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

/**
 * Clears Credential Manager's cached credential state.
 *
 * Without this, after signing out of Firebase the Google account picker does not reappear, because
 * Credential Manager keeps returning the previously chosen account, and the user cannot switch.
 */
actual suspend fun clearSocialSignInState() {
    try {
        CredentialManager.create(appContext).clearCredentialState(ClearCredentialStateRequest())
    } catch (e: Exception) {
        // Never fail sign-out because of this: the Firebase session is already gone.
        Logger.w("Platform", "clearCredentialState failed: ${e.message}")
    }
}

/**
 * Declared no-op.
 *
 * App Review guideline 5.1.1(v) applies to the iOS app, and Apple Sign-In on Android is Firebase's
 * web OAuth flow, which issues no token this side can revoke. Deleting the Firebase user is all
 * there is to do here.
 */
actual suspend fun revokeSocialToken(provider: IdentityProvider) {
    Logger.d("Platform", "Nothing to revoke on Android for ${provider.id}.")
}
