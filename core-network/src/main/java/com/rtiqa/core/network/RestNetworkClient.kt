package com.rtiqa.core.network
import com.rtiqa.core.network.api.RestApiContract
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
class RestNetworkClient(private val sessionStore: RestSessionStore, isDebug: Boolean = false) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(RestAuthInterceptor(sessionStore))
            .addInterceptor(RestTenantInterceptor(sessionStore))
            .addInterceptor(RestRetryInterceptor())
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (isDebug) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .build()
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(RestApiConfig.getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
    
    val api: RestApiContract by lazy {
        retrofit.create(RestApiContract::class.java)
    }
}
