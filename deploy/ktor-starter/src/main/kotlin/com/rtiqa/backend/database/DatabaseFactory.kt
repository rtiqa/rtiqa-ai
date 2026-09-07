package com.rtiqa.backend.database

import com.rtiqa.backend.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import java.sql.Connection

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)
    private var dataSource: HikariDataSource? = null

    fun init(config: DatabaseConfig = DatabaseConfig.fromEnvironment()) {
        if (config.jdbcUrl.isBlank()) {
            throw IllegalStateException("Configuration error: DATABASE_URL environment variable is required and cannot be blank.")
        }
        if (config.username.isBlank()) {
            throw IllegalStateException("Configuration error: DATABASE_USER environment variable is required and cannot be blank.")
        }
        if (config.password.isBlank()) {
            throw IllegalStateException("Configuration error: DATABASE_PASSWORD environment variable is required and cannot be blank.")
        }

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            username = config.username
            password = config.password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = config.maximumPoolSize
            minimumIdle = config.minimumIdle
            connectionTimeout = config.connectionTimeoutMs
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            connectionTestQuery = "SELECT 1"
            validate()
        }

        try {
            dataSource = HikariDataSource(hikariConfig)
            logger.info("HikariCP PostgreSQL connection pool initialized successfully for URL: ${redactUrl(config.jdbcUrl)}")
        } catch (e: Exception) {
            logger.error("Failed to initialize HikariCP connection pool: ${e.message}")
            throw e
        }
    }

    fun getConnection(): Connection? {
        return try {
            dataSource?.connection
        } catch (e: Exception) {
            logger.error("Failed to acquire database connection: ${e.message}")
            null
        }
    }

    fun checkHealth(): Boolean {
        val ds = dataSource ?: return false
        return try {
            ds.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT 1").use { rs ->
                        rs.next()
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Database health check query failed: ${e.message}")
            false
        }
    }

    fun close() {
        try {
            dataSource?.close()
            logger.info("HikariCP connection pool closed gracefully.")
        } catch (e: Exception) {
            logger.error("Error closing connection pool: ${e.message}")
        }
    }

    private fun redactUrl(url: String): String {
        return if (url.contains("@")) {
            url.substringBefore("@") + "@[REDACTED]"
        } else {
            url
        }
    }
}
