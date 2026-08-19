package com.apptolast.baselogin.util

import com.apptolast.baselogin.di.DEFAULT_PASSWORD_MIN_LENGTH

/**
 * Centralizes all input validation logic used across ViewModels.
 * Prevents duplication of validation rules throughout the presentation layer.
 */
internal object Validators {

    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

    fun isValidEmail(email: String): Boolean = email.matches(emailRegex)

    fun isValidPassword(
        password: String,
        minLength: Int = DEFAULT_PASSWORD_MIN_LENGTH,
        rejectBlank: Boolean = true,
    ): Boolean {
        val effectiveMinLength = minLength.coerceAtLeast(1)
        return password.length >= effectiveMinLength && (!rejectBlank || password.isNotBlank())
    }
}
