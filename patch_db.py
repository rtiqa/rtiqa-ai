import os

db_kt_path = '/app/applet/deploy/ktor-starter/src/main/kotlin/com/rtiqa/backend/database/DatabaseFactory.kt'
with open(db_kt_path, 'r') as f:
    content = f.read()

target = '''    fun init(config: DatabaseConfig = DatabaseConfig.fromEnvironment()) {
        if (config.jdbcUrl.isBlank()) {
            logger.warn("DATABASE_URL is not set. PostgreSQL connection pool will not be initialized.")
            return
        }

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            if (config.username.isNotBlank()) username = config.username
            if (config.password.isNotBlank()) password = config.password'''

replacement = '''    fun init(config: DatabaseConfig = DatabaseConfig.fromEnvironment()) {
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
            password = config.password'''

if target in content:
    content = content.replace(target, replacement)
    with open(db_kt_path, 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
