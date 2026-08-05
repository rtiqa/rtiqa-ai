# ADR-0007: Instructure Canvas LMS Integration via LTI 1.3 & xAPI

## Metadata
- **Decision ID**: ADR-0007
- **Title**: Learning Management System (LMS) Integration via Instructure Canvas LMS, LTI 1.3, and Experience API (xAPI)
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: LMS Subsystem & Learning Standards

---

## Context
RTIQA must interoperate with established higher-education and K-12 institutional infrastructure. School districts require seamless course enrollment, gradebook synchronization, assignment submission, and standardized learning activity tracking.

## Problem Statement
Building a proprietary closed LMS locks RTIQA out of existing school district software ecosystems. Custom API connectors for every school platform create unsustainable integration complexity.

## Alternatives Considered

1. **Instructure Canvas LMS + LTI 1.3 + xAPI**: Open-source enterprise LMS integration paired with international learning standards.
2. **Moodle Integration**: Integration with Moodle PHP ecosystem.
3. **Open edX Platform**: Integration with Open edX Python microservices.

## Engineering Comparison

| Dimension | Canvas LMS + LTI 1.3 | Moodle Integration | Open edX Platform |
| :--- | :--- | :--- | :--- |
| **Standard Support** | LTI 1.3 / Advantage & xAPI | LTI 1.1 / Custom Plugins | LTI 1.3 / Open edX APIs |
| **API Quality** | Modern REST / GraphQL APIs | Legacy PHP REST wrappers | Complex Python endpoints |
| **Higher-Ed Adoption** | Industry market leader | Widespread global legacy | Dominant in MOOCs |
| **Extensibility** | Clean OAuth2 Tool Launch | PHP Module extensions | Django XBlock plugins |

## Advantages
- LTI 1.3 (Learning Tools Interoperability) enables RTIQA to be embedded directly as a secure tool inside any major school LMS without single-sign-on friction.
- Canvas LMS provides comprehensive GraphQL and REST endpoints for courses, assignments, rubrics, and grades.
- Experience API (xAPI) standard tracks granular student learning events (e.g., video completion rates, quiz speeds, AI tutor engagement) into an open Learning Record Store (LRS).

## Disadvantages
- Deploying and self-hosting a full Canvas LMS cluster requires significant infrastructure resources.

## Risks
- OAuth2 token exchange failures during cross-origin LTI iframe or mobile deep-link launches.
- Mitigated by fallback standard OIDC authentication redirects.

## Long-term Maintenance
Canvas LMS is open-sourced by Instructure; LTI and xAPI standards are governed by 1EdTech Consortium and Advanced Distributed Learning (ADL).

## Performance Impact
- Asynchronous xAPI background event batching prevents network overhead during active learning sessions.

## Security Impact
- LTI 1.3 uses OAuth2 with JSON Web Tokens (JWT) and RSA-256 public key encryption, preventing request tampering.

## Scalability Impact
- Standardized interfaces allow RTIQA to scale across thousands of distinct educational institutions effortlessly.

## Cost Impact
- Canvas LMS core is available open-source (AGPLv3); standard LTI 1.3 specs incur zero royalty fees.

## Why RTIQA Selected This Solution
Adopting Canvas LMS APIs alongside LTI 1.3 and xAPI guarantees that RTIQA complies with international edtech standards, enabling frictionless adoption by enterprise school districts worldwide.

## Future Re-evaluation Criteria
Re-evaluate if 1EdTech issues an updated LTI 2.0 specification that mandates new authentication handshakes.
