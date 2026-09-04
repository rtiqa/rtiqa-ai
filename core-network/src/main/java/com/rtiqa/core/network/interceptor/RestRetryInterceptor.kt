package com.rtiqa.core.network.interceptor
import com.rtiqa.core.logging.RtiqaLog
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
class RestRetryInterceptor(private val maxRetries: Int = 3, private val initialDelayMs: Long = 500L) : Interceptor {
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
                if (response.isSuccessful || !isRetryable(response.code, request)) return response
                response.close()
            } catch (e: IOException) {
                exception = e
                RtiqaLog.w(tag, "Attempt $tryCount failed with network exception: ${e.message}")
            }
            if (tryCount < maxRetries) {
                val backoffMs = initialDelayMs * (1 shl (tryCount - 1))
                try { Thread.sleep(backoffMs) } catch (e: InterruptedException) { Thread.currentThread().interrupt(); break }
            }
        }
        if (response != null) return response
        throw exception ?: IOException("Network request failed after $maxRetries attempts")
    }
    private fun isRetryable(code: Int, request: Request): Boolean {
        if (code in listOf(400, 401, 403, 404, 409, 422)) return false
        val isTransientError = code == 429 || code in 500..599
        if (!isTransientError) return false
        val method = request.method
        if (method == "GET" || method == "PUT" || method == "DELETE") return true
        if (method == "POST") return request.header("Idempotency-Key") != null
        return false
    }
}
