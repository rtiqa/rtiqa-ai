# 🗺️ RTIQA 2026 Product & Engineering Roadmap

This document outlines the strategic product vision, feature milestones, and technical enhancements planned for **RTIQA (رتقاء)** throughout 2026.

---

## 📅 Q1 2026: Foundation & Modular Architecture (v1.0.0) — *COMPLETED*
- [x] **Multi-Module Gradle Refactoring**: Rearchitected application into core domain, database, UI, network, and feature modules.
- [x] **Jetpack Compose M3 Design System**: Implemented cohesive RTL/LTR bilingual design system with dynamic light and dark themes.
- [x] **Offline-First Persistence**: Room Database integration with reactive Flow queries, automatic database migrations, and pre-populated educational content.
- [x] **Gemini 1.5 AI Tutor**: Context-aware AI assistant integration with automatic local fallback when offline.
- [x] **Microservice & Container Stack**: Docker Compose & Helm Charts for Keycloak IAM, Directus CMS, LiveKit SFU, Typesense, and Qdrant Vector DB.

---

## 📅 Q2 2026: Real-time Collaboration & AI RAG Integration (v1.1.0)
- [ ] **LiveKit Virtual Classroom Integration**: Native Android WebRTC audio/video call integration with screen sharing for remote tutoring sessions.
- [ ] **RAG Textbook Search Engine**: Native Qdrant vector embedding pipeline to index full PDF textbooks for grounded AI answers with exact page citations.
- [ ] **Keycloak OIDC PKCE Auth**: Native OAuth2/OIDC login integration with SSO support for educational institutions.
- [ ] **Typesense Sub-20ms Search**: Instant course, lesson, and user lookup in mobile UI connected to Typesense cluster.

---

## 📅 Q3 2026: Adaptive Learning Analytics & Gamification (v1.2.0)
- [ ] **Adaptive Knowledge Graphs**: Machine learning algorithm tracking student skill mastery and dynamically recommending remediation lessons.
- [ ] **Expanded Gamification**: Multi-student leaderboards, weekly learning leagues, custom avatars, and verifiable digital certificates.
- [ ] **Parent & Educator Portal**: Comprehensive telemetry metrics dashboard for tracking student assignment completion and focus duration.

---

## 📅 Q4 2026: Enterprise Multi-Tenancy & Global Scale (v2.0.0)
- [ ] **Multi-School District Multi-Tenancy**: Isolated database schemas and branded custom app themes per school district.
- [ ] **On-Device LLM (Gemini Nano / MediaPipe)**: Zero-latency on-device AI inference for offline speech analysis and AI tutoring.
- [ ] **ChromeOS & Tablet Canonical Layouts**: Native split-pane navigation rail and list-detail views optimized for large screens.

---

## 💬 Feature Feedback & Community Input
Have ideas for the roadmap? Open a feature request in [GitHub Discussions](https://github.com/rtiqa/mobile/discussions) or submit a feature issue!
