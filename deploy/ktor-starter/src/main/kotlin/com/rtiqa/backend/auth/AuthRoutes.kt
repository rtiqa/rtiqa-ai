package com.rtiqa.backend.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authClient: SupabaseAuthClient) {
    route("/api/v1/auth") {
        post("/login") {
            val request = try {
                call.receive<LoginRequest>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("status" to 400, "message" to "Malformed login request"))
                return@post
            }

            if (request.email.isBlank() || request.password.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("status" to 400, "message" to "Email and password are required"))
                return@post
            }

            val result = authClient.login(request)
            
            if (result.isSuccess) {
                val tokenResponse = result.getOrNull()!!
                
                val loginResponse = LoginResponse(
                    token = tokenResponse.access_token,
                    user = UserDto(
                        id = tokenResponse.user.id,
                        email = tokenResponse.user.email ?: request.email,
                        role = "", // Mock role removed. Actual roles are tenant-specific and derived per request via X-Tenant-ID.
                        full_name = null // Needs to be fetched from profiles if required by Android
                    ),
                    organization_id = null
                )
                call.respond(HttpStatusCode.OK, loginResponse)
            } else {
                call.respond(
                    HttpStatusCode.Unauthorized, 
                    mapOf("status" to 401, "message" to (result.exceptionOrNull()?.message ?: "Invalid credentials"))
                )
            }
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
        }
    }
}
