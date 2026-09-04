package com.rtiqa.core.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RestApiError(
    @Json(name = "code") val code: String?,
    @Json(name = "message") val message: String?
)

class RestApiException(
    val statusCode: Int,
    val apiError: RestApiError?,
    override val message: String = apiError?.message ?: "Unknown API Error (HTTP $statusCode)"
) : Exception(message)
