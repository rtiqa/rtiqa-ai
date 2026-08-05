# 📚 RTIQA Official Open-Source Acquisition Catalog

**Project**: RTIQA (رتقاء) — Enterprise Smart Learning & Knowledge Platform  
**Document Version**: 1.0.0  
**Author**: RTIQA Open Source Research Directorate  
**Status**: APPROVED REFERENCE ARCHITECTURE  

---

## Executive Summary

The **RTIQA Acquisition Catalog** serves as the definitive engineering reference for evaluating, selecting, and adopting world-class open-source software (OSS) components across the entire RTIQA ecosystem. To transform RTIQA into a global gold standard for smart learning platforms, every subsystem is evaluated against strict criteria:

1. **Permissive Open-Source Licensing** (Apache 2.0, MIT, BSD 3-Clause)
2. **Community Momentum & Active Maintenance** (GitHub stars, regular releases, healthy bus factor)
3. **Enterprise Scalability & Security** (Production hardening, SAST compliance, battle-tested deployments)
4. **Seamless Integration Compatibility** (Kotlin/Android Native, REST/gRPC interfaces, Clean Architecture suitability)

---

## 1. Android Client Architecture & UI Subsystem

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **UI Framework** | Jetpack Compose M3 vs React Native vs Flutter | **Jetpack Compose (M3)** | Apache 2.0 (Google) | Primary native Android framework. Unmatched performance, dynamic light/dark mode, native RTL support, and declarative state-driven UI. |
| **Image Loading** | Coil vs Glide vs Fresco | **Coil (Coroutines Image Loader)** | Apache 2.0 (Instacart) | Built 100% on Kotlin Coroutines and OkHttp. Lightweight, native Compose integration (`AsyncImage`), minimal APK size footprint. |
| **Data Visualization** | Vico vs Compose-Charts vs MPAndroidChart | **Vico** | Apache 2.0 | Native Jetpack Compose chart library with smooth Bezier curves, animation support, dynamic theming, and full RTL compatibility. |
| **Dependency Injection**| Hilt vs Koin | **Hilt (Dagger-based)** | Apache 2.0 (Google) | Standard for enterprise Android apps. Compile-time dependency verification prevents runtime injection crashes. |
| **Serialization** | Kotlinx.serialization vs Moshi vs Gson | **Kotlinx.serialization** | Apache 2.0 (JetBrains) | Native Kotlin multiplatform support, reflection-free compilation, fast performance, type-safe Navigation routes. |

---

## 2. Backend Engine & Microservices Framework

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **Server Framework** | Ktor vs Spring Boot vs NestJS | **Ktor** | Apache 2.0 (JetBrains) | Async coroutine-native Kotlin framework. Shares domain data models (`core-domain`) directly between Android app and backend. |
| **BaaS / Gateway** | Supabase vs PocketBase vs Appwrite | **Supabase (PostgreSQL)** | Apache 2.0 / MIT | Enterprise-grade BaaS providing Auth, Row Level Security (RLS), realtime WebSockets, edge functions, and pgvector. |

---

## 3. AI & LLM Orchestration Engine

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **LLM Framework** | LangChain4j vs Spring AI vs LlamaIndex | **LangChain4j** | Apache 2.0 | JVM-native framework for LLM orchestration, structured output parsing, dynamic prompt templates, and agentic tool execution. |
| **On-Device LLM** | MediaPipe Tasks vs Ollama Local vs ONNX Runtime | **MediaPipe LLM Inference API** | Apache 2.0 (Google) | Runs quantized local models (Gemma 2B, Phi-3) directly on-device via NPU/GPU for zero-latency offline AI tutoring. |

---

## 4. Learning Management System (LMS) & SCORM / xAPI

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **LMS Core** | Canvas LMS vs Moodle vs Open edX | **Canvas LMS (Instructure)** | AGPLv3 / REST API | Modern GraphQL/REST endpoints. RTIQA integrates via LTI 1.3 (Learning Tools Interoperability) standard. |
| **Learning Analytics**| Experience API (xAPI) Learning Locker vs Ralph | **Ralph (Open edX)** | MIT | LRS (Learning Record Store) for tracking granular learning events (quiz answers, video pause/play, AI interaction loops). |

