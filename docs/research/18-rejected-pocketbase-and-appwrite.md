# RES-0018: Rejected Alternatives: PocketBase & Appwrite Research Report

## Executive Summary
Lightweight open-source Backend-as-a-Service (BaaS) frameworks—specifically PocketBase (embedded Go/SQLite) and Appwrite (Docker-based microservices)—were evaluated as potential backend gateways for RTIQA. This report documents the technical reasons for rejecting both in favor of Supabase PostgreSQL.

---

## Technical Metadata & Overview
- **Technologies Evaluated**: PocketBase (Go / SQLite) & Appwrite (Docker / MariaDB / Redis)
- **Evaluation Subsystems**: Relational Persistence & Backend BaaS
- **Official Websites**: [pocketbase.io](https://pocketbase.io) / [appwrite.io](https://appwrite.io)

---

## Detailed Technical Evaluation

### 1. PocketBase (Single-Binary Go / Embedded SQLite)
PocketBase is an open-source BaaS packaged as a single Go binary, embedding SQLite for data storage, real-time WebSockets, and built-in user authentication.
- **Embedded SQLite Scale Limits**: SQLite operates as a single-file database. While excellent for client-side storage, SQLite cannot handle high-concurrency multi-region server write spikes without lock contention (`SQLITE_BUSY`).
- **No Native Vector Extension**: PocketBase lacks native vector search capabilities (`pgvector`), requiring an external vector database for textbook RAG queries.
- **Lack of Multi-Tenant Security Rules**: Lacks declarative Row Level Security (RLS) policies at the database engine level.

### 2. Appwrite (Docker Microservices / MariaDB)
Appwrite is an open-source BaaS platform providing web/mobile APIs for authentication, database management, file storage, and serverless functions.
- **Complex Multi-Container Footprint**: Appwrite deploys over 15 separate Docker microservice containers (Redis, MariaDB, InfluxDB, Telegraf, Executor) just for baseline operations.
- **NoSQL Abstraction over Relational Storage**: Wraps relational database tables in a rigid document-store abstraction layer, preventing developers from using raw SQL, CTEs, or complex joins.
- **No Vector Search Support**: Lacks native vector embedding indexing for AI retrieval pipelines.

---

## Direct Technical Comparison

| Dimension | Selected: Supabase + Postgres | Rejected: PocketBase | Rejected: Appwrite |
| :--- | :--- | :--- | :--- |
| **Database Engine** | PostgreSQL (Relational) | Embedded SQLite | MariaDB (Document Abstraction)|
| **Vector Search** | `pgvector` Built-in | None | None |
| **Deployment** | Docker Compose / Helm | Single Go Binary | 15+ Docker Containers |
| **Multi-Tenancy** | PostgreSQL Native RLS | Basic Admin Rules | Custom Document Permissions |
| **Relational SQL** | 100% Native SQL & Join support| Limited SQLite | Abbreviated Document API |

---

## Key Reasons for RTIQA Rejection

1. **Relational Scaling & Concurrency**: Enterprise school systems generate high concurrent write volume during exam submissions. PostgreSQL handles concurrent server transactions far better than embedded SQLite.
2. **Integrated Vector Retrieval (`pgvector`)**: Supabase PostgreSQL allows RTIQA to perform vector similarity search for textbook RAG directly inside relational queries, eliminating additional vector database operational overhead for smaller deployments.
3. **Database-Enforced Security (RLS)**: PostgreSQL Row Level Security guarantees multi-tenant isolation at the storage layer, whereas PocketBase and Appwrite enforce security higher up in application code.

---

## References & Citations
1. RTIQA Database Benchmark Report: *PostgreSQL vs Embedded SQLite Concurrency Performance* (2026).
2. Supabase vs Appwrite Architectural Analysis: *Relational Engine vs Document Abstraction Layers*.
