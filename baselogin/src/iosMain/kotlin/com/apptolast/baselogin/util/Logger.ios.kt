package com.apptolast.baselogin.util

import platform.Foundation.NSLog

/**
 * Every line goes through the `"%@"` format: the first argument of `NSLog` **is** the format string,
 * so passing an interpolated one makes any `%` in the message a format specifier. Firebase errors
 * and OAuth callbacks routinely carry percent-encoded URLs (`%20`), which would read arbitrary
 * memory off the varargs.
 */
internal actual object Logger {
    actual fun d(tag: String, message: String) = NSLog("%@", "[$tag] D: $message")
    actual fun w(tag: String, message: String) = NSLog("%@", "[$tag] W: $message")
    actual fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            NSLog("%@", "[$tag] E: $message | ${throwable.message}")
        } else {
            NSLog("%@", "[$tag] E: $message")
        }
    }
}
