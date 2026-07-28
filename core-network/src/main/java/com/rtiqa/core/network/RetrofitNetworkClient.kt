package com.rtiqa.core.network

import com.rtiqa.core.network.api.RtiqaApiService
import com.rtiqa.core.network.interceptor.AuthInterceptor
import com.rtiqa.core.network.interceptor.NetworkRetryInterceptor
import com.rtiqa.core.security.SecurityManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Factory class for creating configured Retrofit and OkHttpClient instances.
 */
object RetrofitNetworkClient {

    private const val DEFAULT_BASE_URL = "https://api.rtiqa.com/"
    private const val CONNECT_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 30L

    fun createMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    fun createOkHttpClient(
        securityManager: SecurityManager,
        isDebug: Boolean = false
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (isDebug) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(securityManager))
            .addInterceptor(NetworkRetryInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()
    }

    fun createApiService(
        okHttpClient: OkHttpClient,
        moshi: Moshi = createMoshi(),
        baseUrl: String = DEFAULT_BASE_URL
    ): RtiqaApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RtiqaApiService::class.java)
    }
}
