package com.rtiqa.core.network

import com.rtiqa.core.network.api.RestApiService
import com.rtiqa.core.network.client.RestApiConfig
import com.rtiqa.core.network.interceptor.RestAuthInterceptor
import com.rtiqa.core.network.interceptor.RestRetryInterceptor
import com.rtiqa.core.network.interceptor.RestTenantInterceptor
import com.rtiqa.core.network.session.RestSessionStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Factory class for creating the canonical REST Retrofit and OkHttpClient instances.
 * Completely isolated from the legacy Firebase runtime infrastructure.
 */
object RestNetworkClient {
    private const val CONNECT_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 30L

    fun createMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    fun createOkHttpClient(
        sessionStore: RestSessionStore,
        isDebug: Boolean = false
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (isDebug) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            // Ensure we never log authorization headers for security
            redactHeader("Authorization")
        }

        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(RestAuthInterceptor(sessionStore))
            .addInterceptor(RestTenantInterceptor(sessionStore))
            .addInterceptor(RestRetryInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()
    }

    fun createApiService(
        okHttpClient: OkHttpClient,
        moshi: Moshi = createMoshi(),
        baseUrl: String = RestApiConfig.getBaseUrl()
    ): RestApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RestApiService::class.java)
    }
}
