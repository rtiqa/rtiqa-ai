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

/**
 * Manager responsible for triggering and queuing offline synchronization.
 */
class OfflineSyncManager(
    private val apiService: RtiqaApiService,
    private val courseDao: CourseDao,
    private val syncDao: SyncDao
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
        return try {
            val syncItem = SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                actionType = actionType,
                payloadJson = payloadJson,
                createdAt = System.currentTimeMillis()
            )
            syncDao.insertSyncItem(syncItem)
            RtiqaLog.i(tag, "Queued offline action ($actionType) into SyncQueue database.")
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.DatabaseError("Failed to enqueue offline action", e))
        }
    }

    override fun observePendingSyncCount(): Flow<Int> {
        return syncDao.getAllPendingSyncItems().map { it.size }
    }
}
