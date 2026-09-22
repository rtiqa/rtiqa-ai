package com.rtiqa.backend.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class LoginRequest(
    val email: String,
    val password: String? = null,
    val passwordHash: String? = null
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: UserDto,
    val organization_id: String? = null
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String? = null,
    val passwordHash: String? = null,
    val name: String? = null,
    val full_name: String? = null
)

@Serializable
data class RegisterResponse(
    val token: String,
    val user: UserDto,
    val organization_id: String? = null
)

@Serializable
data class AuthErrorResponse(
    val status: Int,
    val message: String
)

@Serializable
data class LogoutResponse(
    val message: String,
    val status: Int = 200
)

@Serializable
data class PasswordResetRequest(
    val email: String
)

@Serializable
data class PasswordResetResponse(
    val message: String,
    val status: Int = 200
)

@Serializable
data class SupabaseRecoverRequestBody(
    val email: String
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val role: String = "",
    val full_name: String? = null,
    val name: String? = null,
    val streakCount: Int = 1,
    val totalXp: Int = 0
)

// Supabase REST response models
@Serializable
data class SupabaseSignUpRequestBody(
    val email: String,
    val password: String,
    val data: Map<String, String>? = null
)

@Serializable
data class SupabaseAuthResponse(
    val access_token: String? = null,
    val token_type: String? = null,
    val expires_in: Int? = null,
    val refresh_token: String? = null,
    val user: SupabaseUser? = null,
    val id: String? = null,
    val email: String? = null
)

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
    val email: String? = null,
    val user_metadata: Map<String, JsonElement>? = null
)

@Serializable
data class SupabaseErrorResponse(
    val error: String? = null,
    val error_description: String? = null,
    val msg: String? = null
)
