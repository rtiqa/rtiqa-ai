package com.rtiqa.core.network.interceptor

import com.rtiqa.core.network.session.RestSessionStore
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor that injects the active tenant context header.
 */
class RestTenantInterceptor(
    private val sessionStore: RestSessionStore
) : Interceptor {

    companion object {
        const val HEADER_TENANT_ID = "X-Tenant-Id"
        const val NO_TENANT_HEADER = "X-No-Tenant"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (originalRequest.header(NO_TENANT_HEADER) != null) {
            val newRequest = originalRequest.newBuilder()
                .removeHeader(NO_TENANT_HEADER)
                .build()
            return chain.proceed(newRequest)
        }

        val requestBuilder = originalRequest.newBuilder()

        val tenantId = sessionStore.getActiveOrganizationId()
        if (!tenantId.isNullOrBlank()) {
            requestBuilder.header(HEADER_TENANT_ID, tenantId)
        }

        return chain.proceed(requestBuilder.build())
    }
}
