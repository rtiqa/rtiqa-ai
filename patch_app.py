import os

app_kt_path = '/app/applet/deploy/ktor-starter/src/main/kotlin/com/rtiqa/backend/Application.kt'
with open(app_kt_path, 'r') as f:
    content = f.read()

content = content.replace('''    val dbConfig = DatabaseConfig.fromEnvironment()
    try {
        DatabaseFactory.init(dbConfig)
    } catch (e: Exception) {
        println("Warning: Database initialization failed at startup: ${e.message}")
    }''', '''    val dbConfig = DatabaseConfig.fromEnvironment()
    DatabaseFactory.init(dbConfig)''')

with open(app_kt_path, 'w') as f:
    f.write(content)
