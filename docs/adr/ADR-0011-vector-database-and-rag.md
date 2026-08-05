# ADR-0011: Qdrant Vector Database & pgvector for Textbook RAG

## Metadata
- **Decision ID**: ADR-0011
- **Title**: Dual Vector Search Architecture: Qdrant Engine & PostgreSQL pgvector for Textbook RAG
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: Vector Search & Retrieval-Augmented Generation (RAG)

---

## Context
RTIQA's AI Tutor requires Retrieval-Augmented Generation (RAG) capabilities to answer student questions based on validated textbook content, syllabus documents, and exam archives without hallucinating incorrect answers.

## Problem Statement
Standard keyword search fails to capture semantic meaning in educational queries (e.g., matching "how do plants make food" with "photosynthesis"). Cloud-only vector APIs introduce recurring vector search costs and data privacy concerns.

## Alternatives Considered

1. **Dual Vector Architecture (Qdrant + pgvector)**: Qdrant Rust engine for high-QPS complex vector filtering + `pgvector` inside Supabase PostgreSQL for lightweight transactional vector operations.
2. **Milvus**: Cloud-native distributed vector database.
3. **ChromaDB**: Python-native vector database.

## Engineering Comparison

| Dimension | Qdrant + pgvector | Milvus | ChromaDB |
| :--- | :--- | :--- | :--- |
| **Language & Engine** | Rust / C++ Extensions | Go / C++ Distributed | Python Engine |
| **Payload Filtering** | Rich JSON payload filtering | Complex filtering setup | Basic payload filters |
| **Deployment** | Single Binary / Container | Kubernetes Cluster | Microservice Container |
| **Performance** | Industry-leading QPS / RAM | High horizontal scaling | Lower single-node throughput |

## Advantages
- Qdrant provides ultra-fast vector similarity search with rich metadata payload filtering (e.g., filter vectors by `grade_level == 10` AND `subject == "Physics"`).
- `pgvector` extension allows relational PostgreSQL queries to combine vector similarity with standard SQL joins directly inside Supabase.
- Eliminates reliance on proprietary SaaS vector databases (Pincone, Weaviate Cloud), protecting student data privacy.

## Disadvantages
- Requires managing vector embedding generation pipelines for uploaded PDF textbooks.

## Risks
- Embedding model mismatch if vector dimension sizes differ between indexing and query generation.
- Mitigated by standardized 768-dim embedding models enforced across backend RAG microservices.

## Long-term Maintenance
Qdrant is Apache 2.0 open source with strong institutional funding; `pgvector` is part of the core PostgreSQL ecosystem.

## Performance Impact
- Sub-15ms vector similarity retrieval across millions of indexed textbook paragraphs.

## Security Impact
- Ensures textbook RAG embeddings are stored in-house; zero exposure of proprietary curriculum content to third parties.

## Scalability Impact
- Horizontally scalable vector indexing supporting millions of educational document chunks.

## Cost Impact
- 100% open-source solution eliminating thousands of dollars in monthly third-party vector DB SaaS fees.

## Why RTIQA Selected This Solution
This hybrid architecture combines the simplicity of PostgreSQL `pgvector` for inline relational queries with the raw vector performance and payload filtering of Qdrant for enterprise RAG.

## Future Re-evaluation Criteria
Re-evaluate if on-device quantized vector search engines achieve sub-10ms search performance on Android devices for offline RAG.
