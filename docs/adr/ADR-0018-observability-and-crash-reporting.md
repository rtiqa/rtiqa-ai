# ADR-0018: Open-Source Sentry Crash Reporting & OpenTelemetry Metrics

## Metadata
- **Decision ID**: ADR-0018
- **Title**: Observability Standard: Open-Source Sentry Crash Reporting & OpenTelemetry (OTel) Metrics
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: Observability, Monitoring & Crash Reporting

---

## Context
When RTIQA is deployed across millions of diverse Android devices and self-hosted microservices, real-time visibility into application crashes, unhandled exceptions, network latency, and system performance bottlenecks is vital.

## Problem Statement
Relying on user bug reports to catch crashes delays fixes by days or weeks. Commercial crash monitoring platforms (Firebase Crashlytics, Datadog) store operational telemetry in closed third-party clouds, creating privacy and compliance issues.

## Alternatives Considered

1. **Self-Hosted Sentry + OpenTelemetry (OTel)**: Production-ready open-source error monitoring paired with CNCF standardized telemetry metrics.
2. **Firebase Crashlytics**: Google's free mobile crash reporting service.
3. **Datadog Mobile APM**: Commercial enterprise APM platform.

## Engineering Comparison

| Dimension | Sentry + OpenTelemetry | Firebase Crashlytics | Datadog Mobile APM |
| :--- | :--- | :--- | :--- |
| **Data Ownership** | 100% Self-Hostable on-premise | Hosted on Google Firebase | Hosted on Datadog Cloud |
| **Symbolication** | Full Android ProGuard / R8 mapping | ProGuard / R8 mapping | ProGuard / R8 mapping |
| **Tracing Standard** | OpenTelemetry (CNCF) | Proprietary Firebase Tracing | Proprietary Datadog Agent |
| **Breadcrumbs** | Automatic HTTP, DB, View logs | Basic Firebase events | Full session replays |

## Advantages
- Sentry provides detailed crash stack traces, automatic ProGuard/R8 de-obfuscation, user action breadcrumbs, and device state metadata.
- OpenTelemetry (OTel) provides unified, vendor-neutral distributed tracing across mobile clients, Ktor microservices, and PostgreSQL databases.
- 100% self-hostable, guaranteeing that error reports and diagnostic logs containing user metadata remain strictly under institutional control.

## Disadvantages
- Self-hosting the Sentry event ingestion pipeline requires dedicated storage (Kafka / PostgreSQL / ClickHouse).

## Risks
- Sensitive personal data (passwords, tokens) captured in crash breadcrumbs.
- Mitigated by client-side Sentry event processors that sanitize authorization headers and text fields before dispatch.

## Long-term Maintenance
Sentry is backed by an enterprise open-source company; OpenTelemetry is a top-tier Cloud Native Computing Foundation (CNCF) project.

## Performance Impact
- Low-overhead asynchronous error capture background worker queue; zero main thread stutter.

## Security Impact
- Ensures diagnostic telemetry data remains compliant with student data privacy laws (FERPA / GDPR).

## Scalability Impact
- Ingests millions of error events daily across distributed microservice and client app deployments.

## Cost Impact
- Self-hosted deployment eliminates expensive per-event APM SaaS charges.

## Why RTIQA Selected This Solution
Sentry and OpenTelemetry deliver comprehensive, privacy-compliant, vendor-neutral crash monitoring and distributed performance tracing.

## Future Re-evaluation Criteria
Re-evaluate if Grafana Faro mobile telemetry reaches full crash symbolication parity with Sentry.
