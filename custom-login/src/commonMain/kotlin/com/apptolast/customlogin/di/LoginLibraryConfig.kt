package com.apptolast.customlogin.di

import com.apptolast.customlogin.config.AppleSignInConfig
import com.apptolast.customlogin.config.GoogleSignInConfig
import com.apptolast.customlogin.config.MagicLinkConfig

const val DEFAULT_PASSWORD_MIN_LENGTH = 6
const val DEFAULT_PHONE_AUTH_TIMEOUT_SECONDS = 60L

/**
 * Password policy used by library-managed password forms.
 *
 * @property minLength Minimum password length enforced by register and reset password screens.
 * @property rejectBlank If true, passwords containing only whitespace are rejected even when they
 * meet [minLength].
 */
data class PasswordPolicyConfig(val minLength: Int = DEFAULT_PASSWORD_MIN_LENGTH, val rejectBlank: Boolean = true)

/**
 * Phone authentication settings used by the default phone auth UI and Firebase provider.
 *
 * @property enabled Allows disabling phone auth without using the legacy [LoginLibraryConfig.phoneEnabled]
 * flag.
 * @property defaultCountryCode Country calling code preselected by the phone auth screen.
 * @property timeoutSeconds Android SMS auto-retrieval timeout. Firebase caps this at 120 seconds.
 */
data class PhoneAuthConfig(
    val enabled: Boolean = true,
    val defaultCountryCode: String = "+1",
    val timeoutSeconds: Long = DEFAULT_PHONE_AUTH_TIMEOUT_SECONDS,
)

/**
 * Web OAuth configuration for providers backed by Firebase's OAuthProvider flow.
 *
 * @property enabled Enables the provider even when the legacy boolean flag is false.
 * @property scopes Provider-specific OAuth scopes.
 * @property customParameters Provider-specific OAuth parameters such as tenant or prompt.
 */
data class OAuthProviderConfig(
    val enabled: Boolean = false,
    val scopes: List<String> = emptyList(),
    val customParameters: Map<String, String> = emptyMap(),
)

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
 * @property passwordPolicy Password validation policy used by the default Register and Reset Password screens.
 * @property phoneAuthConfig Phone auth behavior used by the default Phone screen and Firebase provider.
 * @property githubOAuthConfig GitHub web OAuth scopes and custom parameters.
 * @property microsoftOAuthConfig Microsoft web OAuth scopes and custom parameters.
 * @property twitterOAuthConfig Twitter/X web OAuth scopes and custom parameters.
 * @property facebookOAuthConfig Facebook web OAuth scopes and custom parameters.
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
    val passwordPolicy: PasswordPolicyConfig = PasswordPolicyConfig(),
    val phoneAuthConfig: PhoneAuthConfig = PhoneAuthConfig(),
    val githubOAuthConfig: OAuthProviderConfig = OAuthProviderConfig(scopes = listOf("user:email")),
    val microsoftOAuthConfig: OAuthProviderConfig = OAuthProviderConfig(scopes = listOf("email", "profile")),
    val twitterOAuthConfig: OAuthProviderConfig = OAuthProviderConfig(scopes = listOf("email")),
    val facebookOAuthConfig: OAuthProviderConfig = OAuthProviderConfig(scopes = listOf("email", "public_profile")),
) {
    val isPhoneAuthEnabled: Boolean
        get() = phoneEnabled && phoneAuthConfig.enabled

    val isGitHubAuthEnabled: Boolean
        get() = githubEnabled || githubOAuthConfig.enabled

    val isMicrosoftAuthEnabled: Boolean
        get() = microsoftEnabled || microsoftOAuthConfig.enabled

    val isTwitterAuthEnabled: Boolean
        get() = twitterEnabled || twitterOAuthConfig.enabled

    val isFacebookAuthEnabled: Boolean
        get() = facebookEnabled || facebookOAuthConfig.enabled
}
