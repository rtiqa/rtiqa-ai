# RES-0016: Rejected Alternatives: Flutter & React Native Research Report

## Executive Summary
Cross-platform mobile application frameworks attempt to share UI code across Android and iOS by introducing custom rendering engines or JavaScript bridges. This report provides a detailed technical evaluation of Flutter (Dart) and React Native (Fabric/TypeScript), documenting why both frameworks were formally rejected for the RTIQA mobile platform.

---

## Technical Metadata & Overview
- **Technologies Evaluated**: Flutter (Google / Dart) & React Native (Meta / JavaScript/TypeScript)
- **Evaluation Subsystem**: Mobile Client UI & System Architecture
- **Official Websites**: [flutter.dev](https://flutter.dev) / [reactnative.dev](https://reactnative.dev)
- **Primary Domain**: Cross-Platform Mobile Application Development

---

## Detailed Evaluation & Architecture

### 1. Flutter (Dart / Impeller Canvas)
Flutter bypasses native Android UI components entirely, drawing every pixel manually using its custom 2D rendering engine (Skia / Impeller). 
- **RTL & Arabic Text Alignment Bugs**: Flutter relies on custom bidirectional text wrappers (`BidiFormatter`). In complex Arabic rich text layouts (mixing inline math, bold Arabic terms, and English code snippets), Flutter exhibits persistent text clipping and baseline misalignment glitches.
- **Binary Footprint Penalty**: Including the Dart VM runtime and Impeller engine inflates the compiled APK baseline by +15 MB to +22 MB.
- **Android System Integration Friction**: Accessing native Android Jetpack APIs (e.g., Room, WorkManager, Media3 ExoPlayer) requires complex, error-prone platform channels and JNI serializations.

### 2. React Native (Fabric JSI / Hermes JS Engine)
React Native runs JavaScript code on Meta's Hermes JavaScript engine, communicating with native views via the Fabric C++ JavaScript Interface (JSI).
- **Bridge Overhead**: High-frequency state updates (such as real-time audio waveform rendering during AI voice tutoring or 60fps video player playback controls) stall across the JavaScript bridge.
- **State Drift & Memory Leaks**: Managing complex offline database states in JavaScript while syncing with native SQLite modules causes state drift and memory overhead.
- **Third-Party Module Instability**: React Native relies heavily on third-party community packages for core features, leading to version mismatch issues during major Android SDK upgrades.

---

## Direct Architectural Comparison

| Dimension | Selected: Jetpack Compose | Rejected: Flutter | Rejected: React Native |
| :--- | :--- | :--- | :--- |
| **Native Integration** | 100% Native Kotlin DSL | Custom Canvas Skia Render | JavaScript Bridge / Fabric JSI |
| **Arabic RTL Quality** | Native Jetpack Layout Insets | Custom Bidi Layout Wrappers | Inconsistent native text alignment |
| **APK Footprint** | Zero extra engine size | +15 MB to +22 MB engine | +12 MB Hermes JS runtime |
| **State Management** | Native `StateFlow` / UDF | Bloc / Provider / Riverpod | Redux / Zustand / MobX |
| **Jetpack API Access** | Direct Compile-Time Native | Via Custom Platform Channels| Via Native Module Wrappers |

---

## Key Reasons for RTIQA Rejection

1. **Flawless Arabic (RTL) Rendering Requirement**: RTIQA serves native Arabic educational environments. Jetpack Compose provides native RTL layout mirroring, while Flutter and React Native introduce subtle text rendering and touch target bugs.
2. **Zero Bridge Latency for Edge AI**: RTIQA runs local C++ AI models (Whisper.cpp, Piper TTS, MediaPipe LLM) directly on hardware NPUs. Binding C++ engines to Kotlin is simple via JNI, whereas binding C++ to Flutter (Dart FFI) or React Native (JSI wrappers) adds unnecessary architectural complexity.
3. **Android Platform Alignment**: Jetpack Compose is Google's official native UI standard, receiving direct OS support, accessibility optimization, and performance tooling.

---

## References & Citations
1. Android Engineering Audit: *Native Compose vs Cross-Platform Rendering Performance* (2026).
2. RTIQA Internationalization Benchmark: *Arabic Typography & Bidi Text Layout Quality Evaluation*.
