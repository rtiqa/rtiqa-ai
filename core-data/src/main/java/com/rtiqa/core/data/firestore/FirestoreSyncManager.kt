package com.rtiqa.core.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.rtiqa.core.database.entity.UserProfileEntity
import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.logging.RtiqaLog
import kotlinx.coroutines.tasks.await

/**
 * Cloud Firestore synchronization service providing offline-first cloud backup and data sync
 * for user profiles, settings, course progress, and quiz results.
 */
class FirestoreSyncManager {

    private val tag = "FirestoreSyncManager"

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            RtiqaLog.w(tag, "FirebaseFirestore unavailable or google-services.json not configured: ${e.message}")
            null
        }
    }

    /**
     * Checks whether Cloud Firestore service is active and accessible.
     */
    fun isAvailable(): Boolean = firestore != null

    /**
     * Uploads or merges user profile into Firestore collection 'users/{userId}'.
     */
    suspend fun syncUserProfileToCloud(profile: UserProfile): RtiqaResult<Unit> {
        val db = firestore ?: return RtiqaResult.Error(RtiqaError.SyncError("Firestore unavailable"))
        return try {
            val userMap = hashMapOf(
                "id" to profile.id,
                "name" to profile.name,
                "email" to profile.email,
                "levelXp" to profile.levelXp,
                "streakDays" to profile.streakDays,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users")
                .document(profile.id)
                .set(userMap, SetOptions.merge())
                .await()

            RtiqaLog.i(tag, "Synced profile to Firestore for user: ${profile.id}")
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaLog.e(tag, "Failed to sync profile to Firestore", e)
            RtiqaResult.Error(RtiqaError.SyncError("Firestore user profile sync failed", e))
        }
    }

    /**
     * Downloads latest user profile from Firestore collection 'users/{userId}'.
     */
    suspend fun fetchUserProfileFromCloud(userId: String): RtiqaResult<UserProfileEntity?> {
        val db = firestore ?: return RtiqaResult.Error(RtiqaError.SyncError("Firestore unavailable"))
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .get()
                .await()

            if (snapshot.exists()) {
                val entity = UserProfileEntity(
                    id = snapshot.getString("id") ?: userId,
                    name = snapshot.getString("name") ?: "",
                    email = snapshot.getString("email") ?: "",
                    levelXp = (snapshot.getLong("levelXp") ?: 0L).toInt(),
                    streakDays = (snapshot.getLong("streakDays") ?: 0L).toInt()
                )
                RtiqaResult.Success(entity)
            } else {
                RtiqaResult.Success(null)
            }
        } catch (e: Exception) {
            RtiqaLog.e(tag, "Failed to fetch user profile from Firestore", e)
            RtiqaResult.Error(RtiqaError.SyncError("Firestore user profile fetch failed", e))
        }
    }

    /**
     * Uploads course completion or progress details into 'users/{userId}/progress/{courseId}'.
     */
    suspend fun syncCourseProgressToCloud(
        userId: String,
        courseId: String,
        progressPercent: Float,
        completedLessonsCount: Int
    ): RtiqaResult<Unit> {
        val db = firestore ?: return RtiqaResult.Error(RtiqaError.SyncError("Firestore unavailable"))
        return try {
            val progressMap = hashMapOf(
                "courseId" to courseId,
                "progressPercent" to progressPercent,
                "completedLessonsCount" to completedLessonsCount,
                "lastUpdatedTimestamp" to System.currentTimeMillis()
            )
            db.collection("users")
                .document(userId)
                .collection("progress")
                .document(courseId)
                .set(progressMap, SetOptions.merge())
                .await()

            RtiqaLog.i(tag, "Synced course $courseId progress to Firestore for user: $userId")
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaLog.e(tag, "Failed to sync course progress to Firestore", e)
            RtiqaResult.Error(RtiqaError.SyncError("Firestore course progress sync failed", e))
        }
    }

    /**
     * Uploads quiz results log to 'users/{userId}/quiz_results/{quizId}'.
     */
    suspend fun syncQuizResultToCloud(
        userId: String,
        quizId: String,
        score: Int,
        totalQuestions: Int
    ): RtiqaResult<Unit> {
        val db = firestore ?: return RtiqaResult.Error(RtiqaError.SyncError("Firestore unavailable"))
        return try {
            val quizMap = hashMapOf(
                "quizId" to quizId,
                "score" to score,
                "totalQuestions" to totalQuestions,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("users")
                .document(userId)
                .collection("quiz_results")
                .document(quizId)
                .set(quizMap, SetOptions.merge())
                .await()

            RtiqaLog.i(tag, "Synced quiz $quizId score ($score/$totalQuestions) to Firestore for user: $userId")
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaLog.e(tag, "Failed to sync quiz result to Firestore", e)
            RtiqaResult.Error(RtiqaError.SyncError("Firestore quiz result sync failed", e))
        }
    }

    /**
     * Uploads user app settings to 'users/{userId}/settings/user_settings'.
     */
    suspend fun syncUserSettingsToCloud(
        userId: String,
        darkTheme: Boolean,
        notificationsEnabled: Boolean,
        offlineMode: Boolean
    ): RtiqaResult<Unit> {
        val db = firestore ?: return RtiqaResult.Error(RtiqaError.SyncError("Firestore unavailable"))
        return try {
            val settingsMap = hashMapOf(
                "darkTheme" to darkTheme,
                "notificationsEnabled" to notificationsEnabled,
                "offlineMode" to offlineMode,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users")
                .document(userId)
                .collection("settings")
                .document("user_settings")
                .set(settingsMap, SetOptions.merge())
                .await()

            RtiqaLog.i(tag, "Synced user settings to Firestore for user: $userId")
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaLog.e(tag, "Failed to sync user settings to Firestore", e)
            RtiqaResult.Error(RtiqaError.SyncError("Firestore user settings sync failed", e))
        }
    }
}
