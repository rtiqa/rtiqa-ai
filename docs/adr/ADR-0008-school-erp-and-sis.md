# ADR-0008: ERPNext Education Module for Multi-Tenant School Administration

## Metadata
- **Decision ID**: ADR-0008
- **Title**: School Enterprise Resource Planning (ERP) & Student Information System (SIS) Standardization on ERPNext Education
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: Enterprise Resource Planning & School Administration

---

## Context
Enterprise school systems operating RTIQA require comprehensive administrative tools for student enrollment, academic calendars, fee management, teacher schedules, attendance tracking, and institutional reporting.

## Problem Statement
Developing institutional administrative features (billing, payroll, enrollment, attendance compliance) from scratch diverts engineering effort away from core AI learning features and introduces accounting and compliance risks.

## Alternatives Considered

1. **ERPNext (Education Module)**: Open-source enterprise ERP and SIS built on the Python/JS Frappe framework.
2. **OpenSIS**: Dedicated K-12 open-source school management system.
3. **Apache Fineract**: Financial backend framework.

## Engineering Comparison

| Dimension | ERPNext Education | OpenSIS | Apache Fineract |
| :--- | :--- | :--- | :--- |
| **Scope** | Full ERP (SIS, HR, Accounting, Fee Processing) | Dedicated SIS only | Financial ledger only |
| **API Layer** | Comprehensive REST API | Basic REST endpoints | Complex microfinance APIs |
| **Multi-Tenancy** | Native multi-tenant site isolation | Single-tenant database | Complex multi-tenant schemas |
| **UI Customization**| Metadata-driven Form Builder | Hardcoded PHP templates | Headless backend only |

## Advantages
- Out-of-the-box support for complete school operational workflows: Student Admission, Course Scheduling, Attendance Tracking, Assessment Plan, and Fee Collection.
- Built-in multi-tenant architecture allowing isolated school sites to share a single ERPNext cluster safely.
- Robust REST API enabling RTIQA Ktor microservices and mobile apps to query student profiles and record attendance automatically.
- Multi-currency financial module supporting regional tuition billing and scholarship management.

## Disadvantages
- Requires managing a Python/Frappe backend stack alongside Kotlin microservices.

## Risks
- Data synchronization mismatches between mobile offline attendance logs and ERPNext central database.
- Mitigated by idempotent REST API integration handlers with strict transaction logging.

## Long-term Maintenance
Maintained by Frappe Technologies and backed by a global enterprise open-source community with millions of active deployments.

## Performance Impact
- High-performance MariaDB/Redis caching tier handles millions of operational database queries efficiently.

## Security Impact
- Enterprise role-based permission control (RBAC) down to individual document fields; strict audit logs for administrative actions.

## Scalability Impact
- Horizontally scalable containerized web nodes supporting multi-tenant school district scaling.

## Cost Impact
- Open-source software under GPLv3 license; eliminates millions in commercial proprietary ERP subscription fees.

## Why RTIQA Selected This Solution
ERPNext Education provides a complete, battle-tested, enterprise-grade school management engine, allowing RTIQA to deliver full institutional operations immediately.

## Future Re-evaluation Criteria
Re-evaluate if a native Kotlin-based ERP framework emerges with equivalent accounting and SIS capabilities.
