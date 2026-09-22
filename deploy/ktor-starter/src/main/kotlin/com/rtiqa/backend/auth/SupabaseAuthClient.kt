package com.rtiqa.backend.auth

import com.rtiqa.backend.config.SupabaseConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

open class SupabaseAuthClient(private val config: SupabaseConfig) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    open suspend fun login(request: LoginRequest): Result<SupabaseTokenResponse> {
        val password = (request.password ?: request.passwordHash)?.trim()
        if (password.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Password is required"))
        }

        return try {
            val response = client.post("${config.url}/auth/v1/token?grant_type=password") {
                contentType(ContentType.Application.Json)
                header("apikey", config.publishableKey)
                setBody(mapOf("email" to request.email.trim(), "password" to password))
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                var errorMsg = "Authentication failed"
                try {
                    val parsedError = Json { ignoreUnknownKeys = true }.decodeFromString<SupabaseErrorResponse>(errorBody)
                    errorMsg = parsedError.error_description ?: parsedError.msg ?: parsedError.error ?: errorMsg
                } catch (e: Exception) {
                    // Ignore JSON parsing errors for error body
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Service unavailable: ${e.message}"))
        }
    }

    open suspend fun register(request: RegisterRequest): Result<SupabaseAuthResponse> {
        val password = (request.password ?: request.passwordHash)?.trim()
        if (password.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Password is required"))
        }

        return try {
            val userMetadata = mutableMapOf<String, String>()
            val name = (request.name ?: request.full_name)?.trim()
            if (!name.isNullOrBlank()) {
                userMetadata["name"] = name
                userMetadata["full_name"] = name
            }

            val requestBody = SupabaseSignUpRequestBody(
                email = request.email.trim(),
                password = password,
                data = if (userMetadata.isNotEmpty()) userMetadata else null
            )

            val response = client.post("${config.url}/auth/v1/signup") {
                contentType(ContentType.Application.Json)
                header("apikey", config.publishableKey)
                setBody(requestBody)
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                var errorMsg = "Registration failed"
                try {
                    val parsedError = Json { ignoreUnknownKeys = true }.decodeFromString<SupabaseErrorResponse>(errorBody)
                    errorMsg = parsedError.error_description ?: parsedError.msg ?: parsedError.error ?: errorMsg
                } catch (e: Exception) {
                    // Ignore JSON parsing errors for error body
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Service unavailable: ${e.message}"))
        }
    }

    open suspend fun logout(token: String): Result<Unit> {
        val trimmedToken = token.trim()
        if (trimmedToken.isBlank()) {
            return Result.failure(IllegalArgumentException("Token is required"))
        }

        return try {
            val response = client.post("${config.url}/auth/v1/logout") {
                header("apikey", config.publishableKey)
                header(HttpHeaders.Authorization, "Bearer $trimmedToken")
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val errorBody = response.bodyAsText()
                var errorMsg = "Logout failed"
                try {
                    val parsedError = Json { ignoreUnknownKeys = true }.decodeFromString<SupabaseErrorResponse>(errorBody)
                    errorMsg = parsedError.error_description ?: parsedError.msg ?: parsedError.error ?: errorMsg
                } catch (e: Exception) {
                    // Ignore JSON parsing errors for error body
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Service unavailable: ${e.message}"))
        }
    }

    open suspend fun resetPassword(email: String): Result<Unit> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            return Result.failure(IllegalArgumentException("Email is required"))
        }

        return try {
            val response = client.post("${config.url}/auth/v1/recover") {
                contentType(ContentType.Application.Json)
                header("apikey", config.publishableKey)
                setBody(SupabaseRecoverRequestBody(email = trimmedEmail))
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val errorBody = response.bodyAsText()
                var errorMsg = "Password reset request failed"
                try {
                    val parsedError = Json { ignoreUnknownKeys = true }.decodeFromString<SupabaseErrorResponse>(errorBody)
                    errorMsg = parsedError.error_description ?: parsedError.msg ?: parsedError.error ?: errorMsg
                } catch (e: Exception) {
                    // Ignore JSON parsing errors for error body
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Service unavailable: ${e.message}"))
        }
    }
}
