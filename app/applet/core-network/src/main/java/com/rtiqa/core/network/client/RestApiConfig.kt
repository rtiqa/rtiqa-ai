package com.rtiqa.core.network.client

import com.rtiqa.core.network.BuildConfig

/**
 * Provides the base URL for the canonical Node/Express REST API.
 */
object RestApiConfig {
    
    // Default safe URL for development (assumes a local or dev instance)
    private const val DEFAULT_DEV_URL = "https://api.dev.rtiqa.com/"
    
    private const val STAGING_URL = "https://api.staging.rtiqa.com/"
    private const val PRODUCTION_URL = "https://api.rtiqa.com/"

    fun getBaseUrl(): String {
        return when {
            BuildConfig.DEBUG -> DEFAULT_DEV_URL
            else -> PRODUCTION_URL
        }
    }
}
