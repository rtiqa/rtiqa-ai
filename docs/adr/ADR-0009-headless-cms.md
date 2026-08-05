# ADR-0009: Directus Headless CMS for Dynamic Course Authoring

## Metadata
- **Decision ID**: ADR-0009
- **Title**: Headless Content Management System (CMS) Selection: Directus for Educational Content Authoring
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: Content Management & Curriculum Authoring

---

## Context
Curriculum creators, teachers, and instructional designers require a non-technical, visual dashboard to author courses, design lessons, upload multimedia assets, create quiz question banks, and localize Arabic/English content for RTIQA.

## Problem Statement
Hardcoding educational content into source repositories or raw database migrations restricts curriculum updates to software release cycles. Traditional monolithic CMS platforms impose rigid schemas that do not conform to RTIQA's Clean Architecture models.

## Alternatives Considered

1. **Directus**: Open-source headless data platform that mirrors SQL database schemas dynamically.
2. **Strapi**: Node.js headless CMS.
3. **Decap CMS**: Git-based static file CMS.

## Engineering Comparison

| Dimension | Directus | Strapi | Decap CMS |
| :--- | :--- | :--- | :--- |
| **Database Binding** | Directly mirrors raw SQL (Postgres) | Custom database ORM layer | Git Markdown repository |
| **REST / GraphQL** | Instant auto-generated endpoints | Auto-generated REST/GraphQL | None (Static file read) |
| **Media Management**| Built-in asset transformation & CDN | Plugin-based media library | Git LFS storage |
| **RTL / Localization**| Built-in multi-language UI & content | Plugin localization | File-based translations |

## Advantages
- Directus layer wraps RTIQA's existing PostgreSQL database without introducing proprietary vendor lock-in or schema transformations.
- Instant, secure REST and GraphQL endpoints reflecting real-time curriculum changes.
- Granular Role-Based Access Control (RBAC) enabling external subject-matter experts to edit specific lessons without accessing operational system data.
- Built-in multi-language translation support for side-by-side Arabic and English content authoring.

## Disadvantages
- Requires Node.js container deployment for the admin dashboard service.

## Risks
- Direct database modification by non-technical content authors could bypass application-level validation.
- Mitigated by database-level constraints and mandatory staging environment review workflows.

## Long-term Maintenance
Directus is actively maintained with strong enterprise sponsorship and millions of global deployments.

## Performance Impact
- Fast SQL query translation with Redis caching delivers sub-15ms response times for course catalog requests.

## Security Impact
- Enforces strict JWT token authentication and IP-whitelisted authoring access.

## Scalability Impact
- Decoupled headless architecture allows content authoring traffic to scale independently from student client traffic.

## Cost Impact
- Open-source core licensing reduces proprietary CMS software licensing costs to zero.

## Why RTIQA Selected This Solution
Directus connects directly to RTIQA's PostgreSQL relational database, providing content creators with a premium visual authoring experience without altering backend data models.

## Future Re-evaluation Criteria
Re-evaluate if Directus introduces restrictive commercial licensing models on core relational data feature sets.
