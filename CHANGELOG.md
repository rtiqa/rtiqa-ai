# 📜 RTIQA Changelog

All notable changes to the **RTIQA** project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-08-05

### Added
- **Multi-Module Clean Architecture**: Split project into modular structure (`:app`, `:core-data`, `:core-database`, `:core-network`, `:core-ui`, `:core-domain`, `:core-ai`, `:feature-*`).
- **AI Tutor Subsystem**: Context-aware learning assistant powered by Google Gemini 1.5 Flash REST API with offline fallback engines.
- **Offline-First Room Database**: Room 2.6 database entities for courses, lessons, quizzes, user profiles, achievements, and certificate history.
- **Bilingual RTL/LTR UI**: Full Arabic and English language support with custom Jetpack Compose typography and layout mirroring.
- **Microservices & Infrastructure Stack**:
  - `docker-compose.yml` with PostgreSQL + pgvector, Keycloak IAM, Directus CMS, LiveKit SFU, Typesense, Qdrant, and OpenTelemetry Collector.
  - Helm v2 Kubernetes deployment chart (`helm/rtiqa-stack`).
  - Ktor 2.3 Kotlin backend microservice gateway (`deploy/ktor-starter`).
- **Developer Experience**: Single-command zero-to-production bootstrap script (`scripts/bootstrap.sh`) and unified environment variable management (`.env.example`).
- **Open-Source Governance**: Issue templates, Pull Request template, CODEOWNERS, Dependabot configuration, and GitHub Actions CI/CD workflows.

### Changed
- Refactored Kotlin dependencies to version catalog (`gradle/libs.versions.toml`).
- Hardened all environment variables to eliminate hardcoded values and static fallbacks.

---

## [0.9.0] - 2026-07-20

### Added
- Initial release prototype featuring course catalog, student dashboard, interactive quizzes, and offline Room DB seed data.
