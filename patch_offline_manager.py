import re
content = open('core-data/src/main/java/com/rtiqa/core/data/sync/OfflineSyncManager.kt').read()

content = content.replace(
    'private val sessionStore: RestSessionStore\n) : OfflineSyncContract {',
    'private val sessionStore: RestSessionStore,\n    private val securityManager: com.rtiqa.core.security.SecurityManager? = null\n) : OfflineSyncContract {'
)

content = content.replace(
    '''val syncItem = SyncQueueEntity(
                    id = UUID.randomUUID().toString(),
                    actionType = actionType,
                    payloadJson = payloadJson,
                    createdAt = System.currentTimeMillis()
                )''',
    '''val userId = securityManager?.getEncryptedString("user_id") ?: "legacy_user"
                val syncItem = SyncQueueEntity(
                    id = UUID.randomUUID().toString(),
                    actionType = actionType,
                    payloadJson = payloadJson,
                    createdAt = System.currentTimeMillis(),
                    ownerUserId = userId,
                    ownerSessionId = sessionId
                )'''
)

content = content.replace(
    '''val pendingItems = syncDao.getPendingSyncItemsList()''',
    '''val sessionId = sessionStore.getSessionId() ?: return RtiqaResult.Success(Unit)
            val userId = securityManager?.getEncryptedString("user_id") ?: "legacy_user"
            val pendingItems = syncDao.getPendingSyncItemsList(userId, sessionId)'''
)

content = content.replace(
    '''override fun observePendingSyncCount(): Flow<Int> {
        return syncDao.getAllPendingSyncItems().map { it.size }
    }''',
    '''override fun observePendingSyncCount(): Flow<Int> {
        val sessionId = sessionStore.getSessionId() ?: "legacy_session"
        val userId = securityManager?.getEncryptedString("user_id") ?: "legacy_user"
        return syncDao.getAllPendingSyncItems(userId, sessionId).map { it.size }
    }'''
)

open('core-data/src/main/java/com/rtiqa/core/data/sync/OfflineSyncManager.kt', 'w').write(content)
