# ADR-0013: Matrix.org Decentralized Protocol for Peer-to-Peer Study Groups

## Metadata
- **Decision ID**: ADR-0013
- **Title**: Decentralized Chat & Peer-to-Peer Communication via Matrix.org Open Protocol
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: Realtime Chat & Community Infrastructure

---

## Context
RTIQA enables peer-to-peer student study groups, class discussion channels, teacher-parent messaging, and instant notification alerts across educational institutions.

## Problem Statement
Proprietary messaging backends lock communication data into siloed databases, lack end-to-end encryption (E2EE), and fail to support cross-school federation. Custom WebSocket messaging logic is prone to dropped messages and sync bugs.

## Alternatives Considered

1. **Matrix.org Protocol (Synapse / Dendrite Server)**: Open, decentralized, end-to-end encrypted messaging standard.
2. **Stream Chat SDK**: Commercial chat-as-a-service platform.
3. **Custom WebSocket Chat**: In-house WebSocket message broker.

## Engineering Comparison

| Dimension | Matrix.org | Stream Chat | Custom WebSockets |
| :--- | :--- | :--- | :--- |
| **Architecture** | Open Decentralized Federation | Centralized SaaS | Centralized Backend |
| **Encryption** | Native E2EE (Olm / Megolm) | TLS in transit only | Requires custom E2EE |
| **Open Source** | 100% Apache 2.0 | Proprietary Closed SDK | Custom In-House |
| **Federation** | Cross-school server federation | No federation | No federation |

## Advantages
- Matrix is an open, international standard for secure, decentralized messaging.
- Native End-to-End Encryption (E2EE) using Olm and Megolm cryptographic ratchets protects student privacy.
- Federated architecture allows authorized students from different schools to participate in joint research study groups securely.
- Rich ecosystem of open-source client SDKs (Matrix Rust SDK / Kotlin SDK bindings) simplifying mobile integration.

## Disadvantages
- Self-hosting Matrix homeservers (Synapse/Dendrite) requires database storage management for message history.

## Risks
- Matrix state resolution complexity during sudden network splits.
- Mitigated by adopting Dendrite (Go-based next-gen homeserver) for reduced memory overhead.

## Long-term Maintenance
Governed by the non-profit Matrix.org Foundation with worldwide adoption across government, edtech, and defense sectors.

## Performance Impact
- Sub-50ms message delivery via persistent WebSocket connections.

## Security Impact
- End-to-end encryption guarantees that even platform administrators cannot inspect private student study group conversations.

## Scalability Impact
- Matrix federated rooms scale to thousands of concurrent users across distributed server nodes.

## Cost Impact
- Open-source protocol and reference servers eliminate monthly per-user active user (MAU) chat licensing fees.

## Why RTIQA Selected This Solution
Matrix provides RTIQA with an enterprise-grade, privacy-first, decentralized messaging network that guarantees student privacy and supports inter-school collaboration.

## Future Re-evaluation Criteria
Re-evaluate if Matrix.org specification changes cause breaking changes in mobile Kotlin SDK bindings.
