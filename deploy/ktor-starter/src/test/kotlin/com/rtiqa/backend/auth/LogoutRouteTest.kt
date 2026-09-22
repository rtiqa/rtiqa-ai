package com.rtiqa.backend.auth

import com.auth0.jwt.interfaces.Payload
import com.rtiqa.backend.config.SupabaseConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.install
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.routing.routing
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy

class LogoutRouteTest {

    private fun ApplicationTestBuilder.setupRealSecurityApp(authClient: SupabaseAuthClient) {
        val config = SupabaseConfig("https://example.supabase.co", "sb_publishable_test_value")
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureSecurity(config)
            routing {
                authRoutes(authClient)
            }
        }
    }

    private fun ApplicationTestBuilder.setupAuthenticatedApp(authClient: SupabaseAuthClient) {
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Authentication) {
                provider("auth-jwt") {
                    authenticate { context ->
                        val authHeader = context.call.request.headers[HttpHeaders.Authorization]
                        if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.removePrefix("Bearer ").isNotBlank()) {
                            val token = authHeader.removePrefix("Bearer ").trim()
                            if (token != "invalid-token" && token != "garbage-token") {
                                val mockPayload = Proxy.newProxyInstance(
                                    Payload::class.java.classLoader,
                                    arrayOf(Payload::class.java)
                                ) { _, method, _ ->
                                    if (method?.name == "getSubject") "test-user-id" else null
                                } as Payload
                                context.principal(JWTPrincipal(mockPayload))
                            }
                        }
                    }
                }
            }
            routing {
                authRoutes(authClient)
            }
        }
    }

    private class FakeLogoutSupabaseAuthClient(
        private val logoutHandler: suspend (String) -> Result<Unit>
    ) : SupabaseAuthClient(SupabaseConfig("https://example.supabase.co", "test-key")) {
        override suspend fun logout(token: String): Result<Unit> {
            return logoutHandler(token)
        }
    }

    @Test
    fun `No Authorization header returns 401 on logout`() = testApplication {
        val clientAuth = FakeLogoutSupabaseAuthClient { Result.success(Unit) }
        setupRealSecurityApp(clientAuth)

        val response = client.post("/api/v1/auth/logout")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `Basic header returns 401 on logout`() = testApplication {
        val clientAuth = FakeLogoutSupabaseAuthClient { Result.success(Unit) }
        setupRealSecurityApp(clientAuth)

        val response = client.post("/api/v1/auth/logout") {
            header(HttpHeaders.Authorization, "Basic dXNlcjpwYXNz")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `Malformed Bearer returns 401 on logout`() = testApplication {
        val clientAuth = FakeLogoutSupabaseAuthClient { Result.success(Unit) }
        setupRealSecurityApp(clientAuth)

        val response = client.post("/api/v1/auth/logout") {
            header(HttpHeaders.Authorization, "Bearer garbage-token")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `Successful logout with valid bearer token returns 200 OK`() = testApplication {
        var tokenLoggedOut = ""
        val clientAuth = FakeLogoutSupabaseAuthClient { token ->
            tokenLoggedOut = token
            Result.success(Unit)
        }
        setupAuthenticatedApp(clientAuth)

        val response = client.post("/api/v1/auth/logout") {
            header(HttpHeaders.Authorization, "Bearer valid-jwt-token-xyz")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("valid-jwt-token-xyz", tokenLoggedOut)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("Successfully logged out"), "Should contain logout success message")
    }

    @Test
    fun `Logout when Supabase fails returns 400 Bad Request`() = testApplication {
        val clientAuth = FakeLogoutSupabaseAuthClient {
            Result.failure(Exception("Supabase token revocation failed"))
        }
        setupAuthenticatedApp(clientAuth)

        val response = client.post("/api/v1/auth/logout") {
            header(HttpHeaders.Authorization, "Bearer valid-jwt-token-xyz")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("Supabase token revocation failed"))
    }
}
