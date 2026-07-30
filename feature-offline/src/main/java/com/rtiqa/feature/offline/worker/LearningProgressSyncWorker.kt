package com.rtiqa.feature.offline.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.rtiqa.core.database.RtiqaDatabase
import com.rtiqa.feature.offline.data.OfflineSyncPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager CoroutineWorker for automatically syncing local learning progress with Firestore
 * when network connectivity is available.
 */
class LearningProgressSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "LearningProgressSyncWorker"
        const val WORK_NAME = "learning_progress_sync_work"
        const val FIRESTORE_COLLECTION_PROGRESS = "learning_progress"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting learning progress sync worker...")
        try {
            val db = RtiqaDatabase.getInstance(applicationContext)
            val syncDao = db.syncDao()

            // Retrieve pending sync queue items from Room DB
            val pendingSyncItems = syncDao.getPendingSyncItemsList()
            Log.d(TAG, "Found ${pendingSyncItems.size} pending items to sync.")

            val firestore = FirebaseFirestore.getInstance()

            for (item in pendingSyncItems) {
                val dataMap = mapOf(
                    "id" to item.id,
                    "actionType" to item.actionType,
                    "payloadJson" to item.payloadJson,
                    "createdAt" to item.createdAt,
                    "syncedAt" to System.currentTimeMillis()
                )

                // Push payload to Firestore collection
                val documentRef = firestore
                    .collection(FIRESTORE_COLLECTION_PROGRESS)
                    .document(item.id)

                val task = documentRef.set(dataMap)
                Tasks.await(task, 15, TimeUnit.SECONDS)

                // On successful upload to Firestore, clear local queued item
                syncDao.deleteSyncItem(item.id)
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
