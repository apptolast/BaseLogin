package com.apptolast.customlogin.util

import platform.Foundation.NSLog

internal actual object Logger {
    actual fun d(tag: String, message: String) = NSLog("[$tag] D: $message")
    actual fun w(tag: String, message: String) = NSLog("[$tag] W: $message")
    actual fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            NSLog("[$tag] E: $message | ${throwable.message}")
        } else {
            NSLog("[$tag] E: $message")
        }
    }
}
