package com.apptolast.customlogin

import android.content.Context
import com.apptolast.customlogin.domain.model.IdentityProvider

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
 * Actual implementation for getting a social ID token on Android.
 */
actual suspend fun getSocialIdToken(provider: IdentityProvider): String? {
    return when (provider) {
//        is IdentityProvider.Google -> GoogleSignInHandler.signIn()
        is IdentityProvider.GitHub -> {
            // TODO: Implement GitHub OAuth flow for Android.
            println("GitHub Sign-In for Android is not implemented yet.")
            null
        }
        else -> {
            println("Social sign-in for ${provider.id} is not implemented on Android yet.")
            null
        }
    }
}

// TODO: Implement this handler in your androidApp module.
// This object needs to be a singleton that can be accessed from your KMP code.
// It will be responsible for launching the Google Sign-In intent and handling the result.
/*
object GoogleSignInHandler {
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID") // Replace with your web client ID
            .requestEmail()
            .build()
        GoogleSignIn.getClient(appContext, gso)
    }

    private val resultChannel = Channel<String?>()

    // This function is called from your Activity's onActivityResult or ActivityResultLauncher
    fun onSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            resultChannel.trySend(account?.idToken)
        } catch (e: ApiException) {
            println("Google sign in failed: ${e.statusCode}")
            resultChannel.trySend(null)
        }
    }

    // This is the function called from your KMP code
    suspend fun signIn(): String? {
        // Check for existing signed-in user
        val lastUser = GoogleSignIn.getLastSignedInAccount(appContext)
        if (lastUser != null && !lastUser.isExpired) {
            return lastUser.idToken
        }

        // Launch the sign-in intent
        // This part needs to be connected to your Android Activity
        val activity = (appContext as? Activity) ?: //... find your current activity
        activity.startActivityForResult(googleSignInClient.signInIntent, 9001)

        // Wait for the result from onSignInResult
        return resultChannel.receive()
    }
}
*/
