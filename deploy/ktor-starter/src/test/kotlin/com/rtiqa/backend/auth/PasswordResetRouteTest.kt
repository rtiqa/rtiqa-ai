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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PasswordResetRouteTest {

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

    private class FakePasswordResetAuthClient(
        private val resetHandler: suspend (String) -> Result<Unit>
    ) : SupabaseAuthClient(SupabaseConfig("https://example.supabase.co", "test-key")) {
        override suspend fun resetPassword(email: String): Result<Unit> {
            return resetHandler(email)
        }
    }

    @Test
    fun `Successful password reset returns 200 OK with generic message`() = testApplication {
        var emailPassed = ""
        val clientAuth = FakePasswordResetAuthClient { email ->
            emailPassed = email
            Result.success(Unit)
        }
        setupApp(clientAuth)

        val response = client.post("/api/v1/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"student@rtiqa.com"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("student@rtiqa.com", emailPassed)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("password reset instructions have been sent"))
    }

    @Test
    fun `Non-existent user email returns 200 OK without leaking user presence`() = testApplication {
        val clientAuth = FakePasswordResetAuthClient {
            Result.failure(Exception("User not found"))
        }
        setupApp(clientAuth)

        val response = client.post("/api/v1/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"nonexistent@rtiqa.com"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val bodyText = response.bodyAsText()
        // Must return the exact same generic message, preventing User Enumeration
        assertTrue(bodyText.contains("password reset instructions have been sent"))
        assertTrue(!bodyText.contains("not found", ignoreCase = true))
    }

    @Test
    fun `Blank email returns 400 Bad Request`() = testApplication {
        val clientAuth = FakePasswordResetAuthClient { Result.success(Unit) }
        setupApp(clientAuth)

        val response = client.post("/api/v1/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":""}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("Valid email is required"))
    }

    @Test
    fun `Malformed email format returns 400 Bad Request`() = testApplication {
        val clientAuth = FakePasswordResetAuthClient { Result.success(Unit) }
        setupApp(clientAuth)

        val response = client.post("/api/v1/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"invalid-email-address"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("Valid email is required"))
    }

    @Test
    fun `Malformed JSON body returns 400 Bad Request`() = testApplication {
        val clientAuth = FakePasswordResetAuthClient { Result.success(Unit) }
        setupApp(clientAuth)

        val response = client.post("/api/v1/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody("""{"invalid_json":""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("Malformed password reset request"))
    }

    @Test
    fun `Supabase rate limit returns 429 Too Many Requests`() = testApplication {
        val clientAuth = FakePasswordResetAuthClient {
            Result.failure(Exception("over_email_send_rate_limit: rate limit exceeded"))
        }
        setupApp(clientAuth)

        val response = client.post("/api/v1/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"student@rtiqa.com"}""")
        }

        assertEquals(HttpStatusCode.TooManyRequests, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("Too many requests"))
    }

    @Test
    fun `Forgot password alias route works identically`() = testApplication {
        var emailPassed = ""
        val clientAuth = FakePasswordResetAuthClient { email ->
            emailPassed = email
            Result.success(Unit)
        }
        setupApp(clientAuth)

        val response = client.post("/api/v1/auth/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"student@rtiqa.com"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("student@rtiqa.com", emailPassed)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("password reset instructions have been sent"))
    }
}
