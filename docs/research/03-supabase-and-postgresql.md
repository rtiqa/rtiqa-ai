# RES-0003: Supabase & PostgreSQL Architecture Research Report

## Executive Summary
PostgreSQL is the world's most advanced open-source relational database. Supabase provides an open-source Backend-as-a-Service (BaaS) architecture built around PostgreSQL, offering instant REST/GraphQL APIs, Row Level Security (RLS), real-time database change subscriptions, and vector similarity search (`pgvector`). This report evaluates Supabase and PostgreSQL for RTIQA.

---

## Technical Metadata
- **Technology Name**: Supabase BaaS & PostgreSQL Relational Database Engine
- **Primary Domain**: Relational Database, Real-time Gateway & Vector Storage
- **Official Website**: [supabase.com](https://supabase.com) / [postgresql.org](https://www.postgresql.org)
- **Official Documentation**: [supabase.com/docs](https://supabase.com/docs) / [postgresql.org/docs](https://www.postgresql.org/docs)
- **GitHub Repository**: [github.com/supabase/supabase](https://github.com/supabase/supabase)
- **License**: Apache License 2.0 / PostgreSQL License (100% Open Source)
- **Maintainer**: Supabase Inc. & PostgreSQL Global Development Group
- **Community Activity**: Extremely high (72k+ GitHub stars, massive global community)

---

## Architecture & Internals
Supabase is not a monolithic proprietary engine; it is an orchestrated collection of open-source components centered around PostgreSQL.

```
+-------------------------------------------------------------+
|                      RTIQA Mobile / Ktor                    |
+-------------------------------------------------------------+
|    PostgREST (Auto REST)  |  GoTrue (Auth) | Realtime (WS)  |
+-------------------------------------------------------------+
|           PostgreSQL Relational Engine (v16+)               |
|      + Row Level Security (RLS)  + pgvector extension       |
+-------------------------------------------------------------+
|                   Storage (MinIO / S3)                      |
+-------------------------------------------------------------+
```

### Core Components
1. **PostgreSQL Relational Core**: ACID-compliant transactional relational storage supporting JSONB, foreign keys, spatial data (PostGIS), and vector extensions (`pgvector`).
2. **PostgREST**: Automatically turns PostgreSQL database schemas into secure, compliant REST APIs.
3. **Row Level Security (RLS)**: Enforces tenant data isolation directly at the PostgreSQL table engine level based on JWT user claims.
4. **Realtime Engine**: Elixir-based server broadcasting database `INSERT`/`UPDATE`/`DELETE` events over WebSockets.

---

## Advantages
- **PostgreSQL Power**: Complete access to SQL features, CTEs, window functions, trigram indexing, and foreign key integrity.
- **Row Level Security (RLS)**: Guarantees multi-tenant data isolation directly inside the database, preventing unauthorized data access even if application code has bugs.
- **`pgvector` Extension**: Enables vector similarity search directly alongside relational data for RAG textbook queries.
- **100% Self-Hostable**: Deployable via Docker Compose or Kubernetes Helm charts with zero cloud lock-in.

## Disadvantages
- **Schema Management Discipline**: Requires structured SQL migrations (Flyway / Liquibase / Supabase CLI) to manage schema changes across environments.

---

## Scalability & Performance
- **Connection Pooling**: PgBouncer integration handles tens of thousands of concurrent microservice database connections effortlessly.
- **Read Scalability**: Supports PostgreSQL read-replicas for scaling high-frequency query workloads globally.
- **Sub-10ms Queries**: Indexed relational queries execute in sub-10ms response times.

---

## Security & Privacy Impact
- **Database-Level Authorization**: RLS policies ensure students can only query their own quiz attempts, school records, and enrollment data.
- **Zero Third-Party Cloud Transmission**: Self-hosted PostgreSQL clusters guarantee compliance with FERPA, COPPA, and regional student data privacy mandates.

---

## Enterprise Adoption & Major Users
- **Mozilla**: Core web telemetry data platforms.
- **PwC, KPMG, Toyota**: Enterprise operational analytics.
- **GitHub, Instagram, Reddit, Apple**: Heavy reliance on PostgreSQL infrastructure.

---

## Comparison with Alternatives

| Dimension | Supabase + Postgres | Firebase Firestore | PocketBase |
| :--- | :--- | :--- | :--- |
| **Data Engine** | PostgreSQL Relational | NoSQL Document Store | Embedded SQLite |
| **Multi-Tenancy** | Native Row Level Security (RLS)| Security Rules DSL | Custom Auth Tables |
| **Vector Search** | `pgvector` built-in | Extension required | Not supported |
| **Self-Hosting** | 100% Open Source Docker | Closed Cloud Only | Single Go Binary |

---

## Why RTIQA Selected This Solution
PostgreSQL and Supabase provide the ultimate relational foundation for RTIQA, pairing multi-tenant database security (RLS) with vector search capabilities and zero vendor lock-in.

---

## Future Outlook
`pgvector` performance continues to advance, matching dedicated vector database throughput while maintaining single-database transactional consistency.

---

## References & Citations
1. PostgreSQL Global Development Group: *PostgreSQL 16 Documentation* (2026).
2. Supabase Engineering Report: *PostgreSQL Row Level Security Performance* (https://supabase.com/docs).
