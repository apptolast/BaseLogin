package com.apptolast.customlogin

import com.apptolast.customlogin.domain.model.IdentityProvider

expect fun platform(): String

/**
 * Initiates a platform-specific social sign-in flow to get an ID token.
 * This common `expect` function is implemented in each platform's `actual` function.
 *
 * @param provider The social provider (e.g., Google, GitHub).
 * @return The ID token from the provider upon successful sign-in, or null if the flow is cancelled or fails.
 */
expect suspend fun getSocialIdToken(provider: IdentityProvider): String?
