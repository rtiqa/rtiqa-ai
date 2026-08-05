#!/usr/bin/env bash
# ==============================================================================
# RTIQA Developer Onboarding & Infrastructure Bootstrap Script
# ==============================================================================
set -euo pipefail

echo "======================================================================"
echo "🚀 Initializing RTIQA Development Environment Bootstrap..."
echo "======================================================================"

# Step 1: Initialize Environment File
if [ ! -f .env ]; then
    echo "📋 Creating .env file from .env.example..."
    cp .env.example .env
    echo "✅ Created .env successfully."
else
    echo "ℹ️ Existing .env file detected, preserving configuration."
fi

# Step 2: Validate Required Tools
echo "🔍 Validating core developer toolchain..."
command -v java >/dev/null 2>&1 || echo "⚠️ Warning: JDK not found in PATH."
command -v docker >/dev/null 2>&1 || echo "ℹ️ Note: Docker executable not present in container environment, Docker Compose configs available under /docker."

# Step 3: Verify Configuration Assets
echo "📁 Validating infrastructure configurations..."
[ -f docker/docker-compose.yml ] && echo "  - Docker Compose configuration verified."
[ -f docker/keycloak/realm-export.json ] && echo "  - Keycloak realm configuration verified."
[ -f docker/livekit/livekit.yaml ] && echo "  - LiveKit SFU configuration verified."
[ -f docker/otel/otel-collector-config.yaml ] && echo "  - OpenTelemetry Collector configuration verified."
[ -f helm/rtiqa-stack/Chart.yaml ] && echo "  - Helm Chart configuration verified."
[ -f deploy/ktor-starter/build.gradle.kts ] && echo "  - Ktor microservice backend build configuration verified."

echo "======================================================================"
echo "🎉 RTIQA Development Environment Setup Completed Successfully!"
echo "======================================================================"
