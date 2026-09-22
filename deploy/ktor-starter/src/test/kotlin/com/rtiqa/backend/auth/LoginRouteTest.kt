package com.rtiqa.backend.auth

import com.rtiqa.backend.config.SupabaseConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.routing
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LoginRouteTest {

    private fun ApplicationTestBuilder.setupApp(authClient: SupabaseAuthClient) {
        val config = SupabaseConfig("https://example.supabase.co", "sb_publishable_test_value")
        application {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            configureSecurity(config)
            routing {
                authRoutes(authClient)
            }
        }
    }

    private class FakeLoginSupabaseAuthClient(
        private val loginHandler: suspend (LoginRequest) -> Result<SupabaseTokenResponse>
    ) : SupabaseAuthClient(SupabaseConfig("https://example.supabase.co", "test-key")) {
        override suspend fun login(request: LoginRequest): Result<SupabaseTokenResponse> {
            return loginHandler(request)
        }
    }

    @Test
    fun `Successful login with password returns 200 OK with token and user dto`() = testApplication {
        val fakeClient = FakeLoginSupabaseAuthClient { req ->
            Result.success(
                SupabaseTokenResponse(
                    access_token = "mock-jwt-login-token-12345",
                    token_type = "bearer",
                    expires_in = 3600,
                    refresh_token = "refresh-xyz",
                    user = SupabaseUser(
                        id = "user-uuid-111",
                        email = req.email,
                        user_metadata = mapOf("name" to JsonPrimitive("Ahmad Al-Mansoor"))
                    )
                )
            )
        }
        setupApp(fakeClient)

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "ahmad@rtiqa.com", "password": "securePass123"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("mock-jwt-login-token-12345"), "Response should contain access token")
        assertTrue(bodyText.contains("user-uuid-111"), "Response should contain user id")
        assertTrue(bodyText.contains("ahmad@rtiqa.com"), "Response should contain user email")
        assertTrue(bodyText.contains("Ahmad Al-Mansoor"), "Response should contain derived name")
    }

    @Test
    fun `Successful login with passwordHash (Android compatibility) returns 200 OK`() = testApplication {
        var receivedPassword = ""
        val fakeClient = FakeLoginSupabaseAuthClient { req ->
            receivedPassword = req.password ?: req.passwordHash ?: ""
            Result.success(
                SupabaseTokenResponse(
                    access_token = "mock-android-token-67890",
                    token_type = "bearer",
                    expires_in = 3600,
                    refresh_token = "refresh-android",
                    user = SupabaseUser(
                        id = "user-uuid-222",
                        email = req.email
                    )
                )
            )
        }
        setupApp(fakeClient)

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "student@rtiqa.com", "passwordHash": "hashed_login_pass"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("hashed_login_pass", receivedPassword)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("mock-android-token-67890"))
        assertTrue(bodyText.contains("student"), "Should fallback to email prefix when metadata is absent")
    }

    @Test
    fun `Login with blank email returns 400 Bad Request`() = testApplication {
        val fakeClient = FakeLoginSupabaseAuthClient {
            Result.failure(Exception("Not called"))
        }
        setupApp(fakeClient)

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "   ", "password": "pass"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("Email and password are required"))
    }

    @Test
    fun `Login with blank password returns 400 Bad Request`() = testApplication {
        val fakeClient = FakeLoginSupabaseAuthClient {
            Result.failure(Exception("Not called"))
        }
        setupApp(fakeClient)

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "test@rtiqa.com", "password": ""}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("Email and password are required"))
    }

    @Test
    fun `Login with invalid credentials returns 401 Unauthorized`() = testApplication {
        val fakeClient = FakeLoginSupabaseAuthClient {
            Result.failure(Exception("Invalid login credentials"))
        }
        setupApp(fakeClient)

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "wrong@rtiqa.com", "password": "wrongpassword"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("Invalid login credentials"))
    }

    @Test
    fun `Malformed login JSON returns 400 Bad Request`() = testApplication {
        val fakeClient = FakeLoginSupabaseAuthClient {
            Result.failure(Exception("Not called"))
        }
        setupApp(fakeClient)

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("not a valid json")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("Malformed login request"))
    }
}
