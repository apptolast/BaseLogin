package com.apptolast.customlogin.util

import android.util.Log

internal actual object Logger {
    actual fun d(tag: String, message: String) { Log.d(tag, message) }
    actual fun w(tag: String, message: String) { Log.w(tag, message) }
    actual fun e(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}
