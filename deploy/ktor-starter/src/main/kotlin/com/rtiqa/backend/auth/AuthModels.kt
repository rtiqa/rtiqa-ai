package com.rtiqa.backend.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String? = null
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: UserDto,
    val organization_id: String? = null
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val role: String,
    val full_name: String? = null
)

// Supabase REST response models
@Serializable
data class SupabaseTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String,
    val user: SupabaseUser
)

@Serializable
data class SupabaseUser(
    val id: String,
    val email: String? = null
)

@Serializable
data class SupabaseErrorResponse(
    val error: String? = null,
    val error_description: String? = null,
    val msg: String? = null
)
