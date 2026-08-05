# 🔬 RTIQA Open-Source Engineering Research Library

**Project**: RTIQA (رتقاء) — Enterprise Smart Learning & Knowledge Ecosystem  
**Directory**: `docs/research/`  
**Governance**: RTIQA Technology & Architecture Council  

Welcome to the **RTIQA Open-Source Engineering Research Library**. This repository houses deep technical evaluations, benchmark analyses, architectural breakdowns, security assessments, and enterprise adoption reports for both selected technologies and evaluated alternatives across all 25 core subsystems of RTIQA.

---

## 📖 Research Index

### 🟢 Core Adopted Technologies
| Document ID | Research Topic | Primary Technologies Covered | Subsystem |
| :--- | :--- | :--- | :--- |
| **[RES-0001](01-jetpack-compose-and-m3.md)** | Modern Declarative Mobile UI | Jetpack Compose, Material Design 3, Compose Navigation | Mobile Client UI |
| **[RES-0002](02-ktor-server.md)** | Kotlin Asynchronous Microservices | Ktor Server, Kotlin Coroutines, Netty Engine | Backend Microservices |
| **[RES-0003](03-supabase-and-postgresql.md)** | Relational Gateway & Open BaaS | Supabase, PostgreSQL, Row Level Security (RLS) | Relational Database & BaaS |
| **[RES-0004](04-room-sqlcipher-workmanager.md)** | Offline-First Data & Security | Room ORM, SQLCipher AES-256, AndroidX WorkManager | Offline Sync & Security |
| **[RES-0005](05-hilt-and-coil.md)** | Dependency Injection & Image Engine | Dagger Hilt, Coil Image Loader, Coroutine Pipelines | Architecture & Media |
| **[RES-0006](06-media3-and-livekit.md)** | Video Streaming & Virtual Classroom | AndroidX Media3 ExoPlayer, LiveKit WebRTC SFU | Video & Live Classroom |
| **[RES-0007](07-keycloak-iam.md)** | Enterprise Identity & Access | Red Hat Keycloak, OpenID Connect (OIDC), OAuth 2.0 | Authentication & IAM |
| **[RES-0008](08-directus-cms.md)** | Headless Content Authoring | Directus Headless CMS, PostgreSQL Data Mirroring | Curriculum Authoring |
| **[RES-0009](09-erpnext-and-canvas-lms.md)** | Enterprise ERP & LMS Standards | ERPNext Education, Instructure Canvas LMS, LTI 1.3, xAPI | School SIS & LMS |
| **[RES-0010](10-typesense-and-qdrant.md)** | High-Speed Search & Vector RAG | Typesense Search Engine, Qdrant Vector DB, pgvector | Search & Vector RAG |
| **[RES-0011](11-gemini-langchain4j-mediapipe.md)** | Cloud & On-Device AI Tutoring | Gemini 1.5 Flash, LangChain4j, MediaPipe LLM Inference | Intelligent AI Tutor |
| **[RES-0012](12-whisper-piper-mlkit.md)** | Multimodal Edge Speech & Vision | Whisper.cpp (STT), Piper Neural TTS, ML Kit OCR | Speech & Vision Processing |
| **[RES-0013](13-posthog-sentry-opentelemetry.md)** | Product Analytics & Observability | PostHog Analytics, Sentry Error Tracking, OpenTelemetry | Telemetry & Monitoring |
| **[RES-0014](14-github-actions-fastlane-codeql.md)** | Automated CI/CD & Security SAST | GitHub Actions, Fastlane Automation, CodeQL SAST | DevOps & Security SAST |
| **[RES-0015](15-mkdocs-dokka-robolectric-roborazzi.md)** | Docs & Automated JVM Testing | MkDocs Material, JetBrains Dokka, Robolectric, Roborazzi | Docs & Automated Testing |

---

### 🔴 Evaluated & Rejected Alternatives
| Document ID | Research Topic | Evaluated Technologies | Reason for Rejection |
| :--- | :--- | :--- | :--- |
| **[RES-0016](16-rejected-flutter-react-native.md)** | Cross-Platform Mobile Frameworks | Flutter (Dart), React Native (Fabric JS) | Native RTL bugs, bridge overhead, extra binary size |
| **[RES-0017](17-rejected-spring-boot-and-firebase.md)** | Monolithic Backend & Proprietary SaaS | Spring Boot, Google Firebase Suite | Heavy JVM cold-starts, closed SaaS vendor lock-in |
| **[RES-0018](18-rejected-pocketbase-and-appwrite.md)** | Lightweight BaaS Alternatives | PocketBase (SQLite), Appwrite (Docker) | Lack of multi-region postgres scaling & pgvector |
| **[RES-0019](19-rejected-openedx-and-moodle.md)** | Legacy Educational Platforms | Open edX, Moodle PHP Engine | High deployment overhead, monolithic legacy code |
| **[RES-0020](20-rejected-strapi-and-hasura.md)** | Alternative CMS & GraphQL BaaS | Strapi, Hasura GraphQL Engine | Custom ORM lock-in, complex licensing models |
| **[RES-0021](21-rejected-elasticsearch-and-meilisearch.md)** | Legacy & Heavy Search Engines | Elasticsearch, Meilisearch | High JVM RAM footprint, index memory constraints |
| **[RES-0022](22-rejected-chroma-milvus-weaviate.md)** | Proprietary & Heavy Vector DBs | ChromaDB, Milvus, Weaviate Cloud | High operational complexity, cloud SaaS costs |

---

## 🏛️ Research Methodology & Standards

Every research entry in this library undergoes rigorous peer evaluation based on six core pillars:
1. **Source Code Inspection**: Evaluation of architectural patterns, memory allocations, and threading models.
2. **Security & Cryptography Audit**: Verification of credential handling, zero-trust network support, and data privacy compliance (GDPR/FERPA).
3. **Performance Benchmarking**: Micro-benchmark latency, cold-start RAM footprint, and network packet bandwidth.
4. **Community Activity & Governance**: Verification of commit cadences, open issue counts, and organizational backing.
5. **Licensing Compliance**: Preference for OSI-approved permissive licenses (Apache 2.0, MIT, BSD).
6. **Native RTIQA Alignment**: Direct compatibility with Kotlin, Jetpack Compose, Clean Architecture, and multi-tenant Arabic/English learning environments.
