package com.apptolast.customlogin.domain.model

import androidx.compose.runtime.Immutable
import login.custom_login.generated.resources.ATL_isotipo_round
import login.custom_login.generated.resources.Res
import org.jetbrains.compose.resources.DrawableResource

/**
 * Configuration for the Login feature.
 * Use copy() to customize only what you need.
 */
//@Immutable
//data class LoginConfig(
//    // Branding
//    val appName: String = "App to Last",
//    val drawableResource: DrawableResource = Res.drawable.ATL_isotipo_round,
//    val subtitle: String = "Sign in to continue",
//
//    // Features
//    val emailEnabled: Boolean = true,
//    val googleEnabled: Boolean = false,
//    val appleEnabled: Boolean = false,
//    val showRegisterLink: Boolean = true,
//    val showForgotPassword: Boolean = true,
//
//    // Labels (i18n)
//    val emailLabel: String = "Email",
//    val passwordLabel: String = "Password",
//    val signInButtonText: String = "Sign In",
//    val signInWithGoogleText: String = "Sign in with Google",
//    val signInWithAppleText: String = "Sign in with Apple",
//    val registerLinkText: String = "Don't have an account? Sign up",
//    val forgotPasswordText: String = "Forgot password?",
//    val orDividerText: String = "OR",
//
//    // Validation
//    val passwordMinLength: Int = 6,
//
//    // Callbacks
//    val onGoogleSignIn: (() -> Unit)? = null,
//    val onAppleSignIn: (() -> Unit)? = null,
//    val onForgotPassword: (() -> Unit)? = null,
//)