# ADR-0005: Red Hat Keycloak Enterprise IAM with OAuth2 / OIDC

## Metadata
- **Decision ID**: ADR-0005
- **Title**: Identity & Access Management (IAM) Standardization on Red Hat Keycloak Enterprise
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: Authentication & Identity Management

---

## Context
RTIQA serves diverse educational user cohorts: students, teachers, school administrators, parent guardians, and system operators across multi-tenant school districts. The platform requires robust Single Sign-On (SSO), Role-Based Access Control (RBAC), and enterprise identity federation.

## Problem Statement
Building custom authentication logic (password hashing, session tokens, password resets, OAuth flows) leads to security vulnerabilities, non-compliance with identity standards (OIDC, SAML 2.0), and massive ongoing maintenance overhead.

## Alternatives Considered

1. **Red Hat Keycloak**: Production-grade open-source IAM supporting OAuth 2.0, OpenID Connect (OIDC), SAML 2.0, and multi-tenant realms.
2. **Authentik**: Modern Python/Go IAM identity provider.
3. **Firebase Authentication**: Closed-source Google cloud auth service.

## Engineering Comparison

| Dimension | Keycloak | Authentik | Firebase Auth |
| :--- | :--- | :--- | :--- |
| **Open Source License** | Apache 2.0 (Red Hat) | GPLv3 (Copyleft) | Proprietary Closed SaaS |
| **Multi-Tenancy** | Native Realm isolation | Namespace policies | Multiple projects |
| **Enterprise Federation**| Active Directory / LDAP / SAML / OIDC | LDAP / OIDC | Custom SAML (Paid tier) |
| **Self-Hosting** | 100% Self-Hostable | 100% Self-Hostable | No self-hosting option |

## Advantages
- Turnkey support for OpenID Connect (OIDC) and OAuth 2.0 authorization code flow with PKCE.
- Native multi-tenant Realm isolation allowing each school district to manage its own user directory and SSO policies.
- Support for Multi-Factor Authentication (MFA), TOTP, SMS OTP, and biometric login integration.
- Direct federation with enterprise Active Directory / LDAP servers used by educational institutions.

## Disadvantages
- Higher Java JVM memory footprint (~1 GB RAM per identity cluster instance).

## Risks
- Misconfigured realm security settings or token expiration periods.
- Mitigated by automated infrastructure-as-code deployment templates (Terraform / Keycloak Operator).

## Long-term Maintenance
Maintained by Red Hat / IBM and backed by a massive global enterprise contributor community.

## Performance Impact
- Fast JWT validation using public key signature verification on API gateways (zero database roundtrips for token checks).

## Security Impact
- Eliminates cleartext password storage risks; enforces industry-standard Argon2 / bcrypt password hashing.

## Scalability Impact
- Enterprise horizontal cluster scaling across multi-region Kubernetes deployments.

## Cost Impact
- Zero commercial software license fees (Apache 2.0).

## Why RTIQA Selected This Solution
Keycloak is the undeniable global leader for open-source enterprise IAM, giving RTIQA multi-tenant realm isolation, complete self-hosting autonomy, and zero vendor lock-in.

## Future Re-evaluation Criteria
Re-evaluate if Ory Kratos/Hydra microservices demonstrate a 50%+ reduction in operational memory footprint while maintaining identical enterprise SAML/LDAP feature sets.
