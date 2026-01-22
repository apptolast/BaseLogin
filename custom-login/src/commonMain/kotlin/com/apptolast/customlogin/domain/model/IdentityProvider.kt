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
    data object Phone : IdentityProvider("phone")
    data class Custom(val customId: String) : IdentityProvider(customId)
}