---

## 5. Enterprise Resource Planning (ERP) & School Information System (SIS)

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **SIS / ERP** | ERPNext Education vs OpenSIS vs Apache Fineract | **ERPNext (Education Module)** | GPLv3 / REST API | Full school administrative capabilities: student enrollment, attendance, gradebooks, fee processing, and multi-tenant isolation. |

---

## 6. Content Management System (CMS) & Headless Authoring

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **Headless CMS** | Strapi vs Directus vs Decap CMS | **Directus** | BSL / GPLv3 | Wraps existing PostgreSQL databases with instant REST/GraphQL endpoints, fine-grained access control, and rich media management. |

---

## 7. Authentication & Identity Access Management (IAM)

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **Identity Provider** | Keycloak vs Authentik vs Ory Kratos | **Keycloak** | Apache 2.0 (Red Hat) | Enterprise OIDC/OAuth2 server supporting OAuth 2.0, SAML 2.0, social logins, multi-factor authentication (MFA), and tenant realms. |

---

## 8. Video Streaming & Media Player Engine

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **Android Player** | Jetpack Media3 (ExoPlayer) vs VLC Android | **AndroidX Media3 ExoPlayer** | Apache 2.0 (Google) | Official Android media player. Supports HLS, DASH, encrypted DRM (Widevine), video offline caching, and Compose integration. |
| **Live Streaming** | LiveKit vs Jitsi Meet vs Video.js | **LiveKit** | Apache 2.0 | Open-source WebRTC infrastructure for real-time virtual classrooms, video lectures, and live interactive tutoring. |

---

## 9. Speech Processing (STT / TTS)

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **Speech-to-Text** | Whisper.cpp vs Android SpeechRecognizer | **Whisper.cpp (JNI / NDK)** | MIT (ggerganov) | High-accuracy offline speech recognition with multi-language and Arabic dialect support. |
| **Text-to-Speech** | Piper TTS vs Sherpa-onnx | **Piper TTS** | MIT | Fast, lightweight neural text-to-speech engine optimized for mobile devices and embedded hardware. |

---

## 10. Real-time Chat & Community Infrastructure

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **Chat Engine** | Matrix.org (Synapse/Dendrite) vs Stream Chat SDK | **Matrix.org Protocol** | Apache 2.0 | Decentralized, end-to-end encrypted messaging network perfect for peer-to-peer student study groups and teacher communication. |

---

## 11. Vector Database & RAG (Knowledge Retrieval)

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **Vector Engine** | Qdrant vs Milvus vs pgvector | **Qdrant** | Apache 2.0 | High-performance Rust-based vector search engine with filtering, payload storage, and ultra-fast similarity search for textbook RAG. |

---

## 12. Optical Character Recognition (OCR) & Document Scanning

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **OCR Engine** | Google ML Kit Text Recognition vs Tesseract | **ML Kit Text Recognition** | Apache 2.0 | On-device text recognition with instant camera stream binding and accurate Latin/Arabic script detection. |

---

## 13. Search Engine & Content Indexing

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **Full-Text Search**| Meilisearch vs Typesense vs Elasticsearch | **Typesense** | GPLv3 / Apache 2.0 | Lightning-fast, typo-tolerant search engine optimized for educational course catalogs, lesson search, and digital library queries. |

---

## 14. Analytics, Telemetry & Event Tracking

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **Analytics Engine**| PostHog vs Matomo vs Plausible | **PostHog** | MIT / Open Source | Self-hosted product analytics platform providing funnels, user session replays, feature flags, and privacy-compliant event tracking. |

---

## 15. Offline Persistence & Data Synchronization

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **Offline Sync** | PowerSync vs WatermelonDB vs Room + WorkManager | **Room + WorkManager (Custom Sync Engine)** | Apache 2.0 (Google) | Native Kotlin Room ORM paired with AndroidX `WorkManager` for guaranteed background delta synchronization and conflict handling. |

---

## 16. DevOps, Continuous Integration & Automated Pipelines

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **CI Engine** | GitHub Actions vs GitLab CI vs Jenkins | **GitHub Actions** | Native GitHub Platform | Automated linting, static code analysis, JUnit unit tests, code coverage reporting, and automated APK/AAB build distribution. |
| **App Distribution**| Fastlane vs Gradle Play Publisher | **Fastlane** | MIT | Automates Android beta deployment (Firebase App Distribution, Google Play Internal Track) and metadata localization. |

