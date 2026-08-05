# ADR-0019: MkDocs Material & JetBrains Dokka Documentation Engine

## Metadata
- **Decision ID**: ADR-0019
- **Title**: Documentation Infrastructure: MkDocs Material Portal & JetBrains Dokka Code Generator
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: Technical Documentation & Knowledge Management

---

## Context
RTIQA requires clear, accessible, developer-friendly documentation for system architecture, API contracts, design system guidelines, setup manuals, and KDoc API references to onboard contributors, educators, and enterprise partners efficiently.

## Problem Statement
Outdated README files scattered across subfolders lead to developer confusion, broken onboarding flows, and architectural decay. Proprietary wiki tools lock technical documentation away from version control.

## Alternatives Considered

1. **MkDocs Material + JetBrains Dokka**: Fast Python Markdown documentation generator with Material styling, combined with JetBrains' official Kotlin API documentation generator.
2. **Docusaurus**: Meta's React-based documentation platform.
3. **Confluence**: Atlassian proprietary enterprise wiki.

## Engineering Comparison

| Dimension | MkDocs Material + Dokka | Docusaurus | Confluence |
| :--- | :--- | :--- | :--- |
| **Format** | Markdown / KDoc Comments | Markdown / MDX React | Proprietary Rich Text |
| **Kotlin Integration**| Dokka auto-generates HTML from KDoc | No native Kotlin parser | Manual copy-paste |
| **Search Engine** | Instant built-in offline search | Algolia DocSearch dependency| Slow internal search |
| **Hosting** | Free GitHub Pages / Static hosting | Free GitHub Pages | Paid Atlassian Cloud |

## Advantages
- MkDocs Material renders beautiful, mobile-responsive, dark-mode technical portals directly from standard Markdown files stored in `/docs`.
- Dokka automatically parses Kotlin source code annotations (`KDoc`) across all modular libraries (`core-domain`, `core-data`, `core-ui`), generating standard HTML API documentation.
- Instant, zero-latency client-side search supporting both Arabic and English text queries.
- Version control integration: documentation lives alongside source code in Git, updated via Pull Requests.

## Disadvantages
- Requires Python `mkdocs-material` environment setup for local previewing.

## Risks
- Documentation becoming stale as code evolves.
- Mitigated by automated CI pull request checks verifying documentation builds cleanly without broken links.

## Long-term Maintenance
MkDocs Material maintained by Martin Donath; Dokka maintained directly by JetBrains.

## Performance Impact
- Static HTML generation allows documentation portals to load instantly with zero server processing delay.

## Security Impact
- Static site hosting minimizes attack surfaces; zero backend database vulnerability points.

## Scalability Impact
- Effortlessly scales to house thousands of architecture pages, guides, and complete API reference suites.

## Cost Impact
- 100% free open-source software hosted for zero cost on GitHub Pages.

## Why RTIQA Selected This Solution
MkDocs Material and JetBrains Dokka provide the highest quality technical documentation portal experience, keeping architecture records synchronized with Kotlin source code.

## Future Re-evaluation Criteria
Re-evaluate if a KMP-native documentation engine unifies Markdown and KDoc parsing into a single build plugin.
