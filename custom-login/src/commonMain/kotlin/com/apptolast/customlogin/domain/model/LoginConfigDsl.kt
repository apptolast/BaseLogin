// customlogin/src/commonMain/kotlin/.../domain/model/LoginConfigBuilder.kt
package com.apptolast.customlogin.domain.model

/**
 * DSL builder for LoginConfig.
 * Allows clean and readable configuration.
 */
//@DslMarker
//annotation class LoginConfigDsl
//
//@LoginConfigDsl
//class LoginConfigBuilder {
//    var appName: String = "App to Last"
//    var appLogoUrl: String = ""
//    var subtitle: String = "Sign in to continue"
//
//    var emailEnabled: Boolean = true
//    var googleEnabled: Boolean = false
//    var appleEnabled: Boolean = false
//    var showRegisterLink: Boolean = true
//    var showForgotPassword: Boolean = true
//
//    var emailLabel: String = "Email"
//    var passwordLabel: String = "Password"
//    var signInButtonText: String = "Sign In"
//    var signInWithGoogleText: String = "Sign in with Google"
//    var signInWithAppleText: String = "Sign in with Apple"
//    var registerLinkText: String = "Don't have an account? Sign up"
//    var forgotPasswordText: String = "Forgot password?"
//    var orDividerText: String = "OR"
//
//    var passwordMinLength: Int = 6
//
//    var onGoogleSignIn: (() -> Unit)? = null
//    var onAppleSignIn: (() -> Unit)? = null
//    var onForgotPassword: (() -> Unit)? = null
//
//    internal fun build() = LoginConfig(
//        appName = appName,
//        drawableResource = drawableResource,
//        subtitle = subtitle,
//        emailEnabled = emailEnabled,
//        googleEnabled = googleEnabled,
//        appleEnabled = appleEnabled,
//        showRegisterLink = showRegisterLink,
//        showForgotPassword = showForgotPassword,
//        emailLabel = emailLabel,
//        passwordLabel = passwordLabel,
//        signInButtonText = signInButtonText,
//        signInWithGoogleText = signInWithGoogleText,
//        signInWithAppleText = signInWithAppleText,
//        registerLinkText = registerLinkText,
//        forgotPasswordText = forgotPasswordText,
//        orDividerText = orDividerText,
//        passwordMinLength = passwordMinLength,
//        onGoogleSignIn = onGoogleSignIn,
//        onAppleSignIn = onAppleSignIn,
//        onForgotPassword = onForgotPassword
//    )
//}
//
///**
// * Creates a LoginConfig using DSL syntax.
// *
// * Example:
// * ```
// * val config = buildLoginConfig {
// *     appName = "My App"
// *     googleEnabled = true
// *     onGoogleSignIn = { /* handle */ }
// * }
// * ```
// */
//fun buildLoginConfig(block: LoginConfigBuilder.() -> Unit = {}): LoginConfig {
//    return LoginConfigBuilder().apply(block).build()
//}