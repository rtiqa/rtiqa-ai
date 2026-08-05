# 🏛️ RTIQA Architecture Decision Records (ADR) Index

**Project**: RTIQA (رتقاء) — Enterprise Smart Learning & Knowledge Ecosystem  
**Repository**: `rtiqa/mobile`  
**Governance Board**: RTIQA Technical Architecture Council  

This repository maintains formal **Architecture Decision Records (ADR)** to document key architectural choices, technical context, alternatives considered, security implications, scalability impacts, and maintenance criteria.

---

## 📑 Index of Architecture Decision Records

| ID | Title | Status | Date | Subsystem |
| :--- | :--- | :--- | :--- | :--- |
| **[ADR-0001](ADR-0001-android-ui-and-architecture.md)** | Jetpack Compose M3 & Modular Clean Architecture for Native Android | **Accepted** | 2026-08-05 | Mobile Client |
| **[ADR-0002](ADR-0002-backend-microservices-architecture.md)** | Kotlin Ktor Server & Supabase BaaS Infrastructure | **Accepted** | 2026-08-05 | Backend Microservices |
| **[ADR-0003](ADR-0003-ai-and-llm-orchestration-stack.md)** | Gemini 1.5 REST API, LangChain4j & On-Device MediaPipe LLM | **Accepted** | 2026-08-05 | AI Subsystem |
| **[ADR-0004](ADR-0004-database-and-local-persistence.md)** | Room Database with KSP Annotation Processing & SQLCipher Encryption | **Accepted** | 2026-08-05 | Data Persistence |
| **[ADR-0005](ADR-0005-authentication-and-identity-management.md)** | Red Hat Keycloak Enterprise IAM with OAuth2 / OIDC | **Accepted** | 2026-08-05 | Authentication & Identity |
| **[ADR-0006](ADR-0006-offline-first-and-sync-engine.md)** | Reactive Room + WorkManager Background Delta Sync Engine | **Accepted** | 2026-08-05 | Offline Synchronization |
| **[ADR-0007](ADR-0007-lms-and-lti-integration.md)** | Instructure Canvas LMS Integration via LTI 1.3 & xAPI | **Accepted** | 2026-08-05 | LMS Subsystem |
| **[ADR-0008](ADR-0008-school-erp-and-sis.md)** | ERPNext Education Module for Multi-Tenant School Administration | **Accepted** | 2026-08-05 | ERP & SIS |
| **[ADR-0009](ADR-0009-headless-cms.md)** | Directus Headless CMS for Dynamic Course Authoring | **Accepted** | 2026-08-05 | Content Management |
| **[ADR-0010](ADR-0010-search-engine-and-indexing.md)** | Typesense In-Memory C++ Search Engine for Course Catalog | **Accepted** | 2026-08-05 | Full-Text Search |
| **[ADR-0011](ADR-0011-vector-database-and-rag.md)** | Qdrant Vector Database & pgvector for Textbook RAG | **Accepted** | 2026-08-05 | Vector Search & RAG |
| **[ADR-0012](ADR-0012-video-streaming-and-media.md)** | AndroidX Media3 ExoPlayer & LiveKit WebRTC Infrastructure | **Accepted** | 2026-08-05 | Video & Streaming |
| **[ADR-0013](ADR-0013-realtime-communication-and-chat.md)** | Matrix.org Decentralized Protocol for Peer-to-Peer Study Groups | **Accepted** | 2026-08-05 | Communication & Chat |
| **[ADR-0014](ADR-0014-ocr-and-speech-processing.md)** | Google ML Kit OCR, Whisper.cpp STT & Piper Neural TTS | **Accepted** | 2026-08-05 | OCR & Speech |
| **[ADR-0015](ADR-0015-analytics-and-telemetry.md)** | PostHog Product Analytics & Privacy-Compliant Telemetry | **Accepted** | 2026-08-05 | Analytics & Telemetry |
| **[ADR-0016](ADR-0016-devops-and-ci-cd-pipelines.md)** | GitHub Actions CI/CD & Fastlane Release Automation | **Accepted** | 2026-08-05 | DevOps & CI/CD |
| **[ADR-0017](ADR-0017-security-and-vulnerability-auditing.md)** | GitHub CodeQL SAST & MobSF Mobile Security Auditing | **Accepted** | 2026-08-05 | Security & Auditing |
| **[ADR-0018](ADR-0018-observability-and-crash-reporting.md)** | Open-Source Sentry Crash Reporting & OpenTelemetry Metrics | **Accepted** | 2026-08-05 | Observability |
| **[ADR-0019](ADR-0019-technical-documentation-system.md)** | MkDocs Material & JetBrains Dokka Documentation Engine | **Accepted** | 2026-08-05 | Documentation |
| **[ADR-0020](ADR-0020-automated-testing-and-visual-regression.md)** | Robolectric JVM Testing & Roborazzi Screenshot Verification | **Accepted** | 2026-08-05 | Testing & QA |

---

## ⚖️ Governance Rule
All proposed architectural changes, technology stack migrations, or library additions to RTIQA **MUST** be submitted via an ADR Pull Request adhering to this template.
