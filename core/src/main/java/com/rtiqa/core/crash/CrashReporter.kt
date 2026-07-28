package com.rtiqa.core.crash

import com.rtiqa.core.logging.RtiqaLog

/**
 * Interface contract for crash reporting and non-fatal exception tracking.
 */
interface CrashReporter {
    fun recordException(throwable: Throwable)
    fun setCustomKey(key: String, value: String)
    fun UserId(userId: String)
    fun logBreadcrumb(message: String)
}

/**
 * Safe production crash reporter implementation.
 */
class ProductionCrashReporter : CrashReporter {
    private val tag = "CrashReporter"

    override fun recordException(throwable: Throwable) {
        RtiqaLog.e(tag, "Non-fatal exception captured: ${throwable.localizedMessage}", throwable)
    }

    override fun setCustomKey(key: String, value: String) {
        RtiqaLog.d(tag, "Custom key attached: $key = $value")
    }

    override fun UserId(userId: String) {
        RtiqaLog.d(tag, "User identity bound to telemetry session: $userId")
    }

    override fun logBreadcrumb(message: String) {
        RtiqaLog.d(tag, "Breadcrumb: $message")
    }
}
