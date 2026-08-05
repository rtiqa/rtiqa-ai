# Contributing to RTIQA (رتقاء)

Thank you for your interest in contributing to **RTIQA**! We welcome contributions from developers worldwide to help build a world-class, enterprise-grade Android smart learning ecosystem.

---

## 📜 Code of Conduct

Please review and adhere to our [Code of Conduct](CODE_OF_CONDUCT.md) in all community interactions, issues, and pull requests.

---

## 🏗️ Architecture & Project Overview

RTIQA is built using modern Android development practices:
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: Modular MVVM + Clean Architecture with Repository Pattern
- **Persistence**: Room Database (Offline-First strategy)
- **AI Integration**: Gemini 1.5 Pro / Flash REST API with fallback engines
- **Asynchrony**: Kotlin Coroutines & Flow

For a deep dive into the architecture, please read [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

---

## 🛠️ Getting Started & Setup

1. **Fork and Clone the Repository**
   ```bash
   git clone https://github.com/your-username/rtiqa.git
   cd rtiqa
   ```

2. **Open in Android Studio**
   Open the root directory in **Android Studio Ladybug** (or newer) with JDK 17 configured.

3. **Configure Environment Variables**
   Copy `.env.example` to `.env` (or set up system environment variables) if using Gemini API keys:
   ```bash
   cp .env.example .env
   ```

4. **Build & Verify**
   Run local JVM unit tests to verify your environment setup:
   ```bash
   gradle testDebugUnitTest
   ```

---

## 🌿 Branching Strategy & Git Workflow

We follow a structured Git flow:
- `main`: Production-ready releases.
- `develop`: Integration branch for active development.
- Feature branches: Branch off `develop` using `feature/short-description` or `fix/issue-description`.

### Commit Message Conventions
We enforce **Conventional Commits**:
- `feat(courses)`: Add filter by level in course list
- `fix(quiz)`: Prevent null pointer crash on empty quiz questions
- `docs(readme)`: Update architecture overview badges
- `refactor(database)`: Optimize Room DAO query for lessons
- `test(auth)`: Add Robolectric tests for user profile authentication

---

## 🎨 Code Style Guidelines

- Adhere strictly to [Official Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) and Android Jetpack Compose guidelines.
- Format all files matching `.editorconfig`.
- Keep Composables stateless where possible, moving business logic into ViewModels.
- Always include Arabic (`_ar`) and English translations for user-facing string resources when applicable.
- All interactive UI components must include a `testTag` modifier for automated testability (e.g., `Modifier.testTag("submit_button")`).

---

## 🧪 Testing Guidelines

- Write unit tests for all ViewModels, Repositories, and Mappers.
- Keep JVM unit tests fast (`gradle testDebugUnitTest`).
- Avoid adding instrumented tests (`androidTest/`) requiring real emulators for basic logic.

---

## 📥 Submitting a Pull Request

1. Ensure all local tests pass:
   ```bash
   gradle testDebugUnitTest
   ```
2. Make sure code compiles cleanly without compiler warnings.
3. Open a PR against the `develop` branch.
4. Fill out the [Pull Request Template](.github/PULL_REQUEST_TEMPLATE.md) completely.
5. Wait for automated CI status checks and core maintainer review.

Thank you for helping make RTIQA a world-class open-source project! 🚀
