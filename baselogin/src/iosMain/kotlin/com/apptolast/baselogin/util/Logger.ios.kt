package com.apptolast.baselogin.util

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSLog
import platform.Foundation.NSString
import platform.Foundation.create

/**
 * Every line goes through the `"%@"` format: the first argument of `NSLog` **is** the format string,
 * so passing an interpolated one makes any `%` in the message a format specifier. Firebase errors
 * and OAuth callbacks routinely carry percent-encoded URLs (`%20`), which would read arbitrary
 * memory off the varargs.
 *
 * The argument itself must be an Objective-C object. `NSLog` is variadic, and Kotlin/Native passes a
 * Kotlin `String` in a variadic position as a C string (`char*`), not as an `NSString`: `%@` then
 * sends `respondsToSelector:` to the bytes of the text and the app dies with `EXC_BAD_ACCESS`
 * (measured in 2.0.1). Hence the explicit `NSString` built in [log].
 */
internal actual object Logger {
    actual fun d(tag: String, message: String) = log("[$tag] D: $message")
    actual fun w(tag: String, message: String) = log("[$tag] W: $message")
    actual fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            log("[$tag] E: $message | ${throwable.message}")
        } else {
            log("[$tag] E: $message")
        }
    }

    @OptIn(BetaInteropApi::class)
    private fun log(line: String) = NSLog("%@", NSString.create(string = line))
}
