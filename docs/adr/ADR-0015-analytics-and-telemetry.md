# ADR-0015: PostHog Product Analytics & Privacy-Compliant Telemetry

## Metadata
- **Decision ID**: ADR-0015
- **Title**: Self-Hosted Product Analytics & Privacy-Compliant User Telemetry via PostHog Open Source
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: Product Analytics & User Insights

---

## Context
RTIQA product teams, educators, and system administrators require actionable insights into learning behaviors, feature adoption, drop-off rates, course completion funnels, and UX friction points to continuously refine the platform.

## Problem Statement
Third-party commercial analytics platforms (Google Analytics, Mixpanel, Amplitude) collect student behavioral data into proprietary third-party clouds, violating student data privacy regulations (GDPR, COPPA, FERPA).

## Alternatives Considered

1. **PostHog Open Source**: Self-hosted product analytics platform featuring event tracking, funnels, user session replays, feature flags, and A/B testing.
2. **Matomo Analytics**: Open-source web privacy analytics software.
3. **Plausible Analytics**: Minimalist privacy web analytics.

## Engineering Comparison

| Dimension | PostHog Open Source | Matomo Analytics | Plausible Analytics |
| :--- | :--- | :--- | :--- |
| **Feature Set** | Analytics, Funnels, Session Replay, Feature Flags, A/B | Pageview & Visitor analytics | Basic aggregate page metrics |
| **Mobile SDK** | Official Kotlin / Android SDK | Android SDK | Web JavaScript focus |
| **Data Privacy** | 100% Self-Hosted on RTIQA infrastructure | 100% Self-Hosted | 100% Self-Hosted |
| **Anonymization** | Automatic IP & PII stripping | IP masking | Aggregate-only data |

## Advantages
- 100% self-hostable on RTIQA cloud infrastructure, ensuring student analytics data never leaves institutional control.
- Native Android Kotlin SDK supporting offline event buffering (events recorded offline are synced when connected).
- Integrated Feature Flag engine enabling controlled canary rollouts of new learning tools to select school cohorts.
- User Session Replay (with strict PII masking) allows engineers to inspect real mobile app crashes and visual bugs.

## Disadvantages
- Self-hosting ClickHouse analytics database cluster requires dedicated storage monitoring.

## Risks
- Storage volume accumulation from high-frequency event tracking.
- Mitigated by data retention lifecycle policies automatically purging raw telemetry events after 90 days.

## Long-term Maintenance
PostHog is an extremely well-funded open-core company with a massive global open-source community.

## Performance Impact
- Asynchronous non-blocking background event batching; zero impact on main thread UI responsiveness.

## Security Impact
- Complies strictly with international student privacy laws (FERPA, GDPR) by eliminating third-party data tracking.

## Scalability Impact
- ClickHouse columnar database backend handles billions of user analytics events easily.

## Cost Impact
- Eliminates recurring enterprise SaaS analytics bills ($20k+/yr for Mixpanel/Amplitude).

## Why RTIQA Selected This Solution
PostHog gives RTIQA enterprise product analytics, feature flags, and session replays while maintaining strict compliance with student data privacy standards.

## Future Re-evaluation Criteria
Re-evaluate if self-hosted ClickHouse storage maintenance costs exceed cloud analytical server allocations.
