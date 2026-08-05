# ADR-0006: Reactive Room + WorkManager Background Delta Sync Engine

## Metadata
- **Decision ID**: ADR-0006
- **Title**: Native Android Reactive Room + WorkManager Background Delta Synchronization Strategy
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: Offline Synchronization & Network Resilience

---

## Context
Students utilizing RTIQA in emerging markets or rural regions frequently experience unstable, high-latency, or completely disconnected mobile networks. Learning progress, quiz submissions, and notes taken while offline must automatically sync back to the cloud when connectivity is restored.

## Problem Statement
Relying on custom inline HTTP network calls fails when offline, resulting in lost user data, broken UX, and duplicate records. Third-party sync frameworks often require proprietary backends or rigid schema transformations that compromise Clean Architecture.

## Alternatives Considered

1. **Native Room + WorkManager Delta Sync Architecture**: Standard AndroidX reactive database persistence combined with background persistent worker queues.
2. **PowerSync**: Client-side database sync engine connecting Postgres to client SQLite.
3. **WatermelonDB**: React Native offline sync engine.

## Engineering Comparison

| Dimension | Room + WorkManager Engine | PowerSync Engine | WatermelonDB Engine |
| :--- | :--- | :--- | :--- |
| **Android Integration** | 100% Native AndroidX API | SDK Wrapper Layer | Non-native JS engine |
| **Execution Reliability**| Guaranteed execution by Android OS | Dependent on app process | Process dependent |
| **Conflict Resolution**| Granular Kotlin domain handlers | Server-side Postgres triggers| Custom JS handlers |
| **Dependency Footprint**| Zero extra dependencies | Requires PowerSync Cloud/Service| Heavy JS runtime |

## Advantages
- Guaranteed execution by Android OS (`WorkManager`) even if the user exits the app or restarts the device.
- Local-first architecture: UI reads directly from Room via Flow; background workers sync deltas asynchronously.
- Vector clock & timestamp conflict resolution handles simultaneous edits cleanly without data loss.
- Battery and network constraint enforcement (e.g., defer large media downloads until connected to unmetered Wi-Fi).

## Disadvantages
- Requires writing custom delta payload queueing logic for local mutations.

## Risks
- Sybil sync requests or race conditions during multi-device login sessions.
- Mitigated by server-side transaction locks and client sync state versioning.

## Long-term Maintenance
Built entirely on official Google AndroidX libraries (`Room` + `WorkManager`), ensuring 100% backward compatibility and zero third-party deprecation risk.

## Performance Impact
- Zero main thread UI blocking; state mutations are instantaneously rendered from local cache.

## Security Impact
- Offline sync payloads queued in encrypted SQLCipher tables before transmission over TLS 1.3 endpoints.

## Scalability Impact
- Offloads read spikes from backend servers by serving 90%+ of client views directly from device local cache.

## Cost Impact
- 100% open-source solution under Apache 2.0 with zero recurring sync license costs.

## Why RTIQA Selected This Solution
This native architecture provides absolute data safety, guaranteed execution, battery-conscious network scheduling, and complete autonomy over conflict resolution.

## Future Re-evaluation Criteria
Re-evaluate if PowerSync or an equivalent KMP-native sync engine achieves zero-friction integration with Ktor microservices and SQLCipher.
