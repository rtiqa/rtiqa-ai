# 📦 RTIQA Release Process & Deployment Guide

This guide describes the release methodology, semantic versioning rules, build artifacts, and automated release pipeline for **RTIQA**.

---

## 1. Release Strategy & Versioning Rules

RTIQA adheres to [Semantic Versioning 2.0.0](https://semver.org/):
- **MAJOR (`X.0.0`)**: Incompatible API changes, major architectural migration.
- **MINOR (`1.X.0`)**: Backwards-compatible functionality addition.
- **PATCH (`1.0.X`)**: Backwards-compatible bug fixes and stability patches.

---

## 2. Release Branching & Tagging Workflow

1. **Tag Release**: Create a Git tag matching `v*.*.*` on the `main` branch.
   ```bash
   git tag -a v1.0.0 -m "Release v1.0.0 - Enterprise Infrastructure & Multi-Module AI Platform"
   git push origin v1.0.0
   ```
2. **Automated GitHub Action**: Pushing the tag triggers `.github/workflows/release.yml`.
3. **Artifact Build**:
   - Android Debug/Release APK (`app/build/outputs/apk/debug/app-debug.apk`)
   - Android App Bundle (AAB) (`app/build/outputs/bundle/release/app-release.aab`)
   - Ktor Backend Microservice JAR (`deploy/ktor-starter/build/libs/ktor-starter-1.0.0.jar`)
4. **Publish Release**: GitHub Action creates a new release entry attached with the build binaries and release notes.

---

## 3. Pre-Release Quality Assurance Checklist

Prior to tagging a release, the maintainer must verify:
- [ ] `scripts/bootstrap.sh` executes without errors.
- [ ] All JVM unit tests pass (`gradle testDebugUnitTest`).
- [ ] `metadata.json` version and strings match release version.
- [ ] `CHANGELOG.md` is updated with latest release changes.
- [ ] Docker Compose stack services build and start cleanly (`docker compose up -d`).
- [ ] Helm Chart lint passes (`helm lint helm/rtiqa-stack`).
