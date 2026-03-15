package com.apptolast.customlogin.util

/**
 * Multiplatform logging utility.
 * Use instead of println() throughout the library.
 */
internal expect object Logger {
    fun d(tag: String, message: String)
    fun w(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}
