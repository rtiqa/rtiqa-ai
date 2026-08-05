# RES-0006: AndroidX Media3 ExoPlayer & LiveKit WebRTC Research Report

## Executive Summary
Delivering video lectures, interactive course media, and real-time virtual classroom sessions across mobile networks requires adaptive video streaming and scalable WebRTC infrastructure. This report evaluates AndroidX Media3 ExoPlayer (recorded playback) and LiveKit WebRTC (live interactive sessions) for RTIQA.

---

## Technical Metadata
- **Technologies**: AndroidX Media3 ExoPlayer & LiveKit WebRTC SFU Architecture
- **Primary Domain**: Video Lecture Streaming & Realtime Virtual Classrooms
- **Official Documentation**: 
  - Media3: [developer.android.com/media/media3](https://developer.android.com/media/media3)
  - LiveKit: [docs.livekit.io](https://docs.livekit.io)
- **GitHub Repositories**: `androidx/media` & `livekit/livekit`
- **Licenses**: Apache License 2.0 (100% Open Source)
- **Maintainers**: Google Android Media Team & LiveKit Inc.
- **Community Activity**: Very high (Industry standard for Android media and WebRTC)

---

## Architecture & Internals

```
+-------------------------------------------------------------+
|                     Jetpack Compose UI                      |
+-------------------------------------------------------------+
|  ExoPlayer View (Media3)   |    LiveKit Video Room (WebRTC) |
+-------------------------------------------------------------+
|  HLS / DASH Adaptive Engine|    Selective Forwarding Unit   |
+-------------------------------------------------------------+
|  Hardware Media Codecs     |    LiveKit Go SFU Server Cluster|
+-------------------------------------------------------------+
```

### Core Architecture
1. **AndroidX Media3 ExoPlayer**: Google's extensible application-level media player. Supports adaptive streaming formats (HLS, DASH, SmoothStreaming), Widevine DRM encryption, offline video caching, and background audio playback.
2. **LiveKit WebRTC SFU**: Go-based Selective Forwarding Unit (SFU) media server that routes low-latency WebRTC video/audio streams between hundreds of classroom participants without mixing overhead.

---

## Advantages
- **Adaptive Bitrate Streaming (ABR)**: Automatically adjusts video quality based on student network conditions, preventing video stuttering on weak 3G/4G connections.
- **Sub-200ms Latency**: LiveKit WebRTC infrastructure provides real-time voice and video interaction between teachers and students during virtual classes.
- **Widevine DRM Support**: Built-in support for digital rights management protects proprietary educational video content from unauthorized downloading or screen capture.
- **Native Compose Integration**: Clean integration into Jetpack Compose via `AndroidView` wrapping ExoPlayer `PlayerView` and LiveKit Room state primitives.

## Disadvantages
- **Media Server Orchestration**: Self-hosting LiveKit SFU clusters requires TURN/STUN bandwidth routing configuration.

---

## Scalability & Performance
- **Hardware Video Acceleration**: Direct binding to Android `MediaCodec` enables smooth 1080p/4K video decoding with minimal battery consumption.
- **SFU Scalability**: LiveKit SFU architecture handles over 100,000 concurrent video streams per server cluster.

---

## Security & Privacy Impact
- **End-to-End Encryption Options**: LiveKit supports E2EE WebRTC data channels for private audio/video calls.
- **Encrypted Local Cache**: Cached HLS video segments stored on mobile devices are encrypted to prevent unauthorized extraction.

---

## Enterprise Adoption & Major Users
- **Google**: YouTube, Google TV, Android OS System Apps.
- **OpenAI**: ChatGPT voice mode infrastructure powered by LiveKit WebRTC.
- **Twitch, Spotify, HBO Max, NBC**: Primary media streaming engines.

---

## Comparison with Alternatives

| Dimension | Media3 + LiveKit Stack | Native WebRTC SDK | Agora / Zoom Commercial SaaS |
| :--- | :--- | :--- | :--- |
| **Adaptive Streaming** | HLS / DASH / WebRTC | WebRTC only | Proprietary WebRTC |
| **Server Architecture**| Open-Source LiveKit SFU | Custom SFU required | Closed Proprietary SaaS |
| **DRM Support** | Widevine DRM Built-in | None | Limited |
| **Operating Cost** | 100% Open Source (Zero fees)| Zero fees | High per-minute SaaS billing |

---

## Why RTIQA Selected This Solution
Media3 ExoPlayer provides the best Android video playback engine, while LiveKit delivers enterprise open-source WebRTC virtual classroom capabilities without recurring API usage costs.

---

## Future Outlook
LiveKit continues to expand WebRTC agent capabilities for AI voice interaction, aligning directly with RTIQA's intelligent voice tutoring roadmap.

---

## References & Citations
1. Google Android Developer Documentation: *AndroidX Media3 Overview* (2026).
2. LiveKit Architectural Documentation: *Open Source WebRTC SFU Scaling* (https://livekit.io).
