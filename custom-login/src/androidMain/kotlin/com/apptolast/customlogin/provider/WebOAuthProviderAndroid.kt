package com.apptolast.customlogin.provider

import com.apptolast.customlogin.SocialTokenResult
import com.apptolast.customlogin.platform.ActivityHolder
import com.apptolast.customlogin.util.Logger
import com.google.firebase.Firebase
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Handles any Firebase OAuthProvider-based web sign-in flow on Android.
 *
 * Used for providers that don't have a native Android SDK (Apple, GitHub, Microsoft).
 * Firebase opens the OAuth page in a Chrome Custom Tab and handles the redirect.
 *
 * On success, the user is already signed in via Firebase. The function returns
 * [SocialTokenResult.PlatformHandled] so [FirebaseAuthProvider] knows to call refreshSession()
 * instead of creating a new credential.
 *
 * ## Required Firebase console setup
 * - Enable the provider (Apple / GitHub / Microsoft) in Firebase Auth.
 * - Add the SHA-1 / SHA-256 fingerprints for your Android app.
 * - For Apple: configure a Services ID and redirect URI in Apple Developer portal.
 * - For GitHub/Microsoft: add the OAuth app redirect URI from Firebase console.
 */
object WebOAuthProviderAndroid {

    suspend fun signIn(
        providerId: String,
        scopes: List<String> = emptyList(),
        customParams: Map<String, String> = emptyMap()
    ): SocialTokenResult? {
        val activity = try {
            ActivityHolder.requireActivity()
        } catch (e: IllegalStateException) {
            Logger.w("WebOAuth", "Activity not available for $providerId OAuth flow.")
            return null
        }

        val provider = OAuthProvider.newBuilder(providerId).apply {
            setScopes(scopes)
            customParams.forEach { (key, value) -> addCustomParameter(key, value) }
        }.build()

        return suspendCancellableCoroutine { cont ->
            Firebase.auth
                .startActivityForSignInWithProvider(activity, provider)
                .addOnSuccessListener {
                    if (cont.isActive) cont.resume(SocialTokenResult.PlatformHandled)
                }
                .addOnFailureListener { e ->
                    Logger.e("WebOAuth", "$providerId sign-in failed: ${e.message}")
                    if (cont.isActive) cont.resume(null)
                }
                .addOnCanceledListener {
                    if (cont.isActive) cont.resume(null)
                }
        }
    }
}