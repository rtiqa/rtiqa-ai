# 💻 RTIQA Development & Setup Guide

This guide provides instructions for setting up your local environment, building the application, running tests, and managing dependencies.

---

## 📋 Prerequisites

- **JDK**: Version 17 (Temurin recommended)
- **Android Studio**: Ladybug (2024.2.1) or newer
- **Android SDK**: API Level 35 (Compile), API Level 26+ (Minimum)
- **Gradle**: 8.7+ (Kotlin DSL `.gradle.kts`)

---

## 🚀 Building the App

### Command Line
To build the debug APK from the root directory:
```bash
./gradlew assembleDebug
```

To run a clean build (only if necessary):
```bash
./gradlew clean assembleDebug
```

---

## 🧪 Running Unit & Integration Tests

RTIQA uses fast JVM-based local testing for ViewModels, Repositories, DAOs, and Mappers.

### Run All Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Run Tests for a Specific Module
```bash
./gradlew :app:testDebugUnitTest
./gradlew :core-data:testDebugUnitTest
```

---

## 🔑 Environment Variables & API Keys

RTIQA uses `.env` files and `BuildConfig` injection for API keys (such as Gemini API key).

1. Copy `.env.example` to `.env` in the project root:
   ```bash
   cp .env.example .env
   ```
2. Open `.env` and set your API key:
   ```env
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
3. Rebuild the app to update `BuildConfig.GEMINI_API_KEY`.

---

## 📁 Repository Structure

```
├── app/                  # Application container & main entry
├── core-ai/              # AI abstractions & Gemini interfaces
├── core-data/            # Repositories & offline sync manager
├── core-database/        # Room Database, DAOs & Entities
├── core-design/          # Design tokens & core styling
├── core-domain/          # Shared domain models & result states
├── core-network/         # Retrofit, Ktor & HTTP clients
├── core-ui/              # Shared Compose UI design system
├── feature-admin/        # Administrative & school dashboard
├── feature-ai/           # Interactive AI Tutor UI
├── feature-auth/         # Authentication & login flow
├── feature-courses/      # Course management & catalog UI
├── feature-home/         # Student dashboard & hero UI
├── feature-lessons/      # Lesson player & content viewer
├── feature-offline/      # Offline downloads & sync manager
├── feature-profile/      # User profile, badges & certificates
├── feature-quiz/         # Interactive quizzes & assessments
├── feature-settings/     # App settings & preferences
├── docs/                 # Architectural & development docs
└── gradle/               # Gradle wrapper & version catalog
```

---

## 💡 Code Formatting & Verification

Before submitting code:
1. Ensure code adheres to `.editorconfig` rules.
2. Verify all tests pass cleanly:
   ```bash
   gradle testDebugUnitTest
   ```
3. Ensure APK compiles without errors:
   ```bash
   gradle assembleDebug
   ```
