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

class SupabaseAuthClient(private val config: SupabaseConfig) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun login(request: LoginRequest): Result<SupabaseTokenResponse> {
        return try {
            val response = client.post("${config.url}/auth/v1/token?grant_type=password") {
                contentType(ContentType.Application.Json)
                header("apikey", config.anonKey)
                setBody(mapOf("email" to request.email, "password" to request.password))
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
}
