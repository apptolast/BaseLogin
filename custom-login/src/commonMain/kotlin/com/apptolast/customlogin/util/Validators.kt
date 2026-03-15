package com.apptolast.customlogin.util

/**
 * Centralizes all input validation logic used across ViewModels.
 * Prevents duplication of validation rules throughout the presentation layer.
 */
internal object Validators {

    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

    fun isValidEmail(email: String): Boolean = email.matches(emailRegex)

    fun isValidPassword(password: String): Boolean = password.length >= 6
}
