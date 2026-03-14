package com.apptolast.customlogin.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.apptolast.customlogin.BuildConfig
import com.apptolast.customlogin.platform.ActivityHolder
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Android-specific helper for Google Sign-In using the Credential Manager API.
 * Returns the Google ID token upon successful authentication, or null on failure/cancellation.
 */
internal class GoogleAuthProviderAndroid(private val context: Context) {

    suspend fun signIn(): String? {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                context = ActivityHolder.requireActivity(),
                request = request,
            )
            GoogleIdTokenCredential.createFrom(result.credential.data).idToken
        } catch (e: GetCredentialException) {
            println("Google Sign-In failed: ${e.message}")
            null
        }
    }
}