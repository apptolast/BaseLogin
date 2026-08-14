import SwiftUI
import FirebaseCore
import FirebaseAuth
import FirebaseAppCheck
import GoogleSignIn
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {

        let providerFactory = AppCheckDebugProviderFactory()
        AppCheck.setAppCheckProviderFactory(providerFactory)

        FirebaseApp.configure()

        // Configure the sign-in handlers Kotlin calls into, before any Composable renders.
        configureGoogleSignIn()
        AppleSignInCoordinator.shared.register()
        FirebaseOAuthCoordinator.shared.register()
        PhoneAuthCoordinator.shared.register()

        return true
    }

    /// Routes an incoming URL to whoever is waiting for it: Firebase (the OAuth web flow of
    /// GitHub/Microsoft/Twitter/Facebook, and the phone reCAPTCHA fallback) or Google Sign-In.
    ///
    /// Under the SwiftUI lifecycle this delegate method is **not** called — URLs arrive at the
    /// scene — so the live entry point is `.onOpenURL` below. It is kept for hosts that still run
    /// a UIKit app delegate without scenes.
    static func handle(_ url: URL) -> Bool {
        if Auth.auth().canHandle(url) {
            return true
        }
        return GIDSignIn.sharedInstance.handle(url)
    }

    func application(_ app: UIApplication,
                     open url: URL,
                     options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
        return AppDelegate.handle(url)
    }

    // ── Phone auth: silent APNs verification ─────────────────────────────────
    // Firebase proves the request comes from this app before sending any SMS. It asks for the APNs
    // token itself; these two callbacks are what let it use the push instead of falling back to the
    // reCAPTCHA page. They need the Push Notifications capability and an APNs key in the Firebase
    // console — without them the fallback still works, it is just a browser detour for the user.

    func application(_ application: UIApplication,
                     didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Auth.auth().setAPNSToken(deviceToken, type: .unknown)
    }

    func application(_ application: UIApplication,
                     didFailToRegisterForRemoteNotificationsWithError error: Error) {
        NSLog("%@", "[PhoneAuth] No APNs token, reCAPTCHA fallback will be used: \(error.localizedDescription)")
    }

    func application(_ application: UIApplication,
                     didReceiveRemoteNotification userInfo: [AnyHashable: Any],
                     fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        if Auth.auth().canHandleNotification(userInfo) {
            completionHandler(.noData)
            return
        }
        // A host with its own push payloads branches here; the demo has none.
        completionHandler(.noData)
    }

    private func configureGoogleSignIn() {
        // Firebase's signOut() does not touch GIDSignIn: its currentUser lives in the keychain, and
        // without this the next sign-in reuses the same account and nobody can switch.
        GoogleSignInProviderIOS.Companion.shared.signOutHandler = {
            GIDSignIn.sharedInstance.signOut()
        }

        // Set up the Google Sign-In handler that Kotlin will call
        GoogleSignInProviderIOS.Companion.shared.signInHandler = { clientId, completion in
            guard let clientId = clientId else {
                completion(nil)
                return
            }

            // Configure GIDSignIn with the client ID
            let config = GIDConfiguration(clientID: clientId)
            GIDSignIn.sharedInstance.configuration = config

            // Get the presenting view controller
            guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                  let rootViewController = windowScene.windows.first?.rootViewController
            else {
                print("No root view controller found")
                completion(nil)
                return
            }

            // Find the top-most view controller
            var topController = rootViewController
            while let presented = topController.presentedViewController {
                topController = presented
            }

            // Perform sign-in
            GIDSignIn.sharedInstance.signIn(withPresenting: topController) { result, error in
                if let error = error {
                    print("Google Sign-In error: \(error.localizedDescription)")
                    completion(nil)
                    return
                }

                guard let user = result?.user,
                      let idToken = user.idToken?.tokenString
                else {
                    print("No ID token received from Google Sign-In")
                    completion(nil)
                    return
                }

                // Get the access token (required by Firebase on iOS)
                let accessToken = user.accessToken.tokenString

                // Return both tokens separated by delimiter for Kotlin to parse
                let combinedTokens = "\(idToken)|||accessToken|||\(accessToken)"
                completion(combinedTokens)
            }
        }
    }
}

@main
struct iOSApp: App {
    // register app delegate for Firebase setup
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
                // Where OAuth and reCAPTCHA callbacks actually land under the SwiftUI lifecycle.
                .onOpenURL { url in
                    _ = AppDelegate.handle(url)
                }
        }
    }
}
