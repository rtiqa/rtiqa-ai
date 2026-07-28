package com.rtiqa.core.logging

import android.util.Log

/**
 * Unified logging interface for Rtiqa application components.
 */
interface RtiqaLogger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

/**
 * Production implementation of [RtiqaLogger] with severity filtering and log sanitization.
 */
class ProductionLogger(
    private val isDebug: Boolean = false,
    private val minLogLevel: Int = Log.INFO
) : RtiqaLogger {

    override fun d(tag: String, message: String) {
        if (isDebug && Log.DEBUG >= minLogLevel) {
            Log.d(sanitizeTag(tag), sanitizeMessage(message))
        }
    }

    override fun i(tag: String, message: String) {
        if (Log.INFO >= minLogLevel) {
            Log.i(sanitizeTag(tag), sanitizeMessage(message))
        }
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        if (Log.WARN >= minLogLevel) {
            if (throwable != null) {
                Log.w(sanitizeTag(tag), sanitizeMessage(message), throwable)
            } else {
                Log.w(sanitizeTag(tag), sanitizeMessage(message))
            }
        }
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        if (Log.ERROR >= minLogLevel) {
            if (throwable != null) {
                Log.e(sanitizeTag(tag), sanitizeMessage(message), throwable)
            } else {
                Log.e(sanitizeTag(tag), sanitizeMessage(message))
            }
        }
    }

    private fun sanitizeTag(tag: String): String {
        return "Rtiqa_$tag".take(23)
    }

    private fun sanitizeMessage(message: String): String {
        // Redact potential auth token / API key patterns from log output
        return message.replace(Regex("(Bearer\\s+|key=)[A-Za-z0-9_.-]+"), "$1[REDACTED]")
    }
}

/**
 * Global singleton wrapper for app-wide logging access.
 */
object RtiqaLog : RtiqaLogger {
    @Volatile
    private var logger: RtiqaLogger = ProductionLogger(isDebug = true, minLogLevel = Log.VERBOSE)

    fun initialize(customLogger: RtiqaLogger) {
        logger = customLogger
    }

    override fun d(tag: String, message: String) = logger.d(tag, message)
    override fun i(tag: String, message: String) = logger.i(tag, message)
    override fun w(tag: String, message: String, throwable: Throwable?) = logger.w(tag, message, throwable)
    override fun e(tag: String, message: String, throwable: Throwable?) = logger.e(tag, message, throwable)
}
