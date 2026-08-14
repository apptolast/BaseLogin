import AuthenticationServices
import CryptoKit
import Security
import UIKit
import ComposeApp

/// Native Sign in with Apple, wired to the Kotlin `AppleSignInProviderIOS` seam.
///
/// This is the reference integration: the library suspends its coroutine, this coordinator runs
/// AuthenticationServices, and the packed token string travels back through the completion block.
///
/// Requires the **Sign in with Apple** capability (see `iosApp.entitlements`) and the Apple provider
/// enabled in the Firebase console for this bundle id.
final class AppleSignInCoordinator: NSObject {

    static let shared = AppleSignInCoordinator()

    /// Raw (unhashed) nonce of the request in flight. Apple receives its SHA-256; Firebase receives
    /// this one and checks that both match, which is what makes a stolen identity token unusable.
    private var currentRawNonce: String?

    /// Completion handed over by Kotlin. Non-nil exactly while a request is in flight.
    private var completion: ((String?) -> Void)?

    /// `ASAuthorizationController` is not retained by the system: without this reference it is
    /// deallocated before the delegate fires and the sheet never appears.
    private var controller: ASAuthorizationController?

    private override init() {}

    /// Installs the handler. Call once at startup, before the first Composable renders.
    func register() {
        AppleSignInProviderIOS.shared.signInHandler = { [weak self] _, completion in
            self?.start(completion: completion)
        }
    }

    private func start(completion: @escaping (String?) -> Void) {
        // AuthenticationServices is main-thread only; the coroutine may resume anywhere.
        DispatchQueue.main.async { [weak self] in
            guard let self else {
                completion(nil)
                return
            }
            guard self.completion == nil else {
                NSLog("%@", "[AppleSignIn] A request is already in flight; rejecting this one.")
                completion(nil)
                return
            }
            guard let rawNonce = Self.randomNonceString() else {
                NSLog("%@", "[AppleSignIn] Could not generate a secure nonce; aborting sign-in.")
                completion(nil)
                return
            }

            self.completion = completion
            self.currentRawNonce = rawNonce

            let request = ASAuthorizationAppleIDProvider().createRequest()
            request.requestedScopes = [.fullName, .email]
            request.nonce = Self.sha256(rawNonce)

            let controller = ASAuthorizationController(authorizationRequests: [request])
            controller.delegate = self
            controller.presentationContextProvider = self
            self.controller = controller
            controller.performRequests()
        }
    }

    /// Single exit point: the Kotlin completion is called exactly once and the state is cleared.
    private func finish(_ token: String?) {
        let completion = self.completion
        self.completion = nil
        self.currentRawNonce = nil
        self.controller = nil
        completion?(token)
    }
}

// MARK: - ASAuthorizationControllerDelegate

extension AppleSignInCoordinator: ASAuthorizationControllerDelegate {

    func authorizationController(controller: ASAuthorizationController,
                                 didCompleteWithAuthorization authorization: ASAuthorization) {
        guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
              let tokenData = credential.identityToken,
              let idToken = String(data: tokenData, encoding: .utf8)
        else {
            NSLog("%@", "[AppleSignIn] Authorisation returned no usable identity token.")
            finish(nil)
            return
        }
        guard let rawNonce = currentRawNonce else {
            // Signing in without the nonce would drop replay protection: refuse instead.
            NSLog("%@", "[AppleSignIn] No raw nonce for this request; refusing the token.")
            finish(nil)
            return
        }

        // Apple returns the full name **only on the very first authorisation** of each user. The
        // library persists it into the Firebase profile; if it is not sent now it is lost for good.
        var packed = "\(idToken)|||rawNonce|||\(rawNonce)"
        if let displayName = Self.displayName(from: credential.fullName) {
            packed += "|||displayName|||\(displayName)"
        }
        finish(packed)
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        if let authError = error as? ASAuthorizationError, authError.code == .canceled {
            NSLog("%@", "[AppleSignIn] Cancelled by the user.")
        } else {
            NSLog("%@", "[AppleSignIn] Failed: \(error.localizedDescription)")
        }
        finish(nil)
    }
}

// MARK: - ASAuthorizationControllerPresentationContextProviding

extension AppleSignInCoordinator: ASAuthorizationControllerPresentationContextProviding {

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        let scenes = UIApplication.shared.connectedScenes.compactMap { $0 as? UIWindowScene }
        let scene = scenes.first { $0.activationState == .foregroundActive } ?? scenes.first
        return scene?.keyWindow ?? scene?.windows.first ?? UIWindow()
    }
}

// MARK: - Nonce

private extension AppleSignInCoordinator {

    /// Cryptographically secure nonce. Returns `nil` rather than falling back to a predictable
    /// value: a guessable nonce is worse than no sign-in.
    static func randomNonceString(length: Int = 32) -> String? {
        let charset = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        var result = ""
        result.reserveCapacity(length)

        while result.count < length {
            var randoms = [UInt8](repeating: 0, count: 16)
            guard SecRandomCopyBytes(kSecRandomDefault, randoms.count, &randoms) == errSecSuccess else {
                return nil
            }
            for random in randoms where result.count < length && random < charset.count {
                result.append(charset[Int(random)])
            }
        }
        return result
    }

    static func sha256(_ input: String) -> String {
        SHA256.hash(data: Data(input.utf8)).map { String(format: "%02x", $0) }.joined()
    }

    static func displayName(from components: PersonNameComponents?) -> String? {
        guard let components else { return nil }
        let formatter = PersonNameComponentsFormatter()
        formatter.style = .long
        let name = formatter.string(from: components).trimmingCharacters(in: .whitespacesAndNewlines)
        return name.isEmpty ? nil : name
    }
}
