package com.rtiqa.backend.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DatabaseConfigTest {

    @Test
    fun testDefaultConfigValues() {
        val config = DatabaseConfig(
            jdbcUrl = "jdbc:postgresql://localhost:5432/test",
            username = "postgres",
            password = "secretpassword"
        )
        assertEquals("jdbc:postgresql://localhost:5432/test", config.jdbcUrl)
        assertEquals("postgres", config.username)
        assertEquals("secretpassword", config.password)
        assertEquals(10, config.maximumPoolSize)
        assertEquals(2, config.minimumIdle)
        assertEquals(30000L, config.connectionTimeoutMs)
    }
}
