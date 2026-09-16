#!/bin/bash
sed -i '/val restSessionStore: RestSessionStore by lazy {/,+2d' /app/applet/core-data/src/main/java/com/rtiqa/core/data/di/AppDiContainer.kt
sed -i '/val okHttpClient by lazy {/i \    val restSessionStore: RestSessionStore by lazy {\n        RestSessionStoreImpl(coreDiContainer.securityManager)\n    }\n' /app/applet/core-data/src/main/java/com/rtiqa/core/data/di/AppDiContainer.kt
sed -i 's/RetrofitNetworkClient.createOkHttpClient(/RetrofitNetworkClient.createOkHttpClient(\n            sessionStore = restSessionStore,/' /app/applet/core-data/src/main/java/com/rtiqa/core/data/di/AppDiContainer.kt
