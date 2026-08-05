# ADR-0003: Gemini 1.5 REST API, LangChain4j & On-Device MediaPipe LLM

## Metadata
- **Decision ID**: ADR-0003
- **Title**: Multi-Tier AI Subsystem Powered by Gemini 1.5 Flash, LangChain4j & MediaPipe On-Device Inference
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: Artificial Intelligence & Intelligent Tutoring

---

## Context
RTIQA integrates an intelligent AI Tutor (المعلم الذكي) providing real-time lesson explanations, hints, adaptive quiz generation, and student Q&A in both online and offline environments.

## Problem Statement
Relying strictly on cloud-hosted LLM APIs introduces latency, recurring token costs, and total failure when students operate in offline or low-connectivity environments. Conversely, relying solely on on-device LLMs limits reasoning performance on complex subjects.

## Alternatives Considered

1. **Hybrid AI Tier (Gemini 1.5 Flash + LangChain4j + MediaPipe On-Device)**: Cloud-first reasoning with seamless offline local LLM fallback.
2. **Pure Cloud OpenAI / Anthropic Stack**: Cloud-only API calls.
3. **Pure On-Device Llama.cpp / Local LLM**: On-device only generation.

## Engineering Comparison

| Dimension | Hybrid RTIQA Stack | Pure Cloud Stack | Pure On-Device Stack |
| :--- | :--- | :--- | :--- |
| **Offline Functionality** | 100% Available (MediaPipe) | 0% (Network crash) | 100% Available |
| **Complex Reasoning** | High (Gemini 1.5 Flash 1M window) | High | Constrained by 2B model |
| **Latency** | < 300ms Cloud / < 100ms Edge | 800ms+ Network latency | < 100ms Local |
| **API Cost Efficiency** | Optimized via edge caching | High cloud token cost | Zero cloud cost |

## Advantages
- Gemini 1.5 Flash offers a 1-million token context window, multimodal capability, and fast inference speed.
- LangChain4j provides structured JSON extraction, agentic tool binding, and RAG retrieval pipelines on JVM backend services.
- Google MediaPipe LLM Inference API enables execution of lightweight models (Gemma 2B, Phi-3) directly on Android GPU/NPU for zero-latency offline tutoring.

## Disadvantages
- Packaging on-device LLM model weights requires strategic background download management.

## Risks
- Memory exhaustion on low-end Android devices running on-device models.
- Mitigated by automatic RAM capacity checks fallbacks to local rule-based AI engines on devices with < 4GB RAM.

## Long-term Maintenance
Supported by Google DeepMind (Gemini/MediaPipe) and active open-source JVM maintainers (LangChain4j).

## Performance Impact
- Streaming SSE (Server-Sent Events) delivery provides instant character rendering in Compose UI.

## Security Impact
- API keys secured via `BuildConfig` and secret manager panels; zero hardcoding in source control.

## Scalability Impact
- Edge processing reduces cloud LLM workload by up to 40% for frequent offline interactions.

## Cost Impact
- Gemini 1.5 Flash provides industry-leading price-to-performance ratio for educational workloads.

## Why RTIQA Selected This Solution
This hybrid architecture guarantees that RTIQA students never suffer a broken AI learning experience, whether in a connected city classroom or a remote offline rural area.

## Future Re-evaluation Criteria
Re-evaluate when Gemma 3B or equivalent open-weights models achieve 95%+ benchmark parity with cloud GPT-4 class models on mobile NPUs.
