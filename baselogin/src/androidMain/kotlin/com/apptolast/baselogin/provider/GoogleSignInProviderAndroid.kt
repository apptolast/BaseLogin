package com.apptolast.baselogin.provider

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.apptolast.baselogin.SocialTokenResult
import com.apptolast.baselogin.config.GoogleSignInConfig
import com.apptolast.baselogin.platform.ActivityHolder
import com.apptolast.baselogin.util.Logger
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
     *
     * Kept for source compatibility; the library itself uses [signInForToken], which does not
     * collapse a failure into a cancellation.
     */
    suspend fun signIn(): String? = (signInForToken() as? SocialTokenResult.Token)?.value

    /**
     * Same flow as [signIn], but tells a cancellation (`null`) apart from a real failure
     * ([SocialTokenResult.Failed]) so a misconfigured Google sign-in is not silenced.
     *
     * Only [GetCredentialCancellationException] is a cancellation. A [NoCredentialException] is a
     * reason to try the full-screen fallback in the first pass, but after that fallback it means there
     * is no way to get a credential at all (Play Services, SHA-1, client id) and is a failure.
     */
    internal suspend fun signInForToken(): SocialTokenResult? {
        val activity = try {
            ActivityHolder.requireActivity()
        } catch (e: IllegalStateException) {
            Logger.e("GoogleSignIn", "No activity available for sign-in", e)
            return SocialTokenResult.Failed(code = null, message = e.message ?: "No activity available for sign-in")
        }

        // Pass 1: bottom-sheet picker (requires an existing Google account on device)
        when (val picker = tryGetGoogleIdOption(activity)) {
            PickerOutcome.Cancelled -> return null
            is PickerOutcome.Token -> return SocialTokenResult.Token(picker.idToken)
            PickerOutcome.TryFallback -> Unit
        }

        // Pass 2: full-screen Google Sign-In UI (works on emulators / fresh devices)
        return trySignInWithGoogleOption(activity)
    }

    private suspend fun tryGetGoogleIdOption(activity: android.app.Activity): PickerOutcome = try {
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
        handleSignInResult(result)?.let { PickerOutcome.Token(it) } ?: PickerOutcome.TryFallback
    } catch (e: GetCredentialCancellationException) {
        Logger.d("GoogleSignIn", "Sign-In cancelled by user")
        PickerOutcome.Cancelled
    } catch (e: NoCredentialException) {
        Logger.w("GoogleSignIn", "No credentials for GetGoogleIdOption, trying fallback: ${e.message}")
        PickerOutcome.TryFallback
    } catch (e: GetCredentialException) {
        Logger.w("GoogleSignIn", "GetGoogleIdOption failed, trying fallback: ${e.message}")
        PickerOutcome.TryFallback
    }

    private suspend fun trySignInWithGoogleOption(activity: android.app.Activity): SocialTokenResult? = try {
        val option = GetSignInWithGoogleOption.Builder(config.webClientId).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val result: GetCredentialResponse = credentialManager.getCredential(
            context = activity,
            request = request,
        )
        handleSignInResult(result)?.let { SocialTokenResult.Token(it) }
            ?: SocialTokenResult.Failed(code = null, message = "Google Sign-In returned no ID token")
    } catch (e: GetCredentialCancellationException) {
        Logger.d("GoogleSignIn", "Sign-In cancelled by user")
        null
    } catch (e: GetCredentialException) {
        // Includes NoCredentialException: after the full-screen fallback it is not a cancellation.
        Logger.e("GoogleSignIn", "Sign-In failed: ${e.type}", e)
        SocialTokenResult.Failed(code = e.type, message = e.message ?: "Google Sign-In failed")
    } catch (e: IllegalStateException) {
        Logger.e("GoogleSignIn", "Sign-In failed: ${e.message}", e)
        SocialTokenResult.Failed(code = null, message = e.message ?: "Google Sign-In failed")
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

    /** What the first pass (bottom-sheet picker) leaves for the flow to do next. */
    private sealed interface PickerOutcome {
        data class Token(val idToken: String) : PickerOutcome
        data object Cancelled : PickerOutcome
        data object TryFallback : PickerOutcome
    }
}
