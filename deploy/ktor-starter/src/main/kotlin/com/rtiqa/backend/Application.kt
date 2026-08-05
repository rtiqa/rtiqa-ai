package com.rtiqa.backend

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SystemHealth(
    val status: String,
    val service: String,
    val timestamp: Long = System.currentTimeMillis()
)

fun main() {
    val port = System.getenv("KTOR_PORT")?.toIntOrNull() ?: 8081
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    
    install(CORS) {
        anyHost()
    }

    val environmentName = System.getenv("KTOR_ENV") ?: "development"
    val authProvider = System.getenv("AUTH_PROVIDER_NAME") ?: "Keycloak IAM OIDC"
    val dbProvider = System.getenv("DB_PROVIDER_NAME") ?: "Supabase PostgreSQL 15 + pgvector"
    val searchProvider = System.getenv("SEARCH_PROVIDER_NAME") ?: "Typesense 26.0"
    val vectorProvider = System.getenv("VECTOR_PROVIDER_NAME") ?: "Qdrant Rust v1.8.0"
    val mediaProvider = System.getenv("MEDIA_PROVIDER_NAME") ?: "LiveKit WebRTC SFU v1.6.0"

    routing {
        get("/health") {
            call.respond(SystemHealth(status = "UP", service = "RTIQA Microservice Gateway Engine"))
        }

        get("/api/v1/status") {
            call.respond(mapOf(
                "version" to "1.0.0",
                "environment" to environmentName,
                "auth_provider" to authProvider,
                "db_provider" to dbProvider,
                "search_provider" to searchProvider,
                "vector_provider" to vectorProvider,
                "media_provider" to mediaProvider
            ))
        }
    }
}
