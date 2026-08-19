package com.apptolast.baselogin.presentation.util

import com.apptolast.baselogin.domain.model.AuthError
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Verifies that every [AuthError] variant maps to a distinct [StringResource].
 * Adding a new AuthError subtype without updating AuthErrorExt will cause the
 * `when` expression to fail at compile time (non-exhaustive), catching regressions early.
 */
class AuthErrorExtTest {

    private val allErrors: List<AuthError> = listOf(
        AuthError.InvalidCredentials(),
        AuthError.UserNotFound(),
        AuthError.EmailAlreadyInUse(),
        AuthError.WeakPassword(),
        AuthError.InvalidEmail(),
        AuthError.InvalidResetCode(),
        AuthError.NetworkError(),
        AuthError.TooManyRequests(),
        AuthError.UserDisabled(),
        AuthError.OperationNotAllowed(),
        AuthError.SessionExpired(),
        AuthError.RequiresEmailVerification(),
        AuthError.PhoneNumberInvalid(),
        AuthError.InvalidVerificationCode(),
        AuthError.Unknown(),
    )

    @Test
    fun `all AuthError variants return a non-null StringResource`() {
        allErrors.forEach { error ->
            assertNotNull(error.toStringRes(), "toStringRes() returned null for ${error::class.simpleName}")
        }
    }

    @Test
    fun `all AuthError variants map to distinct StringResources`() {
        val resources = allErrors.map { it.toStringRes() }
        val unique = resources.toSet()
        assertTrue(
            unique.size == allErrors.size,
            "Expected ${allErrors.size} distinct resources but got ${unique.size}. " +
                "Possible collision in AuthErrorExt.toStringRes()",
        )
    }

    @Test
    fun `InvalidCredentials and UserNotFound map to different resources`() {
        assertTrue(
            AuthError.InvalidCredentials().toStringRes() != AuthError.UserNotFound().toStringRes(),
        )
    }

    @Test
    fun `RequiresEmailVerification maps to a unique resource`() {
        val reqEmail = AuthError.RequiresEmailVerification().toStringRes()
        val others = allErrors
            .filter { it !is AuthError.RequiresEmailVerification }
            .map { it.toStringRes() }
        assertTrue(
            reqEmail !in others,
            "RequiresEmailVerification shares a StringResource with another error",
        )
    }

    @Test
    fun `Unknown maps to a unique resource`() {
        val unknown = AuthError.Unknown().toStringRes()
        val others = allErrors
            .filter { it !is AuthError.Unknown }
            .map { it.toStringRes() }
        assertTrue(
            unknown !in others,
            "Unknown shares a StringResource with another error",
        )
    }

    @Test
    fun `phone errors map to different resources`() {
        assertTrue(
            AuthError.PhoneNumberInvalid().toStringRes() != AuthError.InvalidVerificationCode().toStringRes(),
        )
    }
}
