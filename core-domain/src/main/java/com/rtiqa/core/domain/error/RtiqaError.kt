package com.rtiqa.core.domain.error

/**
 * Domain error hierarchy representing business and infrastructure failure states.
 */
sealed class RtiqaError(open val message: String, open val cause: Throwable? = null) {

    data class NetworkError(
        override val message: String = "حدث خطأ في الاتصال بالشبكة. يرجى التحقق من اتصالك.",
        val statusCode: Int? = null,
        override val cause: Throwable? = null
    ) : RtiqaError(message, cause)

    data class DatabaseError(
        override val message: String = "حدث خطأ في قاعدة البيانات المحلية.",
        override val cause: Throwable? = null
    ) : RtiqaError(message, cause)

    data class AuthError(
        override val message: String = "فشلت المصادقة أو انتهت صلاحية الجلسة.",
        val errorCode: String? = null,
        override val cause: Throwable? = null
    ) : RtiqaError(message, cause)

    data class ValidationError(
        val errors: List<String>,
        override val message: String = errors.joinToString(", ")
    ) : RtiqaError(message)

    data class AiServiceError(
        override val message: String = "تعذر على محرك الذكاء الاصطناعي معالجة الطلب.",
        override val cause: Throwable? = null
    ) : RtiqaError(message, cause)

    data class SyncError(
        override val message: String = "فشلت المزامنة بدون إنترنت.",
        override val cause: Throwable? = null
    ) : RtiqaError(message, cause)

    data class UnknownError(
        override val message: String = "حدث خطأ غير متوقع.",
        override val cause: Throwable? = null
    ) : RtiqaError(message, cause)
}
