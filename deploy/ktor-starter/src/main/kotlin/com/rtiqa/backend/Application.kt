package com.rtiqa.backend

import com.rtiqa.backend.auth.SupabaseAuthClient
import com.rtiqa.backend.auth.authRoutes
import com.rtiqa.backend.auth.configureSecurity
import com.rtiqa.backend.config.DatabaseConfig
import com.rtiqa.backend.config.SupabaseConfig
import com.rtiqa.backend.database.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SystemHealth(
    val status: String,
    val database: String,
    val service: String,
    val timestamp: Long = System.currentTimeMillis()
)

fun main() {
    val dbConfig = DatabaseConfig.fromEnvironment()
    try {
        DatabaseFactory.init(dbConfig)
    } catch (e: Exception) {
        println("Warning: Database initialization failed at startup: ${e.message}")
    }

    val port = System.getenv("KTOR_PORT")?.toIntOrNull() ?: 8081
    val server = embeddedServer(Netty, port = port, host = "0.0.0.0") {
        module()
    }
    
    Runtime.getRuntime().addShutdownHook(Thread {
        DatabaseFactory.close()
        server.stop(1000, 2000)
    })
    
    server.start(wait = true)
}

fun Application.module() {
    val supabaseConfig = SupabaseConfig.fromEnvironment()
    val authClient = SupabaseAuthClient(supabaseConfig)
    
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
    
    configureSecurity(supabaseConfig)
    
    val environmentName = System.getenv("KTOR_ENV") ?: "development"
    val authProvider = System.getenv("AUTH_PROVIDER_NAME") ?: "Supabase Auth"
    val dbProvider = System.getenv("DB_PROVIDER_NAME") ?: "Supabase PostgreSQL 15 + pgvector"

    routing {
        get("/health") {
            val dbHealthy = DatabaseFactory.checkHealth()
            val statusStr = if (dbHealthy) "UP" else "DEGRADED"
            val dbStr = if (dbHealthy) "UP" else "DOWN"
            val statusCode = if (dbHealthy) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable
            
            call.respond(
                statusCode,
                SystemHealth(
                    status = statusStr,
                    database = dbStr,
                    service = "RTIQA Microservice Gateway Engine"
                )
            )
        }

        get("/api/v1/status") {
            call.respond(mapOf(
                "version" to "1.0.0",
                "environment" to environmentName,
                "auth_provider" to authProvider,
                "db_provider" to dbProvider
            ))
        }
        
        authRoutes(authClient)
    }
}
