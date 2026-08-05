# 🎓 RTIQA (رتقاء) — Smart Learning & Knowledge Platform

[![Android CI/CD](https://github.com/rtiqa/mobile/actions/workflows/ci.yml/badge.svg)](https://github.com/rtiqa/mobile/actions/workflows/ci.yml)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg?logo=kotlin)](https://kotlinlang.org)
[![Android Min SDK](https://img.shields.io/badge/Min%20SDK-26-green.svg?logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Modular%20MVVM%20%2B%20Clean-orange.svg)]()

**RTIQA (رتقاء)** is an enterprise-grade, offline-first smart learning platform built with **Kotlin** and **Jetpack Compose**. It features an intelligent AI Tutor powered by **Gemini 1.5**, interactive quizzes, offline-first Room database synchronization, verified digital certificate generation, user progress tracking, and administrative control panels.

Designed natively for **bilingual (Arabic & English)** mobile learning experiences with full Right-to-Left (RTL) layout support.

---

## 🌟 Key Features

- 🧠 **AI Tutor (المعلم الذكي)**: Interactive learning assistant powered by Gemini 1.5, offering real-time explanations, hints, and adaptive context for every lesson.
- 📚 **Course & Lesson Engine**: Comprehensive course catalog with progress tracking, interactive video/text content, and modular learning paths.
- 📝 **Interactive Quizzes**: Multiple-choice and true/false assessments with real-time feedback, explanations, and XP rewards.
- ⚡ **Offline-First Persistence**: Full offline support powered by Room SQLite Database, ensuring uninterrupted learning without network access.
- 🏆 **Gamification & Certificates**: XP coins, streak counters, unlockable badges, and downloadable digital completion certificates.
- 🏫 **School Administration Portal**: Dedicated admin tools for managing classes, students, course assignments, and system metrics.
- 🎨 **Material Design 3**: Modern visual design system with dynamic light/dark mode, edge-to-edge screens, and fluid animations.

---

## 🏛️ System Architecture

RTIQA is structured as a **Multi-Module Clean Architecture** project to maximize code reusability, testability, and build scalability:

```
                          +-------------------+
                          |      :app         |
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
                 +--------+--------------------+--------+
                          |                    |
              +-----------v----+          +----v-----------+
              | core-database  |          |  core-network  |
              +----------------+          +----------------+
```

For complete architectural details, please refer to [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

---

## 🛠️ Tech Stack & Open Source Libraries

- **Language**: [Kotlin](https://kotlinlang.org/) (100%)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
- **Navigation**: Navigation Compose with type-safe routing
- **Database**: [Room Database](https://developer.android.com/training/data-storage/room) with KSP annotation processor
- **Networking**: [Retrofit 2](https://square.github.io/retrofit/) & OkHttp 3
- **AI Integration**: [Google Gemini 1.5 REST API](https://ai.google.dev/)
- **Asynchrony**: Kotlin Coroutines & `StateFlow`
- **Background Jobs**: AndroidX `WorkManager`
- **Testing**: JUnit 4, Robolectric, Kotlinx Coroutines Test

---

## 🚀 Quick Start Guide

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or IntelliJ IDEA with Android plugin.
- JDK 17 configured.

### 1. Clone & Build
```bash
git clone https://github.com/rtiqa/mobile.git
cd mobile
gradle assembleDebug
```

### 2. Configure Gemini API Key
To enable full AI Tutor capabilities, obtain a Gemini API key from [Google AI Studio](https://aistudio.google.com/) and configure `.env`:

```bash
cp .env.example .env
```
Edit `.env`:
```env
GEMINI_API_KEY=your_gemini_api_key_here
```

### 3. Run Unit Tests
```bash
gradle testDebugUnitTest
```

For complete setup instructions, check [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md).

---

## 🤝 Community & Contributing

We welcome contributions from developers worldwide! Please read our [Contributing Guidelines](CONTRIBUTING.md) and [Code of Conduct](CODE_OF_CONDUCT.md) before submitting pull requests.

### Ways to Contribute:
- 🐛 Report bugs or crashes via [GitHub Issues](https://github.com/rtiqa/mobile/issues).
- 💡 Propose new features or UI enhancements.
- 🔧 Submit Pull Requests to fix bugs or optimize database/UI performance.

---

## 📄 License

RTIQA is licensed under the [Apache License 2.0](LICENSE).

```text
Copyright 2026 RTIQA Platform Maintainers

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
