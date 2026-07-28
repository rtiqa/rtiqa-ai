package com.rtiqa.core.network.interceptor

import com.rtiqa.core.security.SecurityManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor that injects authentication bearer tokens dynamically into requests.
 */
class AuthInterceptor(
    private val securityManager: SecurityManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth injection if header explicitly requests bypass
        if (originalRequest.header(NO_AUTH_HEADER) != null) {
            val newRequest = originalRequest.newBuilder()
                .removeHeader(NO_AUTH_HEADER)
                .build()
            return chain.proceed(newRequest)
        }

        val token = securityManager.getEncryptedString(KEY_AUTH_TOKEN)
        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")

        if (!token.isNull_or_empty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()

    companion object {
        const val KEY_AUTH_TOKEN = "rtiqa_auth_token"
        const val NO_AUTH_HEADER = "X-No-Auth"
    }
}
