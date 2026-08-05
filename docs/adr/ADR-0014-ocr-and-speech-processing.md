# ADR-0014: Google ML Kit OCR, Whisper.cpp STT & Piper Neural TTS

## Metadata
- **Decision ID**: ADR-0014
- **Title**: Multi-Modal Processing Strategy: Google ML Kit OCR, Whisper.cpp Speech-to-Text & Piper Neural Text-to-Speech
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: OCR & Speech Processing Subsystem

---

## Context
RTIQA provides multimodal learning assistance: scanning physical textbook pages via camera (OCR), voice interaction with the AI Tutor (Speech-to-Text), and audio narration of lessons for visually impaired or audio-first learners (Text-to-Speech).

## Problem Statement
Cloud speech and vision APIs require constant internet access, introduce high latency (1s+), and accumulate substantial per-minute usage fees. Cloud OCR models frequently struggle with physical textbook page reflections and complex layout structures.

## Alternatives Considered

1. **Multimodal Edge Stack (ML Kit + Whisper.cpp + Piper TTS)**: On-device optical recognition, C++ neural speech recognition, and fast local neural speech synthesis.
2. **Cloud API Stack (Google Cloud Vision + Speech-to-Text API + ElevenLabs)**: Pure cloud services.
3. **Tesseract OCR + Android Native Speech Synthesizer**: Legacy open-source components.

## Engineering Comparison

| Dimension | Multimodal Edge Stack | Cloud API Stack | Legacy Stack |
| :--- | :--- | :--- | :--- |
| **Offline Operation** | 100% On-Device Capable | 0% (Fails without internet) | 100% On-Device |
| **Arabic Accuracy** | High (ML Kit Vision + Whisper) | High | Low (Tesseract Arabic errors) |
| **Speech Latency** | < 150ms Local TTS | 800ms+ Network latency | < 100ms Local |
| **Operating Cost** | Zero recurring API fees | High per-minute cloud costs | Zero recurring fees |

## Advantages
- Google ML Kit Text Recognition delivers zero-latency on-device OCR, automatically detecting text lines, paragraphs, and language scripts (Latin & Arabic).
- Whisper.cpp provides C/C++ optimized OpenAI Whisper inference on mobile CPUs/GPUs, accurately converting Arabic speech dialects into text offline.
- Piper TTS generates natural, human-like neural voice audio locally on mobile hardware without network dependency.

## Disadvantages
- Packaging Whisper and Piper voice models increases initial asset download size (~40 MB per model).

## Risks
- CPU throttling or excessive battery drain during prolonged continuous speech recognition.
- Mitigated by voice-activity-detection (VAD) triggers that pause inference when silence is detected.

## Long-term Maintenance
ML Kit maintained by Google; Whisper.cpp maintained by Georgi Gerganov; Piper TTS maintained by the Nabu Casa open-source team.

## Performance Impact
- Instant OCR text highlighting within the live camera view finder.
- Sub-200ms voice query processing.

## Security Impact
- Student voice recordings and textbook camera images never leave the local device during offline processing.

## Scalability Impact
- Edge processing eliminates cloud server scaling limits for speech and image processing.

## Cost Impact
- Saves tens of thousands of dollars annually in cloud vision and speech synthesis API billing.

## Why RTIQA Selected This Solution
This multimodal edge processing stack delivers fast, privacy-safe, offline-capable vision and voice interaction for all RTIQA students.

## Future Re-evaluation Criteria
Re-evaluate if unified multimodal edge models (e.g., Gemini Nano Multimodal) combine Vision, STT, and Text Generation into a single unified NPU package on standard Android devices.
