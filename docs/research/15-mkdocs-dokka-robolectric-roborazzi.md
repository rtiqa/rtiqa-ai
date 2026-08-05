# RES-0015: MkDocs Material, Dokka, Robolectric & Roborazzi Research Report

## Executive Summary
Comprehensive technical documentation and fast automated JVM testing ensure codebase maintainability and visual precision across development iterations. This report evaluates MkDocs Material (documentation portal), JetBrains Dokka (Kotlin API doc generator), Robolectric (fast JVM Android simulation), and Roborazzi (Compose visual regression screenshot testing) for RTIQA.

---

## Technical Metadata
- **Technologies**: MkDocs Material, JetBrains Dokka, Robolectric & Roborazzi
- **Primary Domain**: Technical Documentation Systems & Fast Automated JVM Testing
- **Official Documentation**: 
  - MkDocs Material: [squidfunk.github.io/mkdocs-material](https://squidfunk.github.io/mkdocs-material)
  - Dokka: [kotlinlang.org/docs/dokka-expressive.html](https://kotlinlang.org/docs/dokka-expressive.html)
  - Robolectric: [robolectric.org](https://robolectric.org)
  - Roborazzi: [github.com/takahirom/roborazzi](https://github.com/takahirom/roborazzi)
- **GitHub Repositories**: `squidfunk/mkdocs-material`, `Kotlin/dokka`, `robolectric/robolectric` & `takahirom/roborazzi`
- **Licenses**: MIT / Apache 2.0 Open Source
- **Maintainers**: Martin Donath, JetBrains, Google Android Team & Takahirom
- **Community Activity**: Extremely high (Industry standards for Kotlin docs and testing)

---

## Architecture & Internals

```
+-------------------------------------------------------------+
|                 Documentation System Architecture           |
|  Markdown Files (/docs) -> MkDocs Material -> Static HTML   |
|  KDoc Source Comments  -> JetBrains Dokka  -> API Reference |
+-------------------------------------------------------------+
|                 Automated JVM Testing Pipeline              |
|  Robolectric: Simulates Android SDK on Local JVM (No ADB)   |
|  Roborazzi: Captures PNG Screenshots of Compose UI          |
+-------------------------------------------------------------+
```

### Technical Breakdown
1. **MkDocs Material**: Python static site generator that builds responsive documentation portals with search and dark-mode themes directly from Markdown files.
2. **JetBrains Dokka**: Official documentation engine for Kotlin, parsing `KDoc` comments across Kotlin libraries to produce standard HTML API documentation.
3. **Robolectric Framework**: Runs Android unit tests directly on the local JVM in seconds by intercepting Android framework calls (`Shadow` classes), eliminating the need for slow Android emulators.
4. **Roborazzi Engine**: Screenshot testing tool that renders Jetpack Compose components during local JVM Robolectric test runs, saving PNG screenshots to detect visual regression bugs automatically in Pull Requests.

---

## Advantages
- **Fast Local JVM Execution**: Robolectric tests run in milliseconds without launching heavy emulators or ADB bridges.
- **Visual Regression Automation**: Roborazzi automatically highlights unintended UI layout shifts, font overflow, and RTL alignment bugs in PR reviews.
- **Single Source of Truth Documentation**: Documentation lives alongside Kotlin code in Git, updated via standard Pull Requests.
- **Arabic Search Support**: MkDocs Material includes client-side search engines capable of querying both Arabic and English text.

## Disadvantages
- **Screenshot Reference Storage**: Reference baseline screenshots must be managed using Git LFS or storage buckets.

---

## Scalability & Performance
- **Sub-30 Second Test Execution**: Hundreds of UI unit and visual tests complete in under 30 seconds on local developer machines.
- **Instant Static Hosting**: MkDocs static HTML output loads instantly with zero backend database processing overhead.

---

## Security & Privacy Impact
- **Static Hosting Security**: Static documentation site generation eliminates backend database attack vectors.
- **Automated Validation**: Verifies security-critical UI flows (login inputs, permissions dialogs) on every CI commit.

---

## Enterprise Adoption & Major Users
- **MkDocs Material**: AWS, Square, Google Open Source, Spotify.
- **Dokka**: Official documentation tool for Kotlin, AndroidX, and Ktor.
- **Robolectric & Roborazzi**: Google Android Team, Uber, Lyft, CashApp.

---

## Comparison with Alternatives

| Dimension | Selected Testing & Docs Stack | Emulator Instrumentation Tests | Confluence / Docusaurus |
| :--- | :--- | :--- | :--- |
| **Test Environment** | Fast Local JVM (Robolectric) | Slow Android Emulator | N/A |
| **Execution Speed** | ~50ms per test | 15+ minutes per test run | N/A |
| **Visual QA** | Roborazzi PNG Diffing | Manual UI Check | N/A |
| **Docs Generator** | MkDocs Material + Dokka | N/A | Proprietary Wiki |

---

## Why RTIQA Selected This Solution
This documentation and testing architecture provides RTIQA with fast local JVM unit testing, visual screenshot regression verification, and technical documentation portals without relying on slow emulators.

---

## Future Outlook
JetBrains Dokka is integrating deeper Kotlin Multiplatform (KMP) documentation parsing, while Robolectric continues to shadow new Android API levels seamlessly.

---

## References & Citations
1. Google Android Developer Documentation: *Robolectric Testing Guidelines* (2026).
2. JetBrains Kotlin Team: *Dokka Documentation Engine Specification*.
