package com.apptolast.customlogin.domain.provider

/**
 * Registry for authentication providers.
 * Allows dynamic registration and retrieval of auth providers at runtime.
 */
object AuthProviderRegistry {
    private val providers = mutableMapOf<String, AuthProvider>()
    private var defaultProviderId: String? = null

    /**
     * Register an authentication provider.
     * @param provider The provider to register.
     * @param isDefault Whether this should be the default provider.
     */
    fun register(provider: AuthProvider, isDefault: Boolean = false) {
        providers[provider.id] = provider
        if (isDefault || defaultProviderId == null) {
            defaultProviderId = provider.id
        }
    }

    /**
     * Unregister a provider by ID.
     */
    fun unregister(id: String) {
        providers.remove(id)
        if (defaultProviderId == id) {
            defaultProviderId = providers.keys.firstOrNull()
        }
    }

    /**
     * Get a provider by ID.
     */
    fun get(id: String): AuthProvider? = providers[id]

    /**
     * Get the default provider.
     * @throws IllegalStateException if no provider is registered.
     */
    fun getDefault(): AuthProvider = providers[defaultProviderId]
        ?: throw IllegalStateException("No default AuthProvider registered. Call register() first.")

    /**
     * Get all registered providers.
     */
    fun all(): List<AuthProvider> = providers.values.toList()

    /**
     * Get the IDs of all registered providers.
     */
    fun allIds(): Set<String> = providers.keys.toSet()

    /**
     * Check if a provider is registered.
     */
    fun isRegistered(id: String): Boolean = providers.containsKey(id)

    /**
     * Set the default provider ID.
     * @throws IllegalArgumentException if the provider is not registered.
     */
    fun setDefault(id: String) {
        require(providers.containsKey(id)) { "Provider '$id' is not registered" }
        defaultProviderId = id
    }

    /**
     * Get the current default provider ID.
     */
    fun getDefaultId(): String? = defaultProviderId

    /**
     * Clear all registered providers.
     */
    fun clear() {
        providers.clear()
        defaultProviderId = null
    }
}
