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
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Android implementation of Google Sign-In using Credential Manager API.
 *
 * Sign-in strategy (two-pass):
 * 1. Try [GetGoogleIdOption] (bottom-sheet picker) — fast path for accounts already on device.
 * 2. On [NoCredentialException] fall back to [GetSignInWithGoogleOption] — full-screen Google
 *    Sign-In UI that works even when no Google account is configured on the device (emulators,
 *    fresh setups, etc.).
 *
 * @property config The Google Sign-In configuration containing the web client ID.
 * @property context The Android application context.
 */
class GoogleSignInProviderAndroid(private val config: GoogleSignInConfig, private val context: Context) {
    private val credentialManager: CredentialManager by lazy {
        CredentialManager.create(context)
    }

    /**
     * Initiates the Google Sign-In flow and returns the ID token, or null if cancelled/failed.
     */
    suspend fun signIn(): String? {
        val activity = try {
            ActivityHolder.requireActivity()
        } catch (e: IllegalStateException) {
            Logger.e("GoogleSignIn", "No activity available for sign-in", e)
            return null
        }

        // Pass 1: bottom-sheet picker (requires an existing Google account on device)
        val idTokenFromPicker = tryGetGoogleIdOption(activity)
        if (idTokenFromPicker != null) return idTokenFromPicker

        // Pass 2: full-screen Google Sign-In UI (works on emulators / fresh devices)
        return trySignInWithGoogleOption(activity)
    }

    private suspend fun tryGetGoogleIdOption(activity: android.app.Activity): String? = try {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(config.webClientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val result: GetCredentialResponse = credentialManager.getCredential(
            context = activity,
            request = request,
        )
        handleSignInResult(result)
    } catch (e: NoCredentialException) {
        Logger.w("GoogleSignIn", "No credentials for GetGoogleIdOption, trying fallback: ${e.message}")
        null
    } catch (e: GetCredentialCancellationException) {
        Logger.d("GoogleSignIn", "Sign-In cancelled by user")
        null
    } catch (e: GetCredentialException) {
        Logger.w("GoogleSignIn", "GetGoogleIdOption failed, trying fallback: ${e.message}")
        null
    }

    private suspend fun trySignInWithGoogleOption(activity: android.app.Activity): String? = try {
        val option = GetSignInWithGoogleOption.Builder(config.webClientId).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val result: GetCredentialResponse = credentialManager.getCredential(
            context = activity,
            request = request,
        )
        handleSignInResult(result)
    } catch (e: GetCredentialCancellationException) {
        Logger.d("GoogleSignIn", "Sign-In cancelled by user")
        null
    } catch (e: GetCredentialException) {
        Logger.e("GoogleSignIn", "Sign-In failed: ${e.message}", e)
        null
    } catch (e: IllegalStateException) {
        Logger.e("GoogleSignIn", "Sign-In failed: ${e.message}", e)
        null
    }

    private fun handleSignInResult(result: GetCredentialResponse): String? {
        val credential = result.credential
        return when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    GoogleIdTokenCredential.createFrom(credential.data).idToken
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
