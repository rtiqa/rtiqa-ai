# 🔌 RTIQA API & Service Specifications

This document defines the REST, gRPC, WebRTC, and Authentication APIs powering the **RTIQA** educational platform.

---

## 1. Ktor Backend Microservice Gateway (Port 8081)

The backend gateway acts as the primary API aggregator and service router for RTIQA mobile clients.

### Health Check Endpoint
```http
GET /health
```
**Response (200 OK):**
```json
{
  "status": "UP",
  "service": "RTIQA Microservice Gateway Engine",
  "timestamp": 1785947500000
}
```

### System Platform Status
```http
GET /api/v1/status
```
**Response (200 OK):**
```json
{
  "version": "1.0.0",
  "environment": "development",
  "auth_provider": "Keycloak IAM OIDC",
  "db_provider": "Supabase PostgreSQL 15 + pgvector",
  "search_provider": "Typesense 26.0",
  "vector_provider": "Qdrant Rust v1.8.0",
  "media_provider": "LiveKit WebRTC SFU v1.6.0"
}
```

---

## 2. Gemini 1.5 AI Tutor REST API

RTIQA mobile clients connect to Google Gemini API for intelligent AI tutoring.

### Chat Completions Endpoint
```http
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${GEMINI_API_KEY}
```

**Payload Schema:**
```json
{
  "contents": [
    {
      "role": "user",
      "parts": [
        {
          "text": "Explain quadratic equations step by step in Arabic for a 10th grade student."
        }
      ]
    }
  ],
  "generationConfig": {
    "temperature": 0.7,
    "topK": 40,
    "topP": 0.95,
    "maxOutputTokens": 1024
  }
}
```

---

## 3. Keycloak IAM Authentication (Port 8080)

Identity authentication is managed via OpenID Connect (OIDC) Authorization Code Flow with PKCE.

- **Realm Name**: `RTIQA`
- **Mobile Client ID**: `rtiqa-mobile-app` (Public Client with PKCE S256)
- **Token Endpoint**: `POST /realms/RTIQA/protocol/openid-connect/token`
- **User Info Endpoint**: `GET /realms/RTIQA/protocol/openid-connect/userinfo`

---

## 4. LiveKit WebRTC SFU Media API (Port 7880)

Virtual classroom audio/video streaming is powered by LiveKit SFU.

- **WebSocket Signaling**: `ws://<domain>:7880/rtc`
- **RTC TCP**: `Port 7881`
- **RTC UDP**: `Port 7882`
- **Authentication**: JWT signed with `LIVEKIT_API_KEY` & `LIVEKIT_API_SECRET`

---

## 5. Typesense Search API (Port 8108)

Sub-20ms search API for course materials and lesson catalogs.

- **Search Endpoint**: `GET /collections/courses/documents/search?q=math&query_by=title,description`
- **Header**: `X-TYPESENSE-API-KEY: ${TYPESENSE_API_KEY}`

---

## 6. Qdrant Vector Search API (Port 6333 REST / 6334 gRPC)

Textbook vector embeddings and semantic search engine for RAG.

- **Collection Search**: `POST /collections/textbooks/points/search`
- **Header**: `api-key: ${QDRANT_API_KEY}`
