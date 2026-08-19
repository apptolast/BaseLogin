package com.apptolast.customlogin.domain.model

/**
 * Represents an identity provider for authentication.
 * Each provider has a unique ID that often corresponds to the Firebase provider ID.
 */
sealed class IdentityProvider(val id: String) {
    data object Google : IdentityProvider("google.com")
    data object Apple : IdentityProvider("apple.com")
    data object Facebook : IdentityProvider("facebook.com")
    data object GitHub : IdentityProvider("github.com")
    data object Microsoft : IdentityProvider("microsoft.com")
    data object Twitter : IdentityProvider("twitter.com")
    data object MagicLink : IdentityProvider("magiclink")
    data object Phone : IdentityProvider("phone")
    data class Custom(val customId: String) : IdentityProvider(customId)

    companion object {
        /**
         * Translates a Firebase provider id back to the domain type.
         *
         * Returns `null` for anything this library does not model — `password` among them — instead
         * of guessing a [Custom]: callers use this to decide whether a provider needs special
         * treatment, and a made-up value would send them down the wrong path.
         */
        fun fromId(id: String): IdentityProvider? = when (id) {
            Google.id -> Google
            Apple.id -> Apple
            Facebook.id -> Facebook
            GitHub.id -> GitHub
            Microsoft.id -> Microsoft
            Twitter.id -> Twitter
            MagicLink.id -> MagicLink
            Phone.id -> Phone
            else -> null
        }
    }
}
