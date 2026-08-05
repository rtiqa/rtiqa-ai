# RES-0019: Rejected Alternatives: Open edX & Moodle Research Report

## Executive Summary
Legacy open-source educational learning management platforms—specifically Open edX (Python/Django microservices) and Moodle (PHP web engine)—were evaluated as core LMS foundations for RTIQA. This report documents why both legacy architectures were rejected in favor of Instructure Canvas LMS APIs paired with LTI 1.3 standards.

---

## Technical Metadata & Overview
- **Technologies Evaluated**: Open edX (edX / Python / Django) & Moodle (Moodle HQ / PHP)
- **Evaluation Subsystems**: LMS Core & Course Execution Engine
- **Official Websites**: [openedx.org](https://openedx.org) / [moodle.org](https://moodle.org)

---

## Detailed Technical Evaluation

### 1. Open edX Platform (Python / Django Microservices)
Open edX is an open-source platform originally created by Harvard and MIT to host Massive Open Online Courses (MOOCs).
- **Extreme Operational Complexity**: Deploying Open edX requires orchestrating dozens of complex Python/Django services, edxapp, Forum services, MySQL, MongoDB, and XBlock plugins via intricate Tutor orchestration tools.
- **Monolithic UI Constraints**: Course execution UI is tightly coupled to legacy Server-Side Rendered (SSR) Django HTML templates, preventing clean integration with modern native mobile apps.
- **Heavy Infrastructure Overhead**: A baseline Open edX deployment requires 16 GB+ RAM minimum, making small-to-medium institutional deployments cost-prohibitive.

### 2. Moodle LMS Engine (PHP Monolith)
Moodle is the world's oldest open-source PHP-based learning management system.
- **Legacy PHP Architecture**: Moodle's core architecture relies on procedural PHP patterns, global variables, and legacy server-rendered web pages dating back to 2002.
- **Outdated REST API Wrapper**: Moodle's REST web services API is a retrofitted layer that returns inconsistent JSON payloads, lacking modern GraphQL endpoints.
- **Poor LTI 1.3 Implementation**: Moodle's LTI tool provider implementation requires custom PHP plugin patches that introduce security vulnerabilities across upgrades.

---

## Direct Technical Comparison

| Dimension | Selected: Canvas LMS + LTI 1.3 | Rejected: Open edX | Rejected: Moodle LMS |
| :--- | :--- | :--- | :--- |
| **Language & Stack** | Ruby / Rails + React / GraphQL| Python / Django + React | PHP 8 (Procedural Core) |
| **API Quality** | Modern REST & GraphQL APIs | Complex REST endpoints | Legacy PHP Web Services |
| **LTI 1.3 Support** | Official Native LTI 1.3 | Complex XBlock LTI | Legacy PHP Plugins |
| **RAM Footprint** | Optimized Rails Cluster | Heavy (16 GB+ RAM min) | Low RAM / High Latency |
| **Mobile Integration**| Native SDKs & Deep Linking | Custom Mobile App Fork | Webview Wrapper App |

---

## Key Reasons for RTIQA Rejection

1. **Modern API Architecture**: Canvas LMS provides modern GraphQL endpoints that align with Clean Architecture, whereas Moodle relies on legacy PHP webservice endpoints.
2. **Operational Maintainability**: Open edX's deployment footprint is excessively heavy for K-12 school districts and regional institutions. Canvas LMS provides a much cleaner containerized footprint.
3. **Standardized Tool Interoperability**: Adopting LTI 1.3 allows RTIQA to act as a secure learning tool inside *any* existing institutional LMS, rather than forcing schools to migrate their entire LMS infrastructure to Open edX or Moodle.

---

## References & Citations
1. RTIQA EdTech Integration Benchmark: *Canvas LMS vs Open edX Deployment & API Analysis* (2026).
2. 1EdTech Consortium: *LTI 1.3 Interoperability Audit across Global LMS Frameworks*.
