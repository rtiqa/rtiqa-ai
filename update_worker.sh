#!/bin/bash
sed -i 's/val okHttpClient = RetrofitNetworkClient.createOkHttpClient(securityManager)/val sessionStore = RestSessionStoreImpl(securityManager)\n        val okHttpClient = RetrofitNetworkClient.createOkHttpClient(securityManager, sessionStore)/' /app/applet/core-data/src/main/java/com/rtiqa/core/data/worker/OfflineSyncWorker.kt
sed -i '/val preferencesDataStore/!b;n;/val sessionStore/d' /app/applet/core-data/src/main/java/com/rtiqa/core/data/worker/OfflineSyncWorker.kt
