package com.rtiqa.backend.auth

import com.rtiqa.backend.config.SupabaseConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.routing
import io.ktor.server.testing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RegisterRouteTest {

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

    private class FakeSupabaseAuthClient(
        private val registerHandler: suspend (RegisterRequest) -> Result<SupabaseAuthResponse>
    ) : SupabaseAuthClient(SupabaseConfig("https://example.supabase.co", "test-key")) {
        override suspend fun register(request: RegisterRequest): Result<SupabaseAuthResponse> {
            return registerHandler(request)
        }
    }

    @Test
    fun `Successful register returns 201 Created with token and user dto`() = testApplication {
        val fakeClient = FakeSupabaseAuthClient { req ->
            Result.success(
                SupabaseAuthResponse(
                    access_token = "mock-jwt-token-12345",
                    user = SupabaseUser(id = "user-uuid-abc", email = req.email)
                )
            )
        }
        setupApp(fakeClient)

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "Sara Ahmed", "email": "sara@rtiqa.com", "password": "securePassword123"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("mock-jwt-token-12345"), "Response should contain access token")
        assertTrue(bodyText.contains("user-uuid-abc"), "Response should contain user ID")
        assertTrue(bodyText.contains("sara@rtiqa.com"), "Response should contain email")
        assertTrue(bodyText.contains("Sara Ahmed"), "Response should contain user name")
    }

    @Test
    fun `Successful register with passwordHash (Android compatibility) returns 201 Created`() = testApplication {
        var receivedPassword = ""
        val fakeClient = FakeSupabaseAuthClient { req ->
            receivedPassword = req.password ?: req.passwordHash ?: ""
            Result.success(
                SupabaseAuthResponse(
                    access_token = "mock-jwt-hash-token",
                    user = SupabaseUser(id = "user-uuid-android", email = req.email)
                )
            )
        }
        setupApp(fakeClient)

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "Tariq", "email": "tariq@rtiqa.com", "passwordHash": "hashed_pass_value"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("hashed_pass_value", receivedPassword)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("mock-jwt-hash-token"))
        assertTrue(bodyText.contains("Tariq"))
    }

    @Test
    fun `Register with blank email returns 400 Bad Request`() = testApplication {
        val fakeClient = FakeSupabaseAuthClient {
            Result.success(SupabaseAuthResponse())
        }
        setupApp(fakeClient)

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "Test", "email": "   ", "password": "password123"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("Email and password are required"))
    }

    @Test
    fun `Register with blank password returns 400 Bad Request`() = testApplication {
        val fakeClient = FakeSupabaseAuthClient {
            Result.success(SupabaseAuthResponse())
        }
        setupApp(fakeClient)

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "Test", "email": "test@rtiqa.com", "password": ""}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("Email and password are required"))
    }

    @Test
    fun `Register when Supabase fails on duplicate user returns 400 Bad Request`() = testApplication {
        val fakeClient = FakeSupabaseAuthClient {
            Result.failure(Exception("User already registered"))
        }
        setupApp(fakeClient)

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "Existing", "email": "existing@rtiqa.com", "password": "pass"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("User already registered"))
    }

    @Test
    fun `Malformed register JSON returns 400 Bad Request`() = testApplication {
        val fakeClient = FakeSupabaseAuthClient {
            Result.success(SupabaseAuthResponse())
        }
        setupApp(fakeClient)

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("not a valid json")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("Malformed registration request"))
    }
}
