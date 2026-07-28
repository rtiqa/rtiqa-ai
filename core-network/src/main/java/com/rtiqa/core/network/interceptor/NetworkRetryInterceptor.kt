package com.rtiqa.core.network.interceptor

import com.rtiqa.core.logging.RtiqaLog
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor that retries requests automatically on transient 5xx server errors or timeouts.
 */
class NetworkRetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 500L
) : Interceptor {

    private val tag = "NetworkRetry"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: Exception? = null
        var tryCount = 0

        while (tryCount < maxRetries) {
            try {
                tryCount++
                response = chain.proceed(request)
                if (response.isSuccessful || !isRetryableStatusCode(response.code)) {
                    return response
                }
                
                // If 5xx error, close response body before retrying
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

    private fun isRetryableStatusCode(code: Int): Boolean {
        return code in 500..599 || code == 429
    }
}
