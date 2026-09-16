#!/bin/bash
sed -i 's/securityManager.putEncryptedString(KEY_AUTH_TOKEN, /sessionStore.saveSession(/g' /app/applet/core-data/src/main/java/com/rtiqa/core/data/repository/AuthRepositoryImpl.kt
# We need to replace `sessionStore.saveSession(authBody.token)` with `sessionStore.saveSession(authBody.token, null)`
sed -i 's/sessionStore.saveSession(authBody.token)/sessionStore.saveSession(authBody.token, null)/g' /app/applet/core-data/src/main/java/com/rtiqa/core/data/repository/AuthRepositoryImpl.kt
sed -i 's/sessionStore.saveSession("remote_token_$uid")/sessionStore.saveSession("remote_token_$uid", null)/g' /app/applet/core-data/src/main/java/com/rtiqa/core/data/repository/AuthRepositoryImpl.kt
sed -i 's/sessionStore.saveSession("offline_token_$newUserId")/sessionStore.saveSession("offline_token_$newUserId", null)/g' /app/applet/core-data/src/main/java/com/rtiqa/core/data/repository/AuthRepositoryImpl.kt
