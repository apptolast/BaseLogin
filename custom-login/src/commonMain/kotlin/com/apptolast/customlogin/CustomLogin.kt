package com.apptolast.customlogin

import androidx.compose.runtime.Composable
import com.apptolast.customlogin.domain.model.LoginConfig
import com.apptolast.customlogin.domain.provider.AuthProvider
import com.apptolast.customlogin.domain.provider.AuthProviderRegistry
import com.apptolast.customlogin.presentation.navigation.LoginRoute
import com.apptolast.customlogin.presentation.navigation.RootNavGraph
import com.apptolast.customlogin.presentation.navigation.WelcomeRoute
import com.apptolast.customlogin.presentation.theme.AuthScreenSlots
import com.apptolast.customlogin.presentation.theme.AuthTheme
import com.apptolast.customlogin.presentation.theme.AuthThemeProvider

/**
 * Main entry point for the CustomLogin library.
 *
 * ## Quick Start
 *
 * 1. Initialize Koin with auth modules:
 * ```kotlin
 * initKoin {
 *     modules(configModule, dataModule, presentationModule)
 * }
 * ```
 *
 * 2. Use the AuthFlow composable:
 * ```kotlin
 * CustomLogin.AuthFlow(
 *     onAuthSuccess = { session ->
 *         // Navigate to main app
 *     }
 * )
 * ```
 *
 * ## Customization
 *
 * ### Custom Theme
 * ```kotlin
 * CustomLogin.AuthFlow(
 *     theme = AuthTheme(
 *         colors = AuthColors.Light.copy(primary = Color.Blue),
 *         // ...
 *     ),
 *     onAuthSuccess = { }
 * )
 * ```
 *
 * ### Custom Slots
 * ```kotlin
 * CustomLogin.AuthFlow(
 *     slots = AuthScreenSlots(
 *         login = LoginScreenSlots(
 *             logo = { MyCustomLogo() },
 *             socialProviders = { onClick ->
 *                 GoogleSignInButton { onClick("google") }
 *             }
 *         )
 *     ),
 *     onAuthSuccess = { }
 * )
 * ```
 */
object CustomLogin {

    /**
     * Complete authentication flow with navigation.
     *
     * @param theme Custom theme for auth screens (defaults to MaterialTheme-based)
     * @param slots Custom UI slots for screen customization
     * @param showWelcome Whether to show welcome screen first
     * @param onAuthSuccess Callback when authentication succeeds
     */
    @Composable
    fun AuthFlow(
        theme: AuthTheme = AuthTheme.fromMaterialTheme(),
        slots: AuthScreenSlots = AuthScreenSlots(),
        showWelcome: Boolean = true,
        onAuthSuccess: () -> Unit
    ) {
        AuthThemeProvider(theme = theme) {
            RootNavGraph(
                startDestination = if (showWelcome) WelcomeRoute else LoginRoute,
                slots = slots,
                onLoginSuccess = onAuthSuccess,
                onRegisterSuccess = {}
            )
        }
    }

    /**
     * Register a custom authentication provider.
     *
     * @param provider The provider to register
     * @param isDefault Whether this should be the default provider
     */
    fun registerProvider(provider: AuthProvider, isDefault: Boolean = false) {
        AuthProviderRegistry.register(provider, isDefault)
    }

    /**
     * Get the currently registered default provider.
     */
    fun getDefaultProvider(): AuthProvider = AuthProviderRegistry.getDefault()

    /**
     * Get all registered providers.
     */
    fun getAllProviders(): List<AuthProvider> = AuthProviderRegistry.all()

    /**
     * Check if a provider is registered.
     */
    fun isProviderRegistered(id: String): Boolean = AuthProviderRegistry.isRegistered(id)
}

// Re-export commonly used types for convenience
typealias AuthConfig = LoginConfig
