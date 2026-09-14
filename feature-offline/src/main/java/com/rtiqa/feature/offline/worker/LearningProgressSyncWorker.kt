package com.rtiqa.feature.offline.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rtiqa.core.data.di.AppDiContainer
import com.rtiqa.core.database.RtiqaDatabase
import com.rtiqa.feature.offline.data.OfflineSyncPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager CoroutineWorker for automatically syncing local learning progress with Remote Sync DataSource
 * when network connectivity is available.
 */
class LearningProgressSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "LearningProgressSyncWorker"
        const val WORK_NAME = "learning_progress_sync_work"
        const val REMOTE_COLLECTION_PROGRESS = "learning_progress"
        
        // Cache the container instance statically to avoid redundant instantiations per work execution
        private var diContainerInstance: AppDiContainer? = null
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting learning progress sync worker...")
        try {
            val db = RtiqaDatabase.getInstance(applicationContext)
            val syncDao = db.syncDao()
            
            // Ensure single container initialization
            if (diContainerInstance == null) {
                diContainerInstance = AppDiContainer(applicationContext)
            }
            val remoteSyncDataSource = diContainerInstance!!.remoteSyncDataSource

            val securityManager = diContainerInstance!!.coreDiContainer.securityManager
            val sessionId = securityManager.getEncryptedString("rtiqa_rest_session_id", "") as String
            val userId = securityManager.getEncryptedString("user_id", "") as String

            if (sessionId.isBlank() || userId.isBlank()) {
                Log.w(TAG, "Stopping sync gracefully: No active session or user")
                return@withContext Result.success()
            }

            // Retrieve pending sync queue items from Room DB
            val pendingSyncItems = syncDao.getPendingSyncItemsList(userId, sessionId)
            Log.d(TAG, "Found ${pendingSyncItems.size} pending items to sync.")

            for (item in pendingSyncItems) {
                val dataMap = mapOf(
                    "id" to item.id,
                    "actionType" to item.actionType,
                    "payloadJson" to item.payloadJson,
                    "createdAt" to item.createdAt,
                    "syncedAt" to System.currentTimeMillis()
                )

                // Push payload to Remote collection
                val result = remoteSyncDataSource.pushSyncPayload(REMOTE_COLLECTION_PROGRESS, item.id, dataMap)
                if (result is com.rtiqa.core.domain.result.RtiqaResult.Success) {
                    // On successful upload to Remote, clear local queued item
                    syncDao.deleteSyncItem(item.id)
                } else {
                    Log.w(TAG, "Failed to push sync payload for item ${item.id}")
                }
            }

            // Save timestamp of last successful sync in DataStore Preferences
            val currentTimestamp = System.currentTimeMillis()
            OfflineSyncPreferences.saveLastSyncTimestamp(applicationContext, currentTimestamp)

            Log.d(TAG, "Successfully completed learning progress sync at timestamp $currentTimestamp")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during learning progress sync worker execution. Retrying...", e)
            Result.retry()
        }
    }
}
