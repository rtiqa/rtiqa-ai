# RES-0017: Rejected Alternatives: Spring Boot & Google Firebase Research Report

## Executive Summary
Traditional Java enterprise monoliths (Spring Boot) and closed cloud BaaS ecosystems (Google Firebase) were evaluated during the initial architectural design phase of RTIQA. This report documents the technical grounds for rejecting both technologies in favor of Ktor Server microservices and self-hosted Supabase PostgreSQL.

---

## Technical Metadata & Overview
- **Technologies Evaluated**: Spring Boot (VMware / Java) & Google Firebase Suite (Google / Closed Cloud SaaS)
- **Evaluation Subsystems**: Backend Microservices, Auth & Database Infrastructure
- **Official Websites**: [spring.io/projects/spring-boot](https://spring.io/projects/spring-boot) / [firebase.google.com](https://firebase.google.com)

---

## Detailed Technical Evaluation

### 1. Spring Boot (Enterprise Java/Kotlin Monolith)
Spring Boot is the dominant enterprise Java application framework, featuring heavy dependency injection, auto-configuration modules, and object-relational mapping (Hibernate / JPA).
- **Heavy Cold-Start Memory Footprint**: A minimal Spring Boot microservice requires ~350 MB to 500 MB of RAM on startup. In serverless or container auto-scaling environments, cold starts take 3 to 8 seconds.
- **Reflection & Annotation Overhead**: Heavy runtime reflection (`@Autowired`, `@Entity`) complicates compile-time safety and slows application startup.
- **Excessive Framework Abstraction**: Spring Data ORM and Hibernate introduce complex query generation overhead (`N+1` query bugs) that degrade high-throughput database operations.

### 2. Google Firebase Suite (Closed SaaS Cloud)
Google Firebase offers a cloud-hosted Backend-as-a-Service (BaaS) comprising Firestore NoSQL database, Firebase Auth, Cloud Functions, and Firebase Storage.
- **Proprietary Vendor Lock-In**: Firebase Firestore uses a proprietary closed NoSQL document model. Migrating off Firebase requires rewriting all application database code.
- **No Self-Hosting Capability**: Firebase cannot be self-hosted on sovereign cloud infrastructure, violating strict educational data privacy mandates (GDPR, FERPA) required by institutional customers.
- **NoSQL Query Limitations**: Firestore lacks relational SQL joins, foreign key enforcement, complex aggregations, and vector similarity search (`pgvector`).
- **Unpredictable SaaS Costs**: High-frequency database read/write operations incur scaling billing spikes.

---

## Direct Technical Comparison

| Dimension | Selected: Ktor + Supabase | Rejected: Spring Boot | Rejected: Google Firebase |
| :--- | :--- | :--- | :--- |
| **Data Engine** | PostgreSQL Relational (ACID) | Relational (Hibernate ORM) | Closed Firestore NoSQL |
| **Cold-Start RAM** | ~40 MB RAM | ~350 MB - 500 MB RAM | N/A (Cloud Managed) |
| **Self-Hostable** | 100% Self-Hostable Docker | 100% Self-Hostable | 0% (Closed Google SaaS) |
| **Vector Search** | `pgvector` built-in | Requires custom vector DB | Requires Cloud Extensions |
| **Code Sharing** | Direct Kotlin (`core-domain`) | Shared JAR libraries | Manual Client Mapping |

---

## Key Reasons for RTIQA Rejection

1. **Sovereignty & Data Privacy**: RTIQA institutional clients require on-premise or sovereign cloud hosting. Firebase's closed cloud architecture makes compliance impossible.
2. **Resource Efficiency**: Ktor server microservices require 8x less RAM than Spring Boot nodes, allowing RTIQA to run high-density container deployments at lower infrastructure costs.
3. **Relational Data Integrity**: Educational curricula, course prerequisites, enrollments, and grades are inherently relational. PostgreSQL provides rigid ACID compliance, foreign keys, and Row Level Security (RLS) that NoSQL databases like Firestore cannot match.

---

## References & Citations
1. RTIQA Infrastructure Audit: *Microservice Container Memory & Cold-Start Benchmarks* (2026).
2. European Educational Data Sovereignty Standard: *Compliance Requirements for Student Record Hosting*.
