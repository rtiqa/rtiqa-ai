# ADR-0012: AndroidX Media3 ExoPlayer & LiveKit WebRTC Infrastructure

## Metadata
- **Decision ID**: ADR-0012
- **Title**: Video Player & Realtime Streaming Architecture: AndroidX Media3 ExoPlayer & LiveKit WebRTC
- **Status**: Accepted
- **Date**: 2026-08-05
- **Subsystem**: Video Streaming & Virtual Classroom

---

## Context
RTIQA delivers recorded video lectures, interactive course modules, live virtual classrooms, and real-time video tutoring across mobile networks with varying bandwidth capabilities.

## Problem Statement
Building custom video players leads to buffering issues, poor resolution adaptation, battery drain, and lack of DRM protection. Generic WebRTC implementations fail to scale beyond a few video participants in virtual classroom environments.

## Alternatives Considered

1. **AndroidX Media3 ExoPlayer + LiveKit WebRTC**: Google's official Android media player paired with an open-source WebRTC media server framework.
2. **VLC for Android**: Open-source cross-platform media player library.
3. **Jitsi Meet Integration**: Turnkey open-source video conferencing platform.

## Engineering Comparison

| Dimension | Media3 + LiveKit | VLC Android | Jitsi Meet |
| :--- | :--- | :--- | :--- |
| **Android Integration** | Official Google Media3 API | Custom C++ wrappers | Webview / Native SDK |
| **Adaptive Streaming** | HLS / DASH / SmoothStreaming | HLS / DASH | WebRTC only |
| **Live WebRTC Scaling** | SFU (Selective Forwarding Unit) | None | SFU (Jitsi Videobridge) |
| **Compose Binding** | Native Compose Integration | Requires custom View interop| Heavy UI embed |

## Advantages
- AndroidX Media3 ExoPlayer is Google's official media library, featuring adaptive bitrate streaming (HLS/DASH), DRM support (Widevine), video frame caching, and background playback handling.
- LiveKit provides an open-source WebRTC infrastructure capable of hosting interactive virtual classrooms with hundreds of concurrent video feeds, screen sharing, and real-time audio chat.
- Native Android Kotlin SDKs for both ExoPlayer and LiveKit ensure seamless Jetpack Compose UI embedding (`AndroidView` / `LiveKitRoom`).

## Disadvantages
- Self-hosting LiveKit WebRTC media servers requires TURN/STUN server management and bandwidth orchestration.

## Risks
- Video stutter or high battery consumption on low-end Android hardware.
- Mitigated by hardware-accelerated video decoding configuration in ExoPlayer.

## Long-term Maintenance
ExoPlayer is maintained directly by Google's Android Media Team; LiveKit is backed by an active enterprise WebRTC community.

## Performance Impact
- Zero frame drop 60fps video playback with hardware video acceleration.
- Sub-200ms ultra-low latency WebRTC streaming for live Q&A interactions.

## Security Impact
- Widevine DRM protection prevents unauthorized downloading or screen recording of proprietary video content.

## Scalability Impact
- LiveKit SFU architecture scales dynamically across cloud media server clusters.

## Cost Impact
- 100% open-source software (Apache 2.0 license), saving tens of thousands in SaaS video API charges (Agora, Zoom SDK).

## Why RTIQA Selected This Solution
Media3 ExoPlayer provides the best mobile video playback experience on Android, while LiveKit delivers enterprise WebRTC virtual classroom capabilities without software licensing costs.

## Future Re-evaluation Criteria
Re-evaluate if AV1 hardware video decoding becomes mandatory across all budget Android hardware.
