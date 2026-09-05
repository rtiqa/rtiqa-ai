package com.rtiqa.backend.config

data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val maximumPoolSize: Int = 10,
    val minimumIdle: Int = 2,
    val connectionTimeoutMs: Long = 30000
) {
    companion object {
        fun fromEnvironment(): DatabaseConfig {
            val url = System.getenv("DATABASE_URL") ?: ""
            val user = System.getenv("DATABASE_USER") ?: ""
            val pass = System.getenv("DATABASE_PASSWORD") ?: ""
            val maxPool = System.getenv("DB_MAX_POOL_SIZE")?.toIntOrNull() ?: 10
            val minIdle = System.getenv("DB_MIN_IDLE")?.toIntOrNull() ?: 2
            val timeout = System.getenv("DB_CONNECTION_TIMEOUT_MS")?.toLongOrNull() ?: 30000L

            return DatabaseConfig(
                jdbcUrl = url,
                username = user,
                password = pass,
                maximumPoolSize = maxPool,
                minimumIdle = minIdle,
                connectionTimeoutMs = timeout
            )
        }
    }
}
