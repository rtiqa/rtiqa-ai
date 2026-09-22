package com.rtiqa.backend.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

fun Route.authRoutes(authClient: SupabaseAuthClient) {
    route("/api/v1/auth") {
        post("/login") {
            val request = try {
                call.receive<LoginRequest>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, AuthErrorResponse(400, "Malformed login request"))
                return@post
            }

            val actualPassword = (request.password ?: request.passwordHash)?.trim()
            if (request.email.isBlank() || actualPassword.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, AuthErrorResponse(400, "Email and password are required"))
                return@post
            }

            val result = authClient.login(request)
            
            if (result.isSuccess) {
                val tokenResponse = result.getOrNull()!!
                val userEmail = tokenResponse.user.email ?: request.email.trim()
                val derivedName = tokenResponse.user.user_metadata?.get("name")?.jsonPrimitive?.contentOrNull
                    ?: tokenResponse.user.user_metadata?.get("full_name")?.jsonPrimitive?.contentOrNull
                    ?: userEmail.substringBefore("@")
                
                val loginResponse = LoginResponse(
                    token = tokenResponse.access_token,
                    user = UserDto(
                        id = tokenResponse.user.id,
                        email = userEmail,
                        role = "", // Mock role removed. Actual roles are tenant-specific and derived per request via X-Tenant-ID.
                        full_name = derivedName,
                        name = derivedName,
                        streakCount = 1,
                        totalXp = 0
                    ),
                    organization_id = null
                )
                call.respond(HttpStatusCode.OK, loginResponse)
            } else {
                call.respond(
                    HttpStatusCode.Unauthorized, 
                    AuthErrorResponse(401, result.exceptionOrNull()?.message ?: "Invalid credentials")
                )
            }
        }

        post("/register") {
            val request = try {
                call.receive<RegisterRequest>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, AuthErrorResponse(400, "Malformed registration request"))
                return@post
            }

            val actualPassword = (request.password ?: request.passwordHash)?.trim()
            if (request.email.isBlank() || actualPassword.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, AuthErrorResponse(400, "Email and password are required"))
                return@post
            }

            val result = authClient.register(request)

            if (result.isSuccess) {
                val authResponse = result.getOrNull()!!
                val userId = authResponse.user?.id ?: authResponse.id ?: ""
                val userEmail = authResponse.user?.email ?: authResponse.email ?: request.email.trim()
                val displayName = (request.name ?: request.full_name)?.trim()?.ifBlank { null }
                val token = authResponse.access_token ?: ""

                val registerResponse = RegisterResponse(
                    token = token,
                    user = UserDto(
                        id = userId,
                        email = userEmail,
                        role = "",
                        full_name = displayName,
                        name = displayName,
                        streakCount = 1,
                        totalXp = 0
                    ),
                    organization_id = null
                )
                call.respond(HttpStatusCode.Created, registerResponse)
            } else {
                call.respond(
                    HttpStatusCode.BadRequest,
                    AuthErrorResponse(400, result.exceptionOrNull()?.message ?: "Registration failed")
                )
            }
        }

        post("/reset-password") {
            handlePasswordReset(call, authClient)
        }

        post("/forgot-password") {
            handlePasswordReset(call, authClient)
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject
                val email = principal?.payload?.getClaim("email")?.asString()

                if (userId != null) {
                    call.respond(HttpStatusCode.OK, mapOf(
                        "user_id" to userId,
                        "email" to (email ?: "unknown")
                    ))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("status" to 401, "message" to "Invalid token structure"))
                }
            }

            post("/logout") {
                val authHeader = call.request.header(HttpHeaders.Authorization)
                val token = authHeader?.removePrefix("Bearer ")?.trim()
                if (token.isNullOrBlank()) {
                    call.respond(HttpStatusCode.Unauthorized, AuthErrorResponse(401, "Missing or invalid token"))
                    return@post
                }

                val result = authClient.logout(token)

                if (result.isSuccess) {
                    call.respond(HttpStatusCode.OK, LogoutResponse(message = "Successfully logged out"))
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        AuthErrorResponse(400, result.exceptionOrNull()?.message ?: "Logout failed")
                    )
                }
            }
        }
    }
}

private suspend fun handlePasswordReset(call: ApplicationCall, authClient: SupabaseAuthClient) {
    val request = try {
        call.receive<PasswordResetRequest>()
    } catch (e: Exception) {
        call.respond(HttpStatusCode.BadRequest, AuthErrorResponse(400, "Malformed password reset request"))
        return
    }

    val trimmedEmail = request.email.trim()
    if (trimmedEmail.isBlank() || !trimmedEmail.contains("@") || !trimmedEmail.substringAfter("@").contains(".")) {
        call.respond(HttpStatusCode.BadRequest, AuthErrorResponse(400, "Valid email is required"))
        return
    }

    val result = authClient.resetPassword(trimmedEmail)

    val genericSuccessMessage = "If an account exists with this email, password reset instructions have been sent"

    if (result.isSuccess) {
        call.respond(HttpStatusCode.OK, PasswordResetResponse(message = genericSuccessMessage))
    } else {
        val errorMsg = result.exceptionOrNull()?.message ?: "Password reset failed"
        val lowerError = errorMsg.lowercase()

        // Prevent User Enumeration: if error indicates user/email was not found, return generic success
        if (lowerError.contains("user not found") ||
            lowerError.contains("email not found") ||
            lowerError.contains("user not registered") ||
            lowerError.contains("not found")) {
            call.respond(HttpStatusCode.OK, PasswordResetResponse(message = genericSuccessMessage))
        } else if (lowerError.contains("rate limit") || lowerError.contains("too many requests") || lowerError.contains("over_email_send_rate_limit")) {
            call.respond(HttpStatusCode.TooManyRequests, AuthErrorResponse(429, "Too many requests. Please try again later."))
        } else {
            call.respond(HttpStatusCode.BadRequest, AuthErrorResponse(400, errorMsg))
        }
    }
}
