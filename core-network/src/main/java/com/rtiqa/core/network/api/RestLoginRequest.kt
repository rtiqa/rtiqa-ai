package com.rtiqa.core.network.api
import com.squareup.moshi.JsonClass
@JsonClass(generateAdapter = true)
data class RestLoginRequest(
    val email: String,
    val password: String? = null,
    val auth_token: String? = null
)
@JsonClass(generateAdapter = true)
data class RestLoginResponse(
    val token: String,
    val user: RestUserDto,
    val organization_id: String?
)
@JsonClass(generateAdapter = true)
data class RestUserDto(
    val id: String,
    val email: String,
    val role: String,
    val full_name: String?
)
@JsonClass(generateAdapter = true)
data class RestCourseDto(
    val id: String,
    val title: String,
    val description: String?,
    val is_published: Boolean
)
