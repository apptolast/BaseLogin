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
            // User cancelled the sign-in flow
            println("Google Sign-In cancelled by user")
            null
        } catch (e: NoCredentialException) {
            // No credentials available
            println("No Google credentials available: ${e.message}")
            null
        } catch (e: GetCredentialException) {
            // Other credential errors
            println("Google Sign-In failed: ${e.message}")
            null
        } catch (e: IllegalStateException) {
            // ActivityHolder doesn't have an activity
            println("Google Sign-In failed: ${e.message}")
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
                    println("Unexpected credential type: ${credential.type}")
                    null
                }
            }

            else -> {
                println("Unexpected credential class: ${credential.javaClass.name}")
                null
            }
        }
    }
}
