import FirebaseAuth
import ComposeApp

/// Phone (SMS OTP) sign-in.
///
/// Two handlers, because the flow has two round trips: `sendCodeHandler` returns the
/// `verificationID` that the library carries to the OTP screen, and `verifyCodeHandler` signs in
/// and returns the user's uid.
///
/// **Verification of the app itself, before any SMS is sent.** Firebase first proves the request
/// comes from your app: silently, via an APNs push, and if that is unavailable it falls back to a
/// reCAPTCHA page in the browser. Both paths need plumbing in `AppDelegate` —
/// `setAPNSToken`/`canHandleNotification` for the push, `Auth.auth().canHandle(url)` for the
/// reCAPTCHA callback. Without an APNs key uploaded to the Firebase console you always get the
/// reCAPTCHA detour.
final class PhoneAuthCoordinator {

    static let shared = PhoneAuthCoordinator()

    private init() {}

    /// Installs both handlers. Call once at startup, before the first Composable renders.
    func register() {
        PhoneAuthProviderIOS.shared.sendCodeHandler = { phoneNumber, completion in
            // The number must already be in E.164 (+34…); the library's phone screen composes it.
            PhoneAuthProvider.provider().verifyPhoneNumber(phoneNumber, uiDelegate: nil) { verificationID, error in
                if let error {
                    NSLog("%@", "[PhoneAuth] Could not send the code: \(error.localizedDescription)")
                    completion(nil)
                    return
                }
                completion(verificationID)
            }
        }

        PhoneAuthProviderIOS.shared.verifyCodeHandler = { verificationID, smsCode, completion in
            let credential = PhoneAuthProvider.provider()
                .credential(withVerificationID: verificationID, verificationCode: smsCode)

            Auth.auth().signIn(with: credential) { result, error in
                if let error {
                    NSLog("%@", "[PhoneAuth] OTP rejected: \(error.localizedDescription)")
                    completion(nil)
                    return
                }
                completion(result?.user.uid)
            }
        }
    }
}
