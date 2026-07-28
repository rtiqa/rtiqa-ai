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

        return try {
            val pendingItems = syncDao.getPendingSyncItemsList()
            if (pendingItems.isEmpty()) {
                RtiqaLog.i(tag, "No pending offline items to sync.")
                return Result.success()
            }

            val payload = NetworkSyncPayloadDto(
                userId = securityManager.getEncryptedString("user_id") ?: "anonymous",
                progressUpdates = pendingItems.map { item ->
                    mapOf("id" to item.id, "type" to item.actionType, "payload" to item.payloadJson)
                },
                lastSyncedTimestamp = System.currentTimeMillis()
            )

            val response = apiService.syncOfflineData(payload)
            if (response.isSuccessful && response.body()?.success == true) {
                // Clear successfully synced items
                pendingItems.forEach { syncDao.deleteSyncItem(it.id) }
                preferencesDataStore.updateLastSyncTimestamp(System.currentTimeMillis())
                RtiqaLog.i(tag, "Successfully synced ${pendingItems.size} offline actions.")
                Result.success()
            } else {
                RtiqaLog.w(tag, "Offline sync failed on server: ${response.code()} ${response.message()}")
                Result.retry()
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