---

## 17. Containerization & Cloud Deployment Infrastructure

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **Container Engine**| Docker & Docker Compose vs Podman | **Docker & Docker Compose** | Apache 2.0 | Single-command local environment bootstrapping (`docker-compose up`) for Keycloak, Directus, Typesense, Qdrant, and Ktor server. |
| **Orchestration** | Kubernetes + Helm vs Nomad | **Kubernetes (K8s)** | Apache 2.0 (CNCF) | Enterprise deployment manifests and Helm charts for multi-tenant school district cloud hosting. |

---

## 18. Design System & Component Library

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **Design Tokens** | Material Design 3 (M3) Tokens | **Rtiqa Design System (RDS)** | Apache 2.0 | Tailored Material 3 design tokens (`core-design`) enforcing 48dp touch targets, elevated surface tokens, and RTL-first typography. |

---

## 19. Administrative & Operational Dashboards

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **Admin Panel** | Refine vs Tooljet vs AdminBro | **Refine (React / TypeScript)** | MIT | Enterprise-grade headless React framework for building school management, user role assignments, and platform analytics dashboards. |

---

## 20. Automated Testing & Visual Regression

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **JVM Testing** | Robolectric vs Standard JUnit | **Robolectric** | Apache 2.0 | Fast, emulator-free local JVM tests simulating Android framework APIs for UI components and ViewModels. |
| **Visual Regression**| Roborazzi vs Paparazzi | **Roborazzi** | Apache 2.0 (takahirom) | High-speed JVM screenshot tests integrated directly with Compose and Robolectric for visual pixel integrity validation. |

---

## 21. Security, Encryption & Vulnerability Scanning

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **DB Encryption** | SQLCipher vs AndroidX EncryptedSharedPreferences | **SQLCipher for Android** | BSD | Full AES-256 database encryption for Room SQLite tables storing sensitive student records and offline lesson cache. |
| **Static Security**| MobSF (Mobile Security Framework) vs SonarQube | **MobSF** | GPLv3 | Automated security auditing for APKs, detecting hardcoded credentials, weak crypto, and manifest permission vulnerabilities. |

---

## 22. Observability, Crash Reporting & Performance Monitoring

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **Crash Reporting**| Sentry Open Source vs GlitchTip | **Sentry** | BUSL / Open Source | Real-time crash reports, stack traces, performance breadcrumbs, and network call metrics for mobile clients. |
| **Telemetry** | OpenTelemetry vs Prometheus + Grafana | **OpenTelemetry (OTel)** | Apache 2.0 (CNCF) | Standardized tracing and metric collection across mobile app and backend services. |

---

## 23. Technical Documentation Systems

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **Docs Generator** | MkDocs Material vs Docusaurus vs Dokka | **MkDocs Material** | MIT | Fast, beautiful documentation portal hosted on GitHub Pages with instant search, dark mode, and multi-language support. |
| **API Docs** | Dokka (Kotlin) | **Dokka** | Apache 2.0 (JetBrains) | Automatic API documentation generator for Kotlin source code across all modular libraries. |

---

## 24. Accessibility (a11y) & Inclusivity Tools

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **Testing Tools** | Android Accessibility Scanner vs Axe-Android | **Axe-Android (Deque)** | MIT | Automated accessibility checks during unit and UI tests for touch targets, content descriptions, and contrast ratios. |

---

## 25. Localization (i18n / L10n) & Translation Engine

| Subsystem Component | Leading Candidates | Selected Winner | License & Maintenance | Justification & RTIQA Integration Plan |
| :--- | :--- | :--- | :--- | :--- |
| **L10n Server** | Weblate vs Tolgee vs Crowdin | **Weblate** | GPLv3 | Continuous localization system integrating directly with GitHub PRs for crowdsourced Arabic and English translation management. |

---

## Implementation Roadmap & Governance

All future architectural pull requests MUST reference the specific selection from this acquisition catalog. Modifications to these selected standards require formal review and approval by the RTIQA Architecture Board.
