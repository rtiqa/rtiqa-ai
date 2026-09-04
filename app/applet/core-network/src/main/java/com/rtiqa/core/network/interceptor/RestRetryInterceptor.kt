package com.rtiqa.core.network.interceptor

import com.rtiqa.core.logging.RtiqaLog
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor that retries requests automatically on transient server errors or timeouts.
 * Adheres to strict safety boundaries for non-idempotent operations.
 */
class RestRetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 500L
) : Interceptor {
    
    private val tag = "RestRetryInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: Exception? = null
        var tryCount = 0

        while (tryCount < maxRetries) {
            try {
                tryCount++
                response = chain.proceed(request)
                
                // Success or non-retryable status
                if (response.isSuccessful || !isRetryable(response.code, request)) {
                    return response
                }
                
                // If retryable, close the response body to avoid leaks before retrying
                response.close()
            } catch (e: IOException) {
                exception = e
                RtiqaLog.w(tag, "Attempt $tryCount failed with network exception: ${e.message}")
            }

            if (tryCount < maxRetries) {
                val backoffMs = initialDelayMs * (1 shl (tryCount - 1))
                try {
                    Thread.sleep(backoffMs)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
            }
        }

        if (response != null) {
            return response
        }
        throw exception ?: IOException("Network request failed after $maxRetries attempts")
    }

    private fun isRetryable(code: Int, request: Request): Boolean {
        // Do not retry specific client or unrecoverable errors
        if (code in listOf(400, 401, 403, 404, 409, 422)) {
            return false
        }
        
        // Retry rate limits (429) and server errors (500, 502, 503, 504)
        val isTransientError = code == 429 || code in 500..599
        
        if (!isTransientError) {
            return false
        }
        
        val method = request.method
        if (method == "GET" || method == "PUT" || method == "DELETE") {
            return true
        }
        
        // Never blindly retry POST unless an idempotency key exists
        if (method == "POST") {
            val hasIdempotencyKey = request.header("Idempotency-Key") != null
            return hasIdempotencyKey
        }
        
        return false
    }
}
