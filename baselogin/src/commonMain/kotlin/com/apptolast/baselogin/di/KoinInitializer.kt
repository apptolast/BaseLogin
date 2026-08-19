package com.apptolast.baselogin.di

import com.apptolast.baselogin.domain.AuthProvider
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

/**
 * Koin module for library configuration.
 *
 * @param config The library configuration including social sign-in providers.
 */
fun loginConfigModule(config: LoginLibraryConfig = LoginLibraryConfig()): Module = module {
    single { config }
    config.googleSignInConfig?.let { googleConfig ->
        single { googleConfig }
    }
}

/**
 * Builds every Koin module needed by the login library.
 *
 * Use this when the host app owns the `startKoin { ... }` call:
 *
 * ```kotlin
 * startKoin {
 *     modules(appModule)
 *     modules(loginModules(config))
 * }
 * ```
 *
 * @param config Login library configuration.
 * @param authProvider Optional custom auth provider. When null, the Firebase provider is registered.
 */
fun loginModules(config: LoginLibraryConfig = LoginLibraryConfig(), authProvider: AuthProvider? = null): List<Module> =
    listOf(
        loginConfigModule(config),
        loginDataModule(authProvider),
        loginPresentationModule,
    )

/**
 * Loads the login modules into an already started Koin application.
 *
 * The returned modules can be passed to [unloadLoginKoinModules], which is useful in tests or
 * dynamic feature/module boundaries.
 */
fun loadLoginKoinModules(
    config: LoginLibraryConfig = LoginLibraryConfig(),
    authProvider: AuthProvider? = null,
): List<Module> {
    val loginModulesList = loginModules(config, authProvider)
    loadKoinModules(loginModulesList)
    return loginModulesList
}

/**
 * Unloads modules previously returned by [loadLoginKoinModules] or [loginModules].
 */
fun unloadLoginKoinModules(modules: List<Module>) {
    unloadKoinModules(modules)
}

/**
 * Initializes Koin with the login modules.
 *
 * If Koin is already running, this function loads the login modules into the existing container
 * instead of calling `startKoin` again. Hosts that need full control should use [loginModules] or
 * [loadLoginKoinModules] directly.
 *
 * @param config The library configuration including social sign-in providers.
 * @param authProvider Optional custom auth provider. When null, the Firebase provider is registered.
 * @param appDeclaration Additional Koin configuration used only when this function starts Koin.
 */
fun initLoginKoin(
    config: LoginLibraryConfig = LoginLibraryConfig(),
    authProvider: AuthProvider? = null,
    appDeclaration: KoinAppDeclaration? = null,
) {
    val loginModulesList = loginModules(config, authProvider)
    if (KoinPlatformTools.defaultContext().getOrNull() != null) {
        loadKoinModules(loginModulesList)
    } else {
        startKoin {
            appDeclaration?.invoke(this)
            modules(loginModulesList)
        }
    }
}
