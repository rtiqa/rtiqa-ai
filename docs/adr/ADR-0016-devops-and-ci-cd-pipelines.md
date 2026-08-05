# ADR-0016: GitHub Actions CI/CD & Fastlane Release Automation

## Metadata
- **Decision ID**: ADR-0016
- **Title**: DevOps & Automation Standardization: GitHub Actions CI/CD Workflows & Fastlane Release Automation
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: DevOps & Release Automation Pipelines

---

## Context
RTIQA requires a resilient, automated Continuous Integration and Continuous Deployment (CI/CD) pipeline to build, test, lint, scan, and deploy mobile Android packages (APKs/AABs) and backend container images continuously.

## Problem Statement
Manual release builds lead to human error, missed regression bugs, inconsistent code formatting, insecure credential handling, and delayed release cycles.

## Alternatives Considered

1. **GitHub Actions + Fastlane**: Declarative YAML workflows tightly integrated with GitHub, paired with Fastlane mobile automation.
2. **GitLab CI/CD**: Self-hosted GitLab pipeline engine.
3. **Jenkins**: Legacy Java CI server.

## Engineering Comparison

| Dimension | GitHub Actions + Fastlane | GitLab CI/CD | Jenkins |
| :--- | :--- | :--- | :--- |
| **GitHub Integration** | 100% Native PR Check runs | Secondary mirror setup | Webhook integrations |
| **Mobile Build Tooling**| Fastlane Ruby integration | Fastlane CLI | Custom bash scripts |
| **Configuration** | Declarative `.github/workflows` | `.gitlab-ci.yml` | Groovy Jenkinsfile / UI |
| **Runner Infrastructure**| GitHub Hosted / Self-Hosted | GitLab Runners | Dedicated Jenkins Nodes |

## Advantages
- Native GitHub integration automatically triggering build verification, lint checks, and unit tests on every Pull Request.
- Fastlane automates code signing key management, screenshot generation, release note localization, and deployment to Google Play Internal Testing tracks.
- Parallel job runners execute Android Gradle builds, static analysis, unit tests, and security scans simultaneously.
- Configuration as Code maintained directly in the repository under `.github/workflows/`.

## Disadvantages
- Build minutes usage on GitHub cloud runners requires build caching optimization (`gradle-build-action`).

## Risks
- Secrets leak during automated CI deployment steps.
- Mitigated by storing production signing keys in GitHub Repository Encrypted Secrets; zero plaintext keys in code.

## Long-term Maintenance
GitHub Actions is backed by GitHub / Microsoft; Fastlane is maintained by an active global mobile developer community.

## Performance Impact
- Gradle Build Cache reuse cuts average PR build verification times from 12 minutes to under 3 minutes.

## Security Impact
- Enforces strict protection rules on `main` and `develop` branches; code cannot be merged without green CI status.

## Scalability Impact
- Effortlessly handles scaling developer teams submitting dozens of concurrent Pull Requests daily.

## Cost Impact
- Open-source repository tier includes generous free GitHub Actions minutes; Fastlane is 100% open source (MIT).

## Why RTIQA Selected This Solution
GitHub Actions and Fastlane deliver seamless automation for mobile app compilation, security testing, and Google Play release distribution.

## Future Re-evaluation Criteria
Re-evaluate if cloud runner build costs exceed dedicated self-hosted M2/M3 Apple Silicon / Linux build server hardware.
