# RES-0022: Rejected Alternatives: ChromaDB, Milvus & Weaviate Research Report

## Executive Summary
Vector databases designed for AI Retrieval-Augmented Generation (RAG)—specifically ChromaDB (Python), Milvus (distributed Go/C++), and Weaviate Cloud (Go/SaaS)—were evaluated for textbook semantic search. This report documents the technical reasons for rejecting all three in favor of Qdrant Rust and PostgreSQL `pgvector`.

---

## Technical Metadata & Overview
- **Technologies Evaluated**: ChromaDB (Python), Milvus (Zilliz / C++) & Weaviate Cloud (Weaviate / Go)
- **Evaluation Subsystem**: Vector Database & Retrieval-Augmented Generation (RAG)
- **Official Websites**: [trychroma.com](https://www.trychroma.com) / [milvus.io](https://milvus.io) / [weaviate.io](https://weaviate.io)

---

## Detailed Technical Evaluation

### 1. ChromaDB (Python-Native Vector Storage)
ChromaDB is an open-source embedding database designed for rapid prototyping with Python AI scripts.
- **Python Runtime Bottleneck**: ChromaDB is built in Python (wrapping ClickHouse/DuckDB). Its single-threaded Python event loop cannot handle high QPS (queries per second) in production microservices.
- **Limited Multi-Tenant Payload Filtering**: Lacks granular payload filtering logic required to restrict vector searches by student grade level and school ID.

### 2. Milvus (Distributed Cloud-Native Vector DB)
Milvus is a heavy open-source distributed vector database built for enterprise-scale vector search.
- **Excessive Infrastructure Complexity**: Milvus requires running Etcd, MinIO, Pulsar, and multiple distributed query nodes just for a minimal cluster, making small and medium deployments unmanageable.
- **High Resource Requirements**: Baseline deployment requires 8 GB+ RAM before indexing any vector embeddings.

### 3. Weaviate Cloud (Go-Based Vector Engine & SaaS)
Weaviate is a vector search engine that stores both objects and vectors, offering GraphQL and REST interfaces.
- **Proprietary SaaS Vendor Push**: While open-source binaries exist, Weaviate heavily pushes users toward their expensive managed cloud SaaS platform.
- **Complex GraphQL Query Syntax**: Searching vectors requires complex GraphQL query wrappers compared to Qdrant's simple REST/gRPC payloads.

---

## Direct Technical Comparison

| Dimension | Selected: Qdrant + pgvector | Rejected: ChromaDB | Rejected: Milvus | Rejected: Weaviate |
| :--- | :--- | :--- | :--- | :--- |
| **Engine Language** | Rust / C++ Extension | Python | Go / C++ Distributed | Go |
| **Deployment** | Single Binary Container | Python Process | 5+ Distributed Services| Container / Managed Cloud |
| **Payload Filtering** | Rich JSON Payload Filter | Basic Metadata Filter | Complex Query DSL | GraphQL Payload Filters |
| **Performance (QPS)**| Industry Leading (Rust) | Low (Python GIL) | High (At Scale) | High |
| **Resource Footprint**| Low (~200 MB RAM base) | Low / Slow | High (8 GB+ RAM min) | Medium (~1 GB RAM) |

---

## Key Reasons for RTIQA Rejection

1. **Rust Execution Performance**: Qdrant's Rust engine provides industry-leading QPS and low memory usage without Python GIL thread locking.
2. **Deployment Simplicity**: Qdrant runs as a single, lightweight Docker container, eliminating the infrastructure complexity of Milvus (Etcd/Pulsar/MinIO).
3. **Dual Vector Strategy (`pgvector`)**: Supabase PostgreSQL `pgvector` allows RTIQA to perform vector similarity queries directly inside relational database joins, reserving Qdrant for complex high-QPS payload filtered RAG operations.

---

## References & Citations
1. RTIQA Vector DB Benchmark Report: *Qdrant Rust vs ChromaDB vs Milvus Latency & QPS* (2026).
2. Qdrant Benchmarking Suite: *Vector Search Performance on Mobile & Edge Datasets*.
