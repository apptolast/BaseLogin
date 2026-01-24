import SwiftUI
import FirebaseCore
import FirebaseAppCheck
import GoogleSignIn
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {

        let providerFactory = AppCheckDebugProviderFactory()
        AppCheck.setAppCheckProviderFactory(providerFactory)

        FirebaseApp.configure()

        // Configure Google Sign-In handler for Kotlin
        configureGoogleSignIn()

        return true
    }

    // Handle URL callback for Google Sign-In
    func application(_ app: UIApplication,
                     open url: URL,
                     options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
        return GIDSignIn.sharedInstance.handle(url)
    }

    private func configureGoogleSignIn() {
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
        }
    }
}
