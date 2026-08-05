# 🏛️ RTIQA Architecture & System Design

This document describes the high-level architecture, module organization, data layer design, and design patterns used in **RTIQA** (رتقاء).

---

## 1. Modular System Overview

RTIQA is structured as a scalable, multi-module Android project following modern Google Android architectural recommendations.

```
                      +-------------------+
                      |      :app         |
                      | (App Container &  |
                      |   Navigation)     |
                      +---------+---------+
                                |
        +-----------------------+-----------------------+
        |                       |                       |
+-------v-------+       +-------v-------+       +-------v-------+
| feature-home  |       | feature-courses|       |  feature-ai   |
| feature-quiz  |       | feature-admin |       | feature-auth  |
+-------+-------+       +-------+-------+       +-------+-------+
        |                       |                       |
        +-----------------------+-----------------------+
                                |
             +------------------v-------------------+
             |            core-data                 |
             |   (Repositories, Mappers, Sync)      |
             +--------+--------------------+--------+
                      |                    |
          +-----------v----+          +----v-----------+
          | core-database  |          |  core-network  |
          |  (Room DB &    |          | (Retrofit/Ktor |
          |   Entities)    |          |  Gemini API)   |
          +----------------+          +----------------+
```

### Module Breakdown:
- **`:app`**: Root application module containing entry point (`MainActivity`), top-level navigation graphs (`RtiqaNavGraph`), and dependency wiring.
- **`:feature-*`**: Feature modules (`feature-home`, `feature-courses`, `feature-quiz`, `feature-ai`, `feature-admin`, `feature-auth`, `feature-lessons`, `feature-profile`, `feature-settings`, `feature-offline`). Encapsulates Compose UI screens, ViewModels, and UI state models.
- **`:core-data`**: Core data domain, repositories (`CourseRepository`, `UserRepository`, `QuizRepository`, `AiRepository`), data mappers, offline sync scheduler (`OfflineSyncWorker`).
- **`:core-database`**: Local SQLite database abstraction using Room ORM (`RtiqaDatabase`, DAOs, Entities, TypeConverters).
- **`:core-network`**: Remote networking client, Retrofit endpoints, network interceptors, and Gemini AI REST client (`GeminiApiService`).
- **`:core-ui`**: Shared Design System (Rtiqa Design System - RDS) containing reusable UI components, typography, themes, colors, top bars, buttons, state wrappers.
- **`:core-domain`**: Core domain interfaces, shared business models, result wrappers (`RtiqaResult`).

---

## 2. Layered Architecture & Unidirectional Data Flow (UDF)

Each feature screen in RTIQA strictly implements **Unidirectional Data Flow**:

```
 [ User Interaction / Input ]  ==> Calls ==>  ViewModel Action
                                                   ||
                                            Updates Repository
                                                   ||
 [ Flow / StateFlow Emission ] <== Emits <== ViewModel State
                                                   ||
    Compose Screen Renders UI <== Receives <== State
```

1. **UI Layer (Jetpack Compose)**: Stateless Composables consume `@Composable State` and emit UI events.
2. **ViewModel Layer**: Manages `StateFlow<UiState>` using sealed class states (`Loading`, `Success`, `Error`).
3. **Repository Layer**: Single Source of Truth (SSOT). Fetches from Room DB first, synchronizes with remote REST API, and updates local database reactively.
4. **Data Layer (Room + Retrofit)**: Low-level data sources.

---

## 3. Data Persistence & Offline-First Engine

- **Database Engine**: Room 2.6+ with KSP code generation.
- **Single Source of Truth**: UI screens observe Room database queries via Kotlin `Flow<List<T>>`.
- **Pre-populated Seed Data**: Automatic non-destructive DB callback seeds initial courses, lessons, quizzes, user profiles, and certificates upon first app initialization.
- **Background Sync**: Integrated with `WorkManager` (`OfflineSyncWorker`) for resilient background data synchronization.

---

## 4. AI Tutor Subsystem (Gemini API Integration)

- **Service**: REST integration with Gemini 1.5 Flash model.
- **Smart System Prompting**: Contextual prompts tailored to student grade level, subject, and current lesson.
- **Fallback Engine**: Smart local fallback mechanism ensuring 100% continuous functionality even in offline mode.

---

## 5. Security & Credentials Management

- API keys are injected at build-time via `BuildConfig` (`.env` file or environment variables).
- Zero hardcoded credentials or API tokens in source code.
- Keystore signing configuration managed via Gradle properties.
