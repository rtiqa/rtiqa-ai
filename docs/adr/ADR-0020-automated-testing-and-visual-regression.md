# ADR-0020: Robolectric JVM Testing & Roborazzi Screenshot Verification

## Metadata
- **Decision ID**: ADR-0020
- **Title**: Automated QA Architecture: Robolectric Fast JVM Unit Testing & Roborazzi Visual Regression Verification
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: Automated Testing & Visual Quality Assurance

---

## Context
RTIQA requires continuous automated quality assurance across its Jetpack Compose UI components, ViewModels, repository logic, and database migrations without relying on slow, flaky, hardware-dependent Android emulators or ADB instances.

## Problem Statement
Traditional instrumented Android tests (`androidTest`) require running emulators, introducing massive CI build delays (15m+ per run), intermittent network flakiness, and high compute infrastructure costs. Manual UI inspection misses subtle visual regression bugs across app updates.

## Alternatives Considered

1. **Robolectric + Roborazzi Stack**: Fast JVM-based simulation of the Android framework paired with high-speed Compose visual screenshot regression testing.
2. **Instrumentation Tests (Espresso + Emulator)**: Standard Android device emulator testing.
3. **Paparazzi**: LayoutLib-based screenshot testing engine by CashApp.

## Engineering Comparison

| Dimension | Robolectric + Roborazzi | Instrumentation Tests | Paparazzi |
| :--- | :--- | :--- | :--- |
| **Execution Environment**| Local JVM (no emulator) | Android Emulator / ADB | Local JVM |
| **Execution Speed** | Ultra-Fast (~50ms / test) | Slow (Minutes per test run) | Ultra-Fast |
| **User Interaction** | Full Click / Drag / Text simulation | Full simulation | Rendering only (No interaction) |
| **Visual Screenshots**| Native Roborazzi screenshots | Espresso Screenshots | Paparazzi Screenshots |

## Advantages
- Robolectric executes tests directly on the local JVM in seconds, simulating Android SDK classes (Resources, Context, Canvas) accurately.
- Roborazzi captures high-precision PNG screenshot snapshots of Jetpack Compose UI components during local JVM test runs.
- Instant visual regression diffing in GitHub Pull Requests: automatically highlights visual bugs, typography misalignments, and RTL layout glitches.
- Enables 100% test coverage verification in headless cloud CI runners without requiring nested virtualization hardware for emulators.

## Disadvantages
- Initial reference screenshot generation must be committed to git or managed via LFS storage.

## Risks
- Minor font rendering differences across host OS environments (Linux CI vs macOS dev machine).
- Mitigated by running canonical screenshot verification inside standardized Docker container environments.

## Long-term Maintenance
Robolectric maintained by Google Android Team and open-source contributors; Roborazzi maintained by Takahirom.

## Performance Impact
- Executes hundreds of UI unit and visual tests in under 30 seconds on standard developer laptops.

## Security Impact
- Ensures that security-critical user flows (login validation, permission prompts) are verified automatically before every release.

## Scalability Impact
- Rapid feedback loops allow engineering teams to scale test suites into thousands of unit and UI tests without slowing down CI development.

## Cost Impact
- 100% free open-source tooling (Apache 2.0); cuts cloud CI emulator runner bills to zero.

## Why RTIQA Selected This Solution
Robolectric and Roborazzi deliver fast local JVM testing and visual regression verification, ensuring RTIQA UI components maintain precision.

## Future Re-evaluation Criteria
Re-evaluate if Google's official Compose Desktop testing framework provides faster JVM rendering with identical Android SDK shadow fidelity.
