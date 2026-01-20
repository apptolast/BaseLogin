package com.apptolast.customlogin.domain.model

/**
 * Represents a social login provider.
 */
sealed class SocialProvider {
    /**
     * Google social provider.
     */
    data object Google : SocialProvider()

    /**
     * Phone authentication provider.
     */
    data object Phone : SocialProvider()

    /**
     * A custom social provider.
     * @param id The unique identifier for the custom provider.
     */
    data class Custom(val id: String) : SocialProvider()
}
