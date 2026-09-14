import re
content = open('feature-offline/src/main/java/com/rtiqa/feature/offline/worker/LearningProgressSyncWorker.kt').read()

patch = '''            val securityManager = com.rtiqa.core.security.EncryptedSecurityManager(applicationContext)
            val sessionStore = com.rtiqa.core.network.session.RestSessionStoreImpl(securityManager)
            val sessionId = sessionStore.getSessionId()
            val userId = securityManager.getEncryptedString("user_id") ?: ""

            if (sessionId.isNullOrBlank() || userId.isEmpty()) {
                Log.w(TAG, "Stopping sync gracefully: No active session or user")
                return@withContext Result.success()
            }

            // Retrieve pending sync queue items from Room DB
            val pendingSyncItems = syncDao.getPendingSyncItemsList(userId, sessionId)'''

content = content.replace('''            val securityManager = com.rtiqa.core.security.EncryptedSecurityManager(applicationContext)
            val sessionStore = com.rtiqa.core.network.session.RestSessionStoreImpl(securityManager)
            val sessionId = sessionStore.getSessionId()
            val userId = securityManager.getEncryptedString("user_id")

            if (sessionId.isNullOrBlank() || userId.isNullOrBlank()) {
                Log.w(TAG, "Stopping sync gracefully: No active session or user")
                return@withContext Result.success()
            }

            // Retrieve pending sync queue items from Room DB
            val pendingSyncItems = syncDao.getPendingSyncItemsList(userId, sessionId)''', patch)
open('feature-offline/src/main/java/com/rtiqa/feature/offline/worker/LearningProgressSyncWorker.kt', 'w').write(content)
