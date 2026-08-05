# RES-0010: Typesense Search & Qdrant Vector Database Research Report

## Executive Summary
Instant search across course catalogs and semantic vector retrieval (RAG) across textbook libraries are essential for modern educational platforms. This report evaluates Typesense (in-memory typo-tolerant full-text search) and Qdrant (Rust vector similarity engine) paired with `pgvector` for RTIQA.

---

## Technical Metadata
- **Technologies**: Typesense In-Memory Search Engine & Qdrant Vector Database
- **Primary Domain**: High-Speed Full-Text Search & Vector Retrieval-Augmented Generation (RAG)
- **Official Documentation**: 
  - Typesense: [typesense.org/docs](https://typesense.org/docs)
  - Qdrant: [qdrant.tech/documentation](https://qdrant.tech/documentation)
- **GitHub Repositories**: `typesense/typesense` & `qdrant/qdrant`
- **Licenses**: GPLv3 / Apache 2.0 Open Source
- **Maintainers**: Typesense Inc. & Qdrant Solutions GmbH
- **Community Activity**: Very high (Leading modern search and vector databases)

---

## Architecture & Internals

```
+-------------------------------------------------------------+
|                 RTIQA AI Subsystem / Mobile                 |
+-------------------------------------------------------------+
|    Typesense (Sub-20ms Search) | Qdrant (Sub-15ms Vector RAG)|
|    - C++ In-Memory Engine      | - Rust Engine + HNSW Index  |
|    - Arabic Typo Tolerance     | - Rich JSON Payload Filters |
+-------------------------------------------------------------+
|               PostgreSQL / pgvector Sync Storage            |
+-------------------------------------------------------------+
```

### Technical Highlights
1. **Typesense Search Engine**: Written in C++, Typesense stores indices in RAM to deliver sub-20ms search response times out-of-the-box. Includes native UTF-8 tokenization, typo tolerance, and faceted search for Arabic and English text.
2. **Qdrant Vector DB**: Built in Rust, Qdrant utilizes HNSW (Hierarchical Navigable Small World) graphs for ultra-fast vector similarity search. Features rich metadata payload filtering (e.g., filter vectors by `grade == 10` AND `subject == "Physics"`).
3. **`pgvector` Integration**: Works alongside Qdrant inside Supabase PostgreSQL for lightweight transactional vector operations.

---

## Advantages
- **Sub-20ms Instant Search**: Typesense provides instant search-as-you-type UX for courses, lesson transcripts, and teacher profiles.
- **Rich Payload Filtering**: Qdrant allows filtering vector search results by student grade level, subject, and institutional access control tags directly during vector retrieval.
- **Low Memory Overhead**: Written in C++ and Rust, both engines use significantly less RAM than Java-based search clusters (Elasticsearch / Milvus).
- **Native Arabic Tokenization**: Out-of-the-box support for Arabic text tokenization and character normalization.

## Disadvantages
- **In-Memory RAM Capacity**: Typesense indices must fit within server RAM, requiring RAM scaling as text corpora expand.

---

## Scalability & Performance
- **High QPS**: Qdrant handles thousands of vector similarity queries per second per node.
- **High Availability**: Both Typesense and Qdrant support multi-node clustering with automatic data replication.

---

## Security & Privacy Impact
- **Scoped Search Keys**: API key scoping allows read-only search keys to be safely embedded in mobile applications without exposing write endpoints.
- **On-Premise RAG**: In-house vector storage guarantees that proprietary curriculum content and student queries are never transmitted to third-party vector SaaS vendors.

---

## Enterprise Adoption & Major Users
- **Typesense**: Kicksta, Xero, Sony, Thread.
- **Qdrant**: Deloitte, Boom Supersonic, European Enterprise AI Platforms.

---

## Comparison with Alternatives

| Dimension | Typesense + Qdrant Stack | Elasticsearch | Pinecone / Weaviate Cloud |
| :--- | :--- | :--- | :--- |
| **Engine Language** | C++ / Rust | Java (JVM) | Closed Cloud SaaS / Go |
| **Search Latency** | < 20ms | 100ms+ (JVM overhead) | 50ms+ (Network latency) |
| **RAM Footprint** | Low (~150 MB base) | High (2 GB+ RAM min) | Proprietary SaaS |
| **Payload Filtering** | Rich Native Filtering | Complex Query DSL | Basic Metadata |

---

## Why RTIQA Selected This Solution
Typesense delivers instant typo-tolerant search across course catalogs, while Qdrant provides ultra-fast Rust vector similarity retrieval with rich metadata filtering for textbook RAG.

---

## Future Outlook
Qdrant is introducing on-disk vector index quantizations (Scalar & Binary Quantization), enabling multi-gigabyte vector collections to run on minimal RAM hardware footprints.

---

## References & Citations
1. Typesense C++ Benchmark Documentation: *In-Memory Search Performance* (2026).
2. Qdrant Solutions GmbH: *Rust HNSW Vector Indexing Benchmarks* (https://qdrant.tech).
