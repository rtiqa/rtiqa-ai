import re
content = open('core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt').read()
content = content.replace(
    '@Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")\n    fun getAllPendingSyncItems(): Flow<List<com.rtiqa.core.database.entity.SyncQueueEntity>>',
    '@Query("SELECT * FROM sync_queue WHERE ownerUserId = :userId AND ownerSessionId = :sessionId ORDER BY createdAt ASC")\n    fun getAllPendingSyncItems(userId: String, sessionId: String): Flow<List<com.rtiqa.core.database.entity.SyncQueueEntity>>'
)

content = content.replace(
    '@Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")\n    suspend fun getPendingSyncItemsList(): List<com.rtiqa.core.database.entity.SyncQueueEntity>',
    '@Query("SELECT * FROM sync_queue WHERE ownerUserId = :userId AND ownerSessionId = :sessionId ORDER BY createdAt ASC")\n    suspend fun getPendingSyncItemsList(userId: String, sessionId: String): List<com.rtiqa.core.database.entity.SyncQueueEntity>'
)
open('core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt', 'w').write(content)
