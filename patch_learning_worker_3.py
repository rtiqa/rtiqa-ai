import re
content = open('feature-offline/src/main/java/com/rtiqa/feature/offline/worker/LearningProgressSyncWorker.kt').read()

patch = '''            val securityManager = diContainerInstance!!.coreDiContainer.securityManager
            val sessionStore = diContainerInstance!!.restSessionStore
            val sessionId = sessionStore.getSessionId()
            val userId = securityManager.getEncryptedString("user_id") ?: ""

            if (sessionId.isNullOrBlank() || userId.isEmpty()) {
                Log.w(TAG, "Stopping sync gracefully: No active session or user")
                return@withContext Result.success()
            }

            // Retrieve pending sync queue items from Room DB
            val pendingSyncItems = syncDao.getPendingSyncItemsList(userId, sessionId)'''

content = re.sub(
    r'            val securityManager = com\.rtiqa\.core\.security\.EncryptedSecurityManager\(applicationContext\)\s*'
    r'val sessionStore = com\.rtiqa\.core\.network\.session\.RestSessionStoreImpl\(securityManager\)\s*'
    r'val sessionId = sessionStore\.getSessionId\(\)\s*'
    r'val userId = securityManager\.getEncryptedString\("user_id"\) \?: ""\s*'
    r'if \(sessionId\.isNullOrBlank\(\) \|\| userId\.isEmpty\(\)\) \{\s*'
    r'Log\.w\(TAG, "Stopping sync gracefully: No active session or user"\)\s*'
    r'return@withContext Result\.success\(\)\s*'
    r'\}\s*'
    r'// Retrieve pending sync queue items from Room DB\s*'
    r'val pendingSyncItems = syncDao\.getPendingSyncItemsList\(userId, sessionId\)',
    patch, content
)
open('feature-offline/src/main/java/com/rtiqa/feature/offline/worker/LearningProgressSyncWorker.kt', 'w').write(content)
