package com.rtiqa.core.network.interceptor
import com.rtiqa.core.network.session.RestSessionStore
import okhttp3.Interceptor
import okhttp3.Response
class RestAuthInterceptor(private val sessionStore: RestSessionStore) : Interceptor {
    companion object { const val NO_AUTH_HEADER = "X-No-Auth" }
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        if (originalRequest.header(NO_AUTH_HEADER) != null) {
            val newRequest = originalRequest.newBuilder().removeHeader(NO_AUTH_HEADER).build()
            return chain.proceed(newRequest)
        }
        val requestBuilder = originalRequest.newBuilder().header("Accept", "application/json").header("Content-Type", "application/json")
        val token = sessionStore.getSessionToken()
        if (!token.isNullOrBlank()) requestBuilder.header("Authorization", "Bearer $token")
        return chain.proceed(requestBuilder.build())
    }
}
