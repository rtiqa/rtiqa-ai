package com.rtiqa.core.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.database.RtiqaDatabase
import com.rtiqa.core.database.entity.SyncQueueEntity
import com.rtiqa.core.logging.RtiqaLog
import com.rtiqa.core.network.RetrofitNetworkClient
import com.rtiqa.core.network.api.NetworkSyncPayloadDto
import com.rtiqa.core.network.api.RtiqaApiService
import com.rtiqa.core.security.EncryptedSecurityManager
import com.rtiqa.core.network.session.RestSessionStoreImpl
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager CoroutineWorker for executing offline synchronization.
 */
class OfflineSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val tag = "OfflineSyncWorker"

    override suspend fun doWork(): Result {
        RtiqaLog.i(tag, "Executing background offline synchronization task...")

        val context = applicationContext
        val database = RtiqaDatabase.getInstance(context)
        val syncDao = database.syncDao()
        val securityManager = EncryptedSecurityManager(context)
        val okHttpClient = RetrofitNetworkClient.createOkHttpClient(securityManager)
        val apiService = RetrofitNetworkClient.createApiService(okHttpClient)
        val preferencesDataStore = RtiqaPreferencesDataStore(context)
        val sessionStore = RestSessionStoreImpl(securityManager)

        return try {
            val startSessionId = sessionStore.getSessionId()
            val userId = securityManager.getEncryptedString("user_id")
            // Not checking organizationId explicitly here as it's not strictly required in standard login unless scoped

            if (startSessionId.isNullOrBlank() || userId.isNullOrBlank()) {
                RtiqaLog.w(tag, "Stopping sync gracefully: No active session or user")
                return Result.success()
            }

            val pendingItems = syncDao.getPendingSyncItemsList(userId, startSessionId)
            if (pendingItems.isEmpty()) {
                RtiqaLog.i(tag, "No pending offline items to sync.")
                return Result.success()
            }

            // Race condition guard: verify session hasn't changed right before preparing payload
            if (sessionStore.getSessionId() != startSessionId) {
                RtiqaLog.w(tag, "Stopping sync gracefully: Session changed during execution")
                return Result.success()
            }

            val payload = NetworkSyncPayloadDto(
                userId = userId,
                progressUpdates = pendingItems.map { item ->
                    mapOf("id" to item.id, "type" to item.actionType, "payload" to item.payloadJson)
                },
                lastSyncedTimestamp = System.currentTimeMillis()
            )

            val response = apiService.syncOfflineData(payload)
            if (response.isSuccessful && response.body()?.success == true) {
                // Clear ONLY successfully synced items
                pendingItems.forEach { syncDao.deleteSyncItem(it.id) }
                preferencesDataStore.updateLastSyncTimestamp(System.currentTimeMillis())
                RtiqaLog.i(tag, "Successfully synced ${pendingItems.size} offline actions.")
                Result.success()
            } else {
                val code = response.code()
                RtiqaLog.w(tag, "Offline sync failed on server: $code ${response.message()}")
                if (code == 401 || code == 403) {
                    RtiqaLog.e(tag, "Authentication failed during sync, stopping retries.")
                    Result.failure()
                } else {
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            RtiqaLog.e(tag, "Exception during offline background sync", e)
            Result.retry()
        }
    }

    companion object {
        private const val PERIODIC_WORK_NAME = "rtiqa_periodic_sync"
        private const val IMMEDIATE_WORK_NAME = "rtiqa_immediate_sync"

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncWorkRequest = PeriodicWorkRequestBuilder<OfflineSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
            )
        }

        fun triggerImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = OneTimeWorkRequestBuilder<OfflineSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncWorkRequest
            )
        }
    }
}
