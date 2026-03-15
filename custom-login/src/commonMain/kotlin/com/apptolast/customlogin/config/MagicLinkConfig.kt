package com.apptolast.customlogin.config

/**
 * Configuration for Magic Link (email passwordless) sign-in.
 *
 * Firebase sends an email with a link that opens the app. The host app must:
 * 1. Register a deep link / Universal Link / App Link for [continueUrl].
 * 2. When the app is opened via that link, call:
 *    `authRepository.signInWithMagicLink(email, link)` with the captured URL.
 *
 * @param continueUrl The URL Firebase embeds in the email link.
 *   This must be an App Link (Android) / Universal Link (iOS) that opens your app.
 *   Example: "https://yourapp.page.link/signin"
 * @param iosBundleId Your iOS app bundle ID (required for iOS deep link handling).
 */
data class MagicLinkConfig(
    val continueUrl: String,
    val iosBundleId: String? = null
)