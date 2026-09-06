# Stage 1: Build the application
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy the root wrapper files
COPY gradlew ./
COPY gradle ./gradle

# Copy the deploy/ktor-starter project
COPY deploy/ktor-starter ./deploy/ktor-starter

# Make gradlew executable and build the application
RUN chmod +x gradlew
RUN ./gradlew -p deploy/ktor-starter installDist -x test --no-daemon

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Create a non-root user
RUN useradd -m appuser && chown -R appuser /app
USER appuser

# Copy the installed application
COPY --from=builder /app/deploy/ktor-starter/build/install/ktor-starter ./

# Cloud Run provides the PORT environment variable
# ApplicationKt prefers PORT over KTOR_PORT
ENTRYPOINT ["./bin/ktor-starter"]
