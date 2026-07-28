package com.rtiqa.core.domain.error

/**
 * Domain error hierarchy representing business and infrastructure failure states.
 */
sealed class RtiqaError(open val message: String, open val cause: Throwable? = null) {

    data class NetworkError(
        override val message: String = "A network error occurred. Please check your connection.",
        val statusCode: Int? = null,
        override val cause: Throwable? = null
    ) : RtiqaError(message, cause)

    data class DatabaseError(
        override val message: String = "A local database error occurred.",
        override val cause: Throwable? = null
    ) : RtiqaError(message, cause)

    data class AuthError(
        override val message: String = "Authentication failed or token expired.",
        val errorCode: String? = null,
        override val cause: Throwable? = null
    ) : RtiqaError(message, cause)

    data class ValidationError(
        val errors: List<String>,
        override val message: String = errors.joinToString(", ")
    ) : RtiqaError(message)

    data class AiServiceError(
        override val message: String = "AI engine was unable to process request.",
        override val cause: Throwable? = null
    ) : RtiqaError(message, cause)

    data class SyncError(
        override val message: String = "Offline synchronization failed.",
        override val cause: Throwable? = null
    ) : RtiqaError(message, cause)

    data class UnknownError(
        override val message: String = "An unexpected error occurred.",
        override val cause: Throwable? = null
    ) : RtiqaError(message, cause)
}
