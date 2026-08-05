# ADR-0002: Kotlin Ktor Server & Supabase BaaS Infrastructure

## Metadata
- **Decision ID**: ADR-0002
- **Title**: Adoption of Ktor Server Microservices & Supabase Relational Gateway Infrastructure
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: Backend Microservices & BaaS

---

## Context
RTIQA requires a scalable backend microservices layer capable of serving dynamic course data, streaming AI tutoring responses, managing multi-tenant school operations, and maintaining real-time WebSockets synchronization.

## Problem Statement
Monolithic backend frameworks often incur heavy memory footprints, slow cold starts in serverless environments, and high operational maintenance costs. Furthermore, context-switching between different programming languages (e.g., Node.js or Python on the server vs. Kotlin on Android) increases domain model duplication.

## Alternatives Considered

1. **Kotlin Ktor Server + Supabase (PostgreSQL)**: Coroutine-native Kotlin microservices with self-hosted PostgreSQL BaaS.
2. **Spring Boot (Kotlin)**: Java/Kotlin enterprise framework.
3. **NestJS (TypeScript)**: Node.js backend microservice ecosystem.

## Engineering Comparison

| Dimension | Ktor + Supabase | Spring Boot | NestJS |
| :--- | :--- | :--- | :--- |
| **Language** | 100% Kotlin | Java / Kotlin | TypeScript |
| **Cold Start Memory** | ~40 MB RAM | ~350 MB RAM | ~120 MB RAM |
| **Model Code Sharing**| Direct sharing (`core-domain`) | Shared model libraries | Manual schema duplication |
| **Data Engine** | PostgreSQL (Row Level Security) | Flexible (Hibernate/JPA) | TypeORM / Prisma |

## Advantages
- Shared business domain models between Android client (`core-domain`) and Ktor backend.
- Low footprint microservices capable of high-concurrency coroutine processing.
- Supabase delivers battle-tested PostgreSQL, instant REST/GraphQL endpoints, built-in Row Level Security (RLS), and vector search capabilities (`pgvector`).

## Disadvantages
- Smaller community plugin library compared to the massive Spring ecosystem.

## Risks
- Supabase self-hosting orchestration requires docker container management.
- Mitigated by standard Docker Compose and Kubernetes Helm chart configurations.

## Long-term Maintenance
Ktor is developed and maintained directly by JetBrains; Supabase is supported by an active global open-source community.

## Performance Impact
- Sub-10ms response times for core API endpoints.
- Lightweight memory utilization enables high-density container deployment.

## Security Impact
- Fine-grained database access enforcement via PostgreSQL Row Level Security (RLS).
- Zero-trust authentication token verification using standard OIDC JWTs.

## Scalability Impact
- Horizontally scalable containerized Ktor nodes behind a cloud load balancer.

## Cost Impact
- Open-source Apache 2.0 / MIT licensing minimizes infrastructure software overhead.

## Why RTIQA Selected This Solution
Ktor allows RTIQA developers to maintain single-language (Kotlin) full-stack development, reducing technical debt while leveraging the enterprise relational power of PostgreSQL via Supabase.

## Future Re-evaluation Criteria
Re-evaluate if Ktor serverless deployment benchmarks are surpassed by Native GraalVM compiled Spring Boot binaries without memory penalties.
