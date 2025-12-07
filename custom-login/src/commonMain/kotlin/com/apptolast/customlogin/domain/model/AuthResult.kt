package com.apptolast.customlogin.domain.model

/**
 * A sealed interface representing the result of an authentication operation.
 * It can either be a [Success] or an [Error].
 */
interface AuthResult {

    /**
     * Represents a successful authentication.
     * @property user The authenticated user's data.
     */
    data class Success(val user: User) : AuthResult

    /**
     * Represents a failed authentication.
     * @property message A descriptive error message.
     * @property cause The optional exception that caused the failure.
     */
    data class Error(val message: String = "", val cause: Throwable? = null) : AuthResult
}
