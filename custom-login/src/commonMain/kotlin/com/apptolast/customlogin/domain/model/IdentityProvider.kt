package com.apptolast.customlogin.domain.model

/**
 * Represents an identity provider for authentication.
 * This is a sealed interface to represent a closed set of supported providers.
 */
sealed interface IdentityProvider {
    data object Email : IdentityProvider
    data object Google : IdentityProvider
    data object Apple : IdentityProvider
    data object Facebook : IdentityProvider
    data object GitHub : IdentityProvider
    data object Phone : IdentityProvider
    data class Custom(val customId: String) : IdentityProvider
}
