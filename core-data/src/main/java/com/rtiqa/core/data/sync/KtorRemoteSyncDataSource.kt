package com.rtiqa.core.data.sync

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.repository.RemoteSyncDataSource
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.logging.RtiqaLog
import com.rtiqa.core.network.api.RtiqaApiService
import com.rtiqa.core.network.api.NetworkSyncPayloadDto

/**
 * Ktor REST API backed synchronization implementation replacing Firestore.
 */
class KtorRemoteSyncDataSource(
    private val apiService: RtiqaApiService
) : RemoteSyncDataSource {
    private val tag = "KtorRemoteSyncDataSource"

    override fun isAvailable(): Boolean = true

    override suspend fun syncUserProfileToCloud(profile: UserProfile): RtiqaResult<Unit> {
        return try {
            val payload = NetworkSyncPayloadDto(
                userId = profile.id,
                progressUpdates = listOf(mapOf(
                    "type" to "user_profile",
                    "name" to profile.name,
                    "email" to profile.email,
                    "levelXp" to profile.levelXp.toString(),
                    "streakDays" to profile.streakDays.toString()
                )),
                lastSyncedTimestamp = System.currentTimeMillis()
            )
            val response = apiService.syncOfflineData(payload)
            if (response.isSuccessful && response.body()?.success == true) {
                RtiqaResult.Success(Unit)
            } else {
                RtiqaResult.Error(RtiqaError.SyncError("Failed to sync profile to backend"))
            }
        } catch (e: Exception) {
            RtiqaLog.e(tag, "Failed to sync user profile", e)
            RtiqaResult.Error(RtiqaError.SyncError("User profile sync failed", e))
        }
    }

    override suspend fun fetchUserProfileFromCloud(userId: String): RtiqaResult<UserProfile?> {
        return try {
            val response = apiService.getUserProfile()
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val profile = UserProfile(
                    id = dto.id,
                    name = dto.name,
                    email = dto.email,
                    levelXp = dto.totalXp,
                    streakDays = dto.streakCount
                )
                RtiqaResult.Success(profile)
            } else {
                RtiqaResult.Success(null)
            }
        } catch (e: Exception) {
            RtiqaLog.e(tag, "Failed to fetch user profile", e)
            RtiqaResult.Error(RtiqaError.SyncError("User profile fetch failed", e))
        }
    }

    override suspend fun syncCourseProgressToCloud(
        userId: String,
        courseId: String,
        progressPercent: Float,
        completedLessonsCount: Int
    ): RtiqaResult<Unit> {
        return try {
            val payload = NetworkSyncPayloadDto(
                userId = userId,
                progressUpdates = listOf(mapOf(
                    "type" to "course_progress",
                    "courseId" to courseId,
                    "progressPercent" to progressPercent.toString(),
                    "completedLessonsCount" to completedLessonsCount.toString()
                )),
                lastSyncedTimestamp = System.currentTimeMillis()
            )
            val response = apiService.syncOfflineData(payload)
            if (response.isSuccessful) RtiqaResult.Success(Unit)
            else RtiqaResult.Error(RtiqaError.SyncError("Course progress sync failed"))
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.SyncError("Course progress sync exception", e))
        }
    }

    override suspend fun syncQuizResultToCloud(
        userId: String,
        quizId: String,
        score: Int,
        totalQuestions: Int
    ): RtiqaResult<Unit> {
        return try {
            val payload = NetworkSyncPayloadDto(
                userId = userId,
                progressUpdates = listOf(mapOf(
                    "type" to "quiz_result",
                    "quizId" to quizId,
                    "score" to score.toString(),
                    "totalQuestions" to totalQuestions.toString()
                )),
                lastSyncedTimestamp = System.currentTimeMillis()
            )
            val response = apiService.syncOfflineData(payload)
            if (response.isSuccessful) RtiqaResult.Success(Unit)
            else RtiqaResult.Error(RtiqaError.SyncError("Quiz result sync failed"))
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.SyncError("Quiz result sync exception", e))
        }
    }

    override suspend fun syncUserSettingsToCloud(
        userId: String,
        darkTheme: Boolean,
        notificationsEnabled: Boolean,
        offlineMode: Boolean
    ): RtiqaResult<Unit> {
        return try {
            val payload = NetworkSyncPayloadDto(
                userId = userId,
                progressUpdates = listOf(mapOf(
                    "type" to "user_settings",
                    "darkTheme" to darkTheme.toString(),
                    "notificationsEnabled" to notificationsEnabled.toString(),
                    "offlineMode" to offlineMode.toString()
                )),
                lastSyncedTimestamp = System.currentTimeMillis()
            )
            val response = apiService.syncOfflineData(payload)
            if (response.isSuccessful) RtiqaResult.Success(Unit)
            else RtiqaResult.Error(RtiqaError.SyncError("User settings sync failed"))
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.SyncError("User settings sync exception", e))
        }
    }

    override suspend fun pushSyncPayload(
        collection: String,
        documentId: String,
        payload: Map<String, Any>
    ): RtiqaResult<Unit> {
        return try {
            val stringMap = payload.mapValues { it.value.toString() }
            val netPayload = NetworkSyncPayloadDto(
                userId = documentId,
                progressUpdates = listOf(mapOf("collection" to collection) + stringMap),
                lastSyncedTimestamp = System.currentTimeMillis()
            )
            val response = apiService.syncOfflineData(netPayload)
            if (response.isSuccessful) RtiqaResult.Success(Unit)
            else RtiqaResult.Error(RtiqaError.SyncError("Push sync payload failed"))
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.SyncError("Push sync payload exception", e))
        }
    }
}
