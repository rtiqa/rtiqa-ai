package com.rtiqa.core.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Standardized API error response model for REST network requests.
 */
@JsonClass(generateAdapter = true)
data class RestApiError(
    @Json(name = "status") val status: Int,
    @Json(name = "message") val message: String,
    @Json(name = "code") val code: String? = null
)
