# RES-0005: Dagger Hilt & Coil Image Loader Research Report

## Executive Summary
Dependency injection and efficient image loading are critical for maintaining clean code separation and responsive UI performance in mobile applications. This report evaluates Dagger Hilt (dependency injection framework) and Coil (Coroutine-backed image loading library) for the RTIQA Android client.

---

## Technical Metadata
- **Technologies**: Dagger Hilt & Coil Image Loader
- **Primary Domain**: Dependency Injection Framework & Asynchronous Image Engine
- **Official Documentation**: 
  - Hilt: [developer.android.com/training/dependency-injection/hilt-android](https://developer.android.com/training/dependency-injection/hilt-android)
  - Coil: [coil-kt.github.io/coil](https://coil-kt.github.io/coil)
- **GitHub Repositories**: `google/dagger` & `coil-kt/coil`
- **License**: Apache License 2.0 (100% Open Source)
- **Maintainers**: Google Dagger Team & Coil Open Source Team (Colin White)
- **Community Activity**: Very high (Official Android Jetpack recommended libraries)

---

## Architecture & Internals

```
+-------------------------------------------------------------+
|                     Jetpack Compose UI                      |
+-------------------------------------------------------------+
|       AsyncImage (Coil Engine + OkHttp Cache + Memory)      |
+-------------------------------------------------------------+
|       ViewModel Inject (@HiltViewModel @Inject Constructor) |
+-------------------------------------------------------------+
|         Hilt Dependency Graph (SingletonComponent)          |
+-------------------------------------------------------------+
|           Repositories / Room DB / Network API              |
+-------------------------------------------------------------+
```

### Technical Highlights
1. **Dagger Hilt**: Standardizes Dagger on Android by providing pre-built lifecycle components (`SingletonComponent`, `ActivityComponent`, `ViewModelComponent`). Performs compile-time code generation without runtime reflection.
2. **Coil (Coroutine Image Loader)**: Modern image loading library built specifically for Kotlin and Coroutines. Integrates directly with Jetpack Compose (`AsyncImage`), OkHttp network stack, and disk/memory cache pipelines.

---

## Advantages
- **Compile-Time Safety**: Hilt validates the entire dependency injection graph during compilation, preventing runtime `NullPointerException` or missing binding crashes.
- **Kotlin-First Image Engine**: Coil leverages Kotlin Coroutines, Kotlin Flow, and OkHttp for asynchronous image fetching, avoiding heavy legacy thread management pools.
- **Jetpack Compose Native**: Coil's `AsyncImage` composable offers smooth image loading transitions, crossfades, and placeholder renderings out-of-the-box.
- **Minimal Footprint**: Coil adds ~2,000 methods to compiled binaries compared to Glide's ~14,000 methods.

## Disadvantages
- **Hilt KSP Build Time**: Generating Dagger component classes adds incremental time during fresh application builds.

---

## Scalability & Performance
- **Memory Caching**: Coil dynamically calculates memory cache sizes based on available device RAM, preventing `OutOfMemoryError` (OOM) during heavy image scrolling in course catalogs.
- **Decoupled Architecture**: Hilt enforces modularity across multi-module projects, allowing feature modules to inject shared dependencies cleanly.

---

## Security & Privacy Impact
- **Encrypted Image Cache**: Coil OkHttp disk caches can be bound to encrypted storage directories to protect confidential educational media assets.
- **Zero Reflection Exploits**: Hilt generates pure Java/Kotlin code at compile time, eliminating reflection-based vulnerability vectors.

---

## Enterprise Adoption & Major Users
- **Google**: Google Play, YouTube, Google Photos, Android OS Apps.
- **Twitter / X, Netflix, Tinder, Target**: Heavy reliance on Hilt and Coil for Android clients.

---

## Comparison with Alternatives

| Dimension | Hilt + Coil Stack | Koin + Glide Stack | Manual Constructor Injection |
| :--- | :--- | :--- | :--- |
| **DI Validation** | 100% Compile-Time checking | Runtime Resolution | 100% Compile-Time |
| **Image Loading** | Coroutine Native (Coil) | Legacy Java Threads (Glide) | Manual Bitmap Decoding |
| **Compose Integration**| Native (`hiltViewModel()`) | Basic Compose extensions | Manual Injection |
| **Binary Footprint** | Extremely Lightweight | Heavy Glide method count | Zero dependencies |

---

## Why RTIQA Selected This Solution
Dagger Hilt provides compile-time dependency safety across multi-module codebases, while Coil offers the fastest, lightest Kotlin-native image loading engine for Jetpack Compose.

---

## Future Outlook
Coil 3.0 is expanding KMP support for cross-platform image loading, while Hilt continues to receive active feature updates from Google's Android Architecture team.

---

## References & Citations
1. Google Android Developer Documentation: *Dependency Injection with Hilt* (2026).
2. Coil Engineering Documentation: *Coroutines Image Loading Architecture* (https://coil-kt.github.io/coil).
