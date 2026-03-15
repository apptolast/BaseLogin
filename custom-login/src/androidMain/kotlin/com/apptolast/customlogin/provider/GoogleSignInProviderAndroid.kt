package com.apptolast.customlogin.provider

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.apptolast.customlogin.config.GoogleSignInConfig
import com.apptolast.customlogin.platform.ActivityHolder
import com.apptolast.customlogin.util.Logger
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Android implementation of Google Sign-In using Credential Manager API.
 *
 * @property config The Google Sign-In configuration containing the web client ID.
 * @property context The Android application context.
 */
class GoogleSignInProviderAndroid(
    private val config: GoogleSignInConfig,
    private val context: Context
) {
    private val credentialManager: CredentialManager by lazy {
        CredentialManager.create(context)
    }

    /**
     * Initiates the Google Sign-In flow and returns the ID token.
     *
     * @return The Google ID token on success, or null if cancelled/failed.
     */
    suspend fun signIn(): String? {
        return try {
            val activity = ActivityHolder.requireActivity()

            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(config.webClientId)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                context = activity,
                request = request
            )

            handleSignInResult(result)
        } catch (e: GetCredentialCancellationException) {
            Logger.d("GoogleSignIn", "Sign-In cancelled by user")
            null
        } catch (e: NoCredentialException) {
            Logger.w("GoogleSignIn", "No Google credentials available: ${e.message}")
            null
        } catch (e: GetCredentialException) {
            Logger.e("GoogleSignIn", "Sign-In failed: ${e.message}", e)
            null
        } catch (e: IllegalStateException) {
            Logger.e("GoogleSignIn", "Sign-In failed: ${e.message}", e)
            null
        }
    }

    /**
     * Handles the credential response and extracts the ID token.
     */
    private fun handleSignInResult(result: GetCredentialResponse): String? {
        val credential = result.credential

        return when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    googleIdTokenCredential.idToken
                } else {
                    Logger.w("GoogleSignIn", "Unexpected credential type: ${credential.type}")
                    null
                }
            }

            else -> {
                Logger.w("GoogleSignIn", "Unexpected credential class: ${credential.javaClass.name}")
                null
            }
        }
    }
}
