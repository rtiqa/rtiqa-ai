# ADR-0004: Room Database with KSP Annotation Processing & SQLCipher Encryption

## Metadata
- **Decision ID**: ADR-0004
- **Title**: Local Persistence Architecture using Jetpack Room, KSP, and SQLCipher Encryption
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: Local Data Persistence & Database Security

---

## Context
RTIQA requires a robust local persistence layer to store offline course modules, lesson progress, quiz attempts, user credentials, and AI chat histories safely on the Android client device.

## Problem Statement
Unencrypted SQLite databases expose sensitive student progress records, exam answers, and personal profile data to malicious local root inspection or physical device theft. Standard ORMs using heavy Java reflection degrade runtime application startup speed.

## Alternatives Considered

1. **Jetpack Room + KSP + SQLCipher**: Google's standard Android SQLite ORM powered by Kotlin Symbol Processing (KSP) and AES-256 database encryption.
2. **Realm / MongoDB Realm**: Object-oriented mobile database engine.
3. **Raw SQLite API**: Low-level C-based SQLite operations.

## Engineering Comparison

| Dimension | Room + KSP + SQLCipher | Realm Mobile DB | Raw SQLite |
| :--- | :--- | :--- | :--- |
| **Compile-time Verification**| 100% SQL query checking at compile time | No SQL verification | Zero compile checking |
| **Encryption** | SQLCipher 256-bit AES | Built-in AES-256 | Requires custom SQLite build |
| **Code Generation** | Fast KSP (no kapt reflection) | Custom bytecode weaving | None |
| **Android Integration** | Official Jetpack component | Third-party engine | Low-level C API |

## Advantages
- Full compile-time verification of SQL queries prevents runtime syntax crashes.
- KSP compiler plugin builds 2x faster than legacy `kapt` annotation processing.
- Seamless integration with RxJava and Kotlin `Flow` for reactive UI state updates.
- SQLCipher delivers transparent 256-bit AES database encryption at rest.

## Disadvantages
- Incremental APK size overhead for SQLCipher native JNI libraries (~2.5 MB).

## Risks
- Migration script errors during major database schema updates.
- Mitigated by automated Room migration testing (`MigrationTest`) enforced in CI pipelines.

## Long-term Maintenance
Maintained directly by Google's Jetpack Core team; SQLCipher maintained by Zetetic.

## Performance Impact
- Microsecond query latency with indexed foreign key tables.
- Zero main-thread blocking operations through forced coroutine dispatcher execution.

## Security Impact
- All SQLite database files (`rtiqa_database.db`) are encrypted on disk; unreadable without KeyStore-backed key material.

## Scalability Impact
- Easily scales to tens of thousands of local offline lesson entities and indexed search entries.

## Cost Impact
- 100% free open-source software under Apache 2.0 / BSD license.

## Why RTIQA Selected This Solution
Room is the industry gold standard for Android persistence, offering type-safe reactive streams, compile-time query safety, and transparent full-disk database encryption.

## Future Re-evaluation Criteria
Re-evaluate if Kotlin Multiplatform (KMP) `androidx.room` reaches 100% feature parity on desktop and iOS with zero performance regression.
