# RES-0020: Rejected Alternatives: Strapi & Hasura Research Report

## Executive Summary
Headless CMS engines (Strapi) and automatic GraphQL gateways (Hasura) were evaluated for managing curriculum content and exposing APIs. This report details the technical reasons for rejecting both in favor of Directus Headless CMS and Ktor/Supabase APIs.

---

## Technical Metadata & Overview
- **Technologies Evaluated**: Strapi CMS (Node.js / Custom ORM) & Hasura GraphQL Engine (Haskell / Postgres Engine)
- **Evaluation Subsystems**: Curriculum Content Authoring & API Gateway Layer
- **Official Websites**: [strapi.io](https://strapi.io) / [hasura.io](https://hasura.io)

---

## Detailed Technical Evaluation

### 1. Strapi Headless CMS (Node.js / Custom ORM)
Strapi is a popular open-source Node.js headless CMS providing an admin UI and auto-generated REST/GraphQL endpoints.
- **Proprietary Database ORM Isolation**: Strapi creates its own database tables and column naming conventions using a custom ORM layer. It cannot directly mirror an existing PostgreSQL relational database without forcing structural schema migrations.
- **Inflexible Admin Customization**: Customizing Strapi's visual admin interface requires writing custom React plugins and re-building the entire frontend admin bundle.
- **Weak Multi-Tenant RBAC**: Fine-grained role-based access control (RBAC) down to specific fields requires purchasing high-tier commercial enterprise licenses.

### 2. Hasura GraphQL Engine (Haskell / PostgreSQL Gateway)
Hasura is a high-performance engine that compiles GraphQL queries into optimized SQL statements directly against PostgreSQL databases.
- **GraphQL-Only Constraint**: Hasura forces clients to interact primarily via GraphQL, adding query parsing complexity to mobile applications that benefit from simple REST or binary endpoints.
- **Complex Enterprise License Changes**: Recent license changes restricted key features (such as cached queries and enterprise connectors) to paid commercial tiers.
- **No Native Content Management Dashboard**: Hasura is strictly an API gateway; it lacks a visual authoring dashboard for non-technical teachers and curriculum designers.

---

## Direct Technical Comparison

| Dimension | Selected: Directus CMS | Rejected: Strapi CMS | Rejected: Hasura Engine |
| :--- | :--- | :--- | :--- |
| **Database Binding** | Mirrors raw PostgreSQL | Custom Database ORM | Mirrors raw PostgreSQL |
| **Visual Content Dashboard**| Built-in Visual Admin UI | Built-in Visual Admin UI | None (API Gateway only) |
| **API Support** | REST & GraphQL Instant | REST & GraphQL Instant | GraphQL Focused |
| **Multi-Tenant RBAC** | Free Granular RBAC | Requires Paid License | Free Role Permissions |
| **License Model** | BSL / Open Source Core | Open Core / Paid Features| Commercial Enterprise Tier |

---

## Key Reasons for RTIQA Rejection

1. **Direct Database Mirroring**: Directus inspects RTIQA's existing PostgreSQL database tables directly without altering schema structures, whereas Strapi forces proprietary table naming conventions.
2. **Turnkey Authoring Dashboard**: Directus provides non-technical educators with an immediate visual content editing UI, whereas Hasura offers only a developer API gateway without content authoring tools.
3. **Open Access Control**: Directus includes fine-grained field-level RBAC in its open-source core, whereas Strapi restricts advanced security features behind commercial enterprise pricing.

---

## References & Citations
1. RTIQA Architecture Evaluation: *Direct Database Mirroring vs Proprietary CMS Abstraction* (2026).
2. Directus vs Strapi Feature Benchmark: *Role-Based Access Control & Relational Support*.
