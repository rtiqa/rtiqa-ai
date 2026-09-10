package com.rtiqa.core.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Standardized API error response model for REST network requests.
 */
@JsonClass(generateAdapter = true)
data class RestApiError(
    @field:Json(name = "status") val status: Int,
    @field:Json(name = "message") val message: String,
    @field:Json(name = "code") val code: String? = null
)
