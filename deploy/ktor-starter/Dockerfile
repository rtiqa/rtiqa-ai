# Stage 1: Build the application
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy the root wrapper files (since ktor-starter does not have its own gradlew)
COPY gradlew ./
COPY gradle ./gradle

# Copy the deploy/ktor-starter project files
COPY deploy/ktor-starter/settings.gradle.kts ./
COPY deploy/ktor-starter/build.gradle.kts ./
COPY deploy/ktor-starter/src ./src

# Make gradlew executable and build the application
RUN chmod +x gradlew
RUN ./gradlew installDist -x test --no-daemon

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Create a non-root user
RUN useradd -m appuser && chown -R appuser /app
USER appuser

# Copy the installed application
COPY --from=builder /app/build/install/ktor-starter ./

# Cloud Run provides the PORT environment variable
# ApplicationKt prefers PORT over KTOR_PORT
ENTRYPOINT ["./bin/ktor-starter"]
