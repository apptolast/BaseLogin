package com.apptolast.customlogin.di

import com.apptolast.customlogin.config.AppleSignInConfig
import com.apptolast.customlogin.config.GoogleSignInConfig
import com.apptolast.customlogin.config.MagicLinkConfig

/**
 * Configuration for the login library.
 *
 * @property googleSignInConfig Configuration for Google Sign-In. If null, Google Sign-In will not be available.
 * @property appleSignInConfig Configuration for Apple Sign-In (iOS native; web OAuth on Android).
 * @property githubEnabled If true, GitHub Sign-In is available (web OAuth via Firebase on both platforms).
 * @property microsoftEnabled If true, Microsoft Sign-In is available (web OAuth via Firebase on both platforms).
 * @property magicLinkConfig Configuration for passwordless email (Magic Link). If null, Magic Link is disabled.
 * @property phoneEnabled If true, Phone (SMS OTP) Sign-In is available. Defaults to true.
 * @property twitterEnabled If true, Twitter/X Sign-In is available (web OAuth via Firebase on both platforms).
 * @property facebookEnabled If true, Facebook Sign-In is available (web OAuth via Firebase on both platforms).
 */
data class LoginLibraryConfig(
    val googleSignInConfig: GoogleSignInConfig? = null,
    val appleSignInConfig: AppleSignInConfig? = null,
    val githubEnabled: Boolean = false,
    val microsoftEnabled: Boolean = false,
    val magicLinkConfig: MagicLinkConfig? = null,
    val phoneEnabled: Boolean = true,
    val twitterEnabled: Boolean = false,
    val facebookEnabled: Boolean = false,
)
