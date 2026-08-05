# RES-0021: Rejected Alternatives: Elasticsearch & Meilisearch Research Report

## Executive Summary
Full-text search engines—specifically Elasticsearch (Java JVM cluster) and Meilisearch (Rust in-memory search)—were evaluated during search architecture design. This report details the technical reasons for rejecting both in favor of Typesense C++.

---

## Technical Metadata & Overview
- **Technologies Evaluated**: Elasticsearch (Elastic / Java JVM) & Meilisearch (Meilisearch / Rust)
- **Evaluation Subsystem**: Full-Text Search & Catalog Indexing Engine
- **Official Websites**: [elastic.co/elasticsearch](https://www.elastic.co/elasticsearch) / [meilisearch.com](https://www.meilisearch.com)

---

## Detailed Technical Evaluation

### 1. Elasticsearch (Java JVM Cluster Engine)
Elasticsearch is the enterprise industry standard for distributed search and log analytics built on Apache Lucene.
- **Massive RAM Footprint**: Running a production Elasticsearch cluster requires 2 GB+ RAM minimum per node for the Java JVM garbage collection heap alone.
- **Complex Query DSL & Configuration**: Managing Lucene mappings, analyzers, shards, and complex JSON Query DSL introduces operational maintenance overhead.
- **Slow Cold Starts**: JVM startup and index loading take tens of seconds, making instant container scaling slow.

### 2. Meilisearch (Rust In-Memory Engine)
Meilisearch is a modern open-source Rust search engine designed for instant search-as-you-type user experiences.
- **Single-Threaded Indexing Bottleneck**: Meilisearch utilizes a single-threaded indexing model. During heavy batch ingestion of large course catalogs or textbook transcripts, indexing throughput stalls.
- **High Memory Usage During Ingestion**: Meilisearch uses LMDB (Lightning Memory-Mapped Database), which can consume large amounts of virtual memory during index builds.
- **License Limitations**: Dual-licensed under SSPL (Server Side Public License), restricting certain cloud deployment models.

---

## Direct Technical Comparison

| Dimension | Selected: Typesense C++ | Rejected: Elasticsearch | Rejected: Meilisearch |
| :--- | :--- | :--- | :--- |
| **Engine Language** | In-Memory C++ Engine | Java JVM (Lucene) | Rust Engine (LMDB) |
| **Search Response Time**| < 20ms | 100ms+ (JVM overhead) | < 30ms |
| **RAM Footprint** | Low (~150 MB base) | High (2 GB+ RAM min) | Medium during indexing |
| **Typo Tolerance** | Exceptional Out-of-the-Box | Complex Tuning Required | Exceptional |
| **License** | GPLv3 Open Source | Elastic License / SSPL | SSPL / Dual License |

---

## Key Reasons for RTIQA Rejection

1. **Low Resource Footprint**: Typesense C++ requires 10x less RAM than Elasticsearch, enabling high-performance search on lightweight container infrastructure.
2. **Multi-Threaded Indexing Efficiency**: Typesense indexes incoming documents using multi-threaded execution, outperforming Meilisearch's single-threaded indexing bottleneck during batch updates.
3. **Clean Open-Source Licensing**: Typesense provides a permissive open-source model without the cloud hosting restrictions imposed by SSPL licenses.

---

## References & Citations
1. RTIQA Search Benchmark Report: *Typesense vs Elasticsearch vs Meilisearch Throughput* (2026).
2. Open Source License Audit: *Evaluation of SSPL vs GPLv3 for Educational Infrastructure*.
