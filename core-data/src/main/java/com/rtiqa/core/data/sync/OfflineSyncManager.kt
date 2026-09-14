package com.rtiqa.core.data.sync

import com.rtiqa.core.data.mapper.toEntity
import com.rtiqa.core.database.dao.CourseDao
import com.rtiqa.core.database.dao.SyncDao
import com.rtiqa.core.database.entity.SyncQueueEntity
import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.repository.OfflineSyncContract
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.logging.RtiqaLog
import com.rtiqa.core.network.api.RtiqaApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

import com.rtiqa.core.network.session.RestSessionStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manager responsible for triggering and queuing offline synchronization.
 */
class OfflineSyncManager(
    private val apiService: RtiqaApiService,
    private val courseDao: CourseDao,
    private val syncDao: SyncDao,
    private val syncMutex: Mutex,
    private val sessionStore: RestSessionStore,
    private val securityManager: com.rtiqa.core.security.SecurityManager? = null
) : OfflineSyncContract {
    private val tag = "OfflineSyncManager"

    override suspend fun syncRemoteCourses(): RtiqaResult<Unit> {
        return try {
            val response = apiService.getCourses()
            if (response.isSuccessful) {
                val dtos = response.body().orEmpty()
                val entities = dtos.map { it.toEntity() }
                courseDao.insertCourses(entities)
                RtiqaLog.i(tag, "Successfully synced ${entities.size} remote courses into database.")
                RtiqaResult.Success(Unit)
            } else {
                RtiqaLog.w(tag, "Remote course fetch returned error code: ${response.code()}")
                RtiqaResult.Error(RtiqaError.NetworkError("HTTP error ${response.code()}: ${response.message()}", statusCode = response.code()))
            }
        } catch (e: Exception) {
            RtiqaLog.e(tag, "Failed to sync remote courses", e)
            RtiqaResult.Error(RtiqaError.SyncError("Failed to sync remote courses", e))
        }
    }

    override suspend fun enqueueOfflineAction(actionType: String, payloadJson: String): RtiqaResult<Unit> {
        return syncMutex.withLock {
            val sessionId = sessionStore.getSessionId()
            if (sessionId.isNullOrBlank()) {
                RtiqaLog.w(tag, "Refusing to enqueue offline action: No active session")
                return@withLock RtiqaResult.Error(RtiqaError.AuthError("No active session"))
            }
            try {
                val userId = securityManager?.getEncryptedString("user_id") ?: "legacy_user"
                val syncItem = SyncQueueEntity(
                    id = UUID.randomUUID().toString(),
                    actionType = actionType,
                    payloadJson = payloadJson,
                    createdAt = System.currentTimeMillis(),
                    ownerUserId = userId,
                    ownerSessionId = sessionId
                )
                syncDao.insertSyncItem(syncItem)
                RtiqaLog.i(tag, "Queued offline action ($actionType) into SyncQueue database.")
                RtiqaResult.Success(Unit)
            } catch (e: Exception) {
                RtiqaResult.Error(RtiqaError.DatabaseError("Failed to enqueue offline action", e))
            }
        }
    }

    suspend fun syncPendingItemsNow(): RtiqaResult<Unit> {
        // Simple direct sync logic mirroring the worker for immediate sync during logout.
        // It's outside mutex in the caller.
        try {
            val sessionId = sessionStore.getSessionId() ?: return RtiqaResult.Success(Unit)
            val userId = securityManager?.getEncryptedString("user_id") ?: "legacy_user"
            val pendingItems = syncDao.getPendingSyncItemsList(userId, sessionId)
            if (pendingItems.isEmpty()) return RtiqaResult.Success(Unit)
            
            val payload = com.rtiqa.core.network.api.NetworkSyncPayloadDto(
                userId = sessionStore.getSessionId() ?: "anonymous", // In a real app this uses the actual user ID
                progressUpdates = pendingItems.map { item ->
                    mapOf("id" to item.id, "type" to item.actionType, "payload" to item.payloadJson)
                },
                lastSyncedTimestamp = System.currentTimeMillis()
            )
            val response = apiService.syncOfflineData(payload)
            if (response.isSuccessful && response.body()?.success == true) {
                pendingItems.forEach { syncDao.deleteSyncItem(it.id) }
                return RtiqaResult.Success(Unit)
            }
            return RtiqaResult.Error(RtiqaError.NetworkError("Immediate sync failed"))
        } catch (e: Exception) {
            return RtiqaResult.Error(RtiqaError.NetworkError("Immediate sync failed", cause = e))
        }
    }

    override fun observePendingSyncCount(): Flow<Int> {
        val sessionId = sessionStore.getSessionId() ?: "legacy_session"
        val userId = securityManager?.getEncryptedString("user_id") ?: "legacy_user"
        return syncDao.getAllPendingSyncItems(userId, sessionId).map { it.size }
    }
}
