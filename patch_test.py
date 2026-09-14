import sys
content = open('core-data/src/test/java/com/rtiqa/core/data/repository/QuizRepositoryImplTest.kt').read()
content = content.replace(
    'override fun getAllPendingSyncItems(): Flow<List<SyncQueueEntity>> = flowOf(items)',
    'override fun getAllPendingSyncItems(userId: String, sessionId: String): Flow<List<SyncQueueEntity>> = kotlinx.coroutines.flow.flowOf(items.filter { it.ownerUserId == userId && it.ownerSessionId == sessionId })'
)
content = content.replace(
    'override suspend fun getPendingSyncItemsList(): List<SyncQueueEntity> = items',
    'override suspend fun getPendingSyncItemsList(userId: String, sessionId: String): List<SyncQueueEntity> = items.filter { it.ownerUserId == userId && it.ownerSessionId == sessionId }'
)
open('core-data/src/test/java/com/rtiqa/core/data/repository/QuizRepositoryImplTest.kt', 'w').write(content)
