import FirebaseAuth
import UIKit
import ComposeApp

/// GitHub, Microsoft, Twitter/X and Facebook — the four providers Firebase resolves through its
/// generic OAuth web flow, exactly like `WebOAuthProviderAndroid` does on the other platform.
///
/// These differ from Google and Apple in who signs in: here **Swift completes the whole Firebase
/// sign-in**, and the library only needs to be told that it happened. That is what the
/// `PLATFORM_AUTH_HANDLED` sentinel means — on receiving it the Kotlin side just refreshes the
/// session instead of building a credential of its own.
///
/// Needs the app's `REVERSED_CLIENT_ID` registered as a URL scheme and the callback forwarded to
/// `Auth.auth().canHandle(url)`; see `AppDelegate.handle(_:)`.
final class FirebaseOAuthCoordinator {

    static let shared = FirebaseOAuthCoordinator()

    /// The SDK does not retain the provider while the web flow runs: drop this reference and the
    /// credential callback never fires. Keyed by provider id so two flows cannot clobber each other.
    private var inFlight: [String: OAuthProvider] = [:]

    private init() {}

    /// Installs the four handlers. Call once at startup, before the first Composable renders.
    ///
    /// Scopes mirror the defaults of `LoginLibraryConfig`, so both platforms ask for the same thing.
    func register() {
        GitHubSignInProviderIOS.shared.signInHandler = { [weak self] _, completion in
            self?.signIn(
                providerId: "github.com",
                scopes: ["user:email"],
                handledSentinel: GitHubSignInProviderIOS.shared.PLATFORM_AUTH_HANDLED,
                completion: discardingResult(completion)
            )
        }

        MicrosoftSignInProviderIOS.shared.signInHandler = { [weak self] _, completion in
            self?.signIn(
                providerId: "microsoft.com",
                scopes: ["email", "profile"],
                handledSentinel: MicrosoftSignInProviderIOS.shared.PLATFORM_AUTH_HANDLED,
                completion: discardingResult(completion)
            )
        }

        TwitterSignInProviderIOS.shared.signInHandler = { [weak self] _, completion in
            self?.signIn(
                providerId: "twitter.com",
                scopes: ["email"],
                handledSentinel: TwitterSignInProviderIOS.shared.PLATFORM_AUTH_HANDLED,
                completion: discardingResult(completion)
            )
        }

        FacebookSignInProviderIOS.shared.signInHandler = { [weak self] _, completion in
            self?.signIn(
                providerId: "facebook.com",
                scopes: ["email", "public_profile"],
                handledSentinel: FacebookSignInProviderIOS.shared.PLATFORM_AUTH_HANDLED,
                completion: discardingResult(completion)
            )
        }
    }

    private func signIn(providerId: String,
                        scopes: [String],
                        handledSentinel: String,
                        completion: @escaping (String?) -> Void) {
        // The web flow presents UI, so it has to start on the main thread.
        DispatchQueue.main.async { [weak self] in
            guard let self else {
                completion(nil)
                return
            }
            guard self.inFlight[providerId] == nil else {
                NSLog("%@", "[FirebaseOAuth] \(providerId) is already in flight; rejecting this one.")
                completion(nil)
                return
            }

            let provider = OAuthProvider(providerID: providerId)
            provider.scopes = scopes
            self.inFlight[providerId] = provider

            // Called exactly once on every path below.
            let finish: (String?) -> Void = { [weak self] token in
                self?.inFlight.removeValue(forKey: providerId)
                completion(token)
            }

            provider.getCredentialWith(nil) { credential, error in
                if let error {
                    NSLog("%@", "[FirebaseOAuth] \(providerId) failed or was cancelled: \(error.localizedDescription)")
                    finish(nil)
                    return
                }
                guard let credential else {
                    NSLog("%@", "[FirebaseOAuth] \(providerId) returned no credential.")
                    finish(nil)
                    return
                }

                Auth.auth().signIn(with: credential) { _, error in
                    if let error {
                        NSLog("%@", "[FirebaseOAuth] \(providerId) sign-in rejected: \(error.localizedDescription)")
                        finish(nil)
                        return
                    }
                    finish(handledSentinel)
                }
            }
        }
    }
}
