package com.apptolast.customlogin.di

import com.apptolast.customlogin.config.AppleSignInConfig
import com.apptolast.customlogin.config.GoogleSignInConfig
import com.apptolast.customlogin.config.MagicLinkConfig
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * Configuration for the login library.
 *
 * @property googleSignInConfig Configuration for Google Sign-In. If null, Google Sign-In will not be available.
 * @property appleSignInConfig Configuration for Apple Sign-In (iOS native; web OAuth on Android).
 * @property githubEnabled If true, GitHub Sign-In is available (web OAuth via Firebase on both platforms).
 * @property microsoftEnabled If true, Microsoft Sign-In is available (web OAuth via Firebase on both platforms).
 * @property magicLinkConfig Configuration for passwordless email (Magic Link). If null, Magic Link is disabled.
 */
data class LoginLibraryConfig(
    val googleSignInConfig: GoogleSignInConfig? = null,
    val appleSignInConfig: AppleSignInConfig? = null,
    val githubEnabled: Boolean = false,
    val microsoftEnabled: Boolean = false,
    val magicLinkConfig: MagicLinkConfig? = null
)

/**
 * Initializes Koin with custom LoginConfig.
 *
 * @param config The library configuration including social sign-in providers.
 * @param appDeclaration Additional Koin configuration.
 */
fun initLoginKoin(
    config: LoginLibraryConfig = LoginLibraryConfig(),
    appDeclaration: KoinAppDeclaration? = null
) {
    startKoin {
        appDeclaration?.invoke(this)

        // Module for configuration
        val configModule = module {
            single { config }
            config.googleSignInConfig?.let { googleConfig ->
                single { googleConfig }
            }
            config.appleSignInConfig?.let { appleConfig ->
                single { appleConfig }
            }
            config.magicLinkConfig?.let { mlConfig ->
                single { mlConfig }
            }
        }

        modules(
            configModule,
            dataModule,
            presentationModule,
        )
    }
}