# ADR-0010: Typesense In-Memory C++ Search Engine for Course Catalog

## Metadata
- **Decision ID**: ADR-0010
- **Title**: High-Speed Search Engine Standardization on Typesense for Course Catalog and Lesson Retrieval
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: Search & Indexing Engine

---

## Context
RTIQA users need instant, typo-tolerant search across thousands of courses, subjects, lesson transcripts, downloadable resources, and teacher profiles in both Arabic and English.

## Problem Statement
Executing SQL `LIKE` queries or raw database full-text searches across large relational tables causes query locks, high latency (>500ms), poor typo tolerance, and lack of relevance scoring, degrading search experience on mobile devices.

## Alternatives Considered

1. **Typesense**: Modern, in-memory C++ open-source search engine optimized for developer experience and sub-50ms search response times.
2. **Meilisearch**: Rust-based open-source search engine.
3. **Elasticsearch**: Enterprise Java search cluster engine.

## Engineering Comparison

| Dimension | Typesense | Meilisearch | Elasticsearch |
| :--- | :--- | :--- | :--- |
| **Language & Architecture**| In-memory C++ Engine | Rust Engine | Java JVM Engine |
| **Search Latency** | < 20ms | < 30ms | 100ms+ (Resource intensive) |
| **RAM Footprint** | Low (~150 MB RAM base) | Medium during indexing | High (2 GB+ RAM minimum) |
| **Typo Tolerance** | Exceptional out-of-the-box | Exceptional | Complex tuning required |
| **Arabic Text Support**| Native UTF-8 tokenization | Native UTF-8 tokenization | Requires custom analyzers |

## Advantages
- Ultra-fast sub-20ms search response time delivering instant search-as-you-type results.
- Built-in typo tolerance handling spelling mistakes in both Arabic and English seamlessly.
- Simple setup with zero complex JVM configuration compared to Elasticsearch.
- Faceted filtering by subject, grade level, course instructor, and rating.

## Disadvantages
- Entire search index resides in memory (RAM), requiring RAM scaling for multi-gigabyte text indices.

## Risks
- Index rebuild delays during sudden catastrophic server restarts.
- Mitigated by persistent disk snapshotting enabled by default in Typesense.

## Long-term Maintenance
Maintained actively by the Typesense open-source team with expanding enterprise adoption.

## Performance Impact
- Reduces database CPU utilization by 80% by routing all search queries away from core transactional databases.

## Security Impact
- API key scoping allows read-only search keys to be safely embedded in mobile client apps without exposing write access.

## Scalability Impact
- Supports multi-node high-availability clustering for seamless scaling as RTIQA's catalog expands.

## Cost Impact
- Open-source software with minimal RAM requirements significantly lowers hosting expenditure compared to Elasticsearch.

## Why RTIQA Selected This Solution
Typesense delivers instant, typo-tolerant search performance with minimal memory overhead, providing students with a instantaneous discovery experience.

## Future Re-evaluation Criteria
Re-evaluate if Meilisearch adds native multi-tenant index isolation that outperforms Typesense's memory footprint.
