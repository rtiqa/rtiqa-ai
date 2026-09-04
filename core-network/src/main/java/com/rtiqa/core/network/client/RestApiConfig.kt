package com.rtiqa.core.network.client
object RestApiConfig {
    private const val DEFAULT_DEV_URL = "https://api.dev.rtiqa.com/"
    private const val PRODUCTION_URL = "https://api.rtiqa.com/"
    fun getBaseUrl(): String {
        return PRODUCTION_URL // Fallback, could be overridden by DI
    }
}
