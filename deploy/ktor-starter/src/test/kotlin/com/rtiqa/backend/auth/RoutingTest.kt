package com.rtiqa.backend.auth

import com.rtiqa.backend.config.SupabaseConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.server.routing.routing
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RoutingTest {

    private fun ApplicationTestBuilder.setupApp() {
        val config = SupabaseConfig("https://example.supabase.co", "anon")
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureSecurity(config)
            routing {
                authRoutes(SupabaseAuthClient(config))
            }
        }
    }

    @Test
    fun `No Authorization header returns 401`() = testApplication {
        setupApp()
        val response = client.get("/api/v1/auth/me")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `Basic header returns 401`() = testApplication {
        setupApp()
        val response = client.get("/api/v1/auth/me") {
            header(HttpHeaders.Authorization, "Basic dXNlcjpwYXNz")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `Malformed Bearer returns 401`() = testApplication {
        setupApp()
        val response = client.get("/api/v1/auth/me") {
            header(HttpHeaders.Authorization, "Bearer garbage-token")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
