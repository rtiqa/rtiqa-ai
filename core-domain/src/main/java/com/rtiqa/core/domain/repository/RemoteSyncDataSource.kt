package com.rtiqa.core.domain.repository

import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.result.RtiqaResult

/**
 * Abstraction for remote synchronization operations.
 * Allows the application to sync local data with a remote backend (Firebase, Supabase, etc.)
 * without being tightly coupled to a specific implementation.
 */
interface RemoteSyncDataSource {
    /**
     * Checks whether the remote sync service is active and accessible.
     */
    fun isAvailable(): Boolean

    /**
     * Uploads or merges the user profile into the remote backend.
     */
    suspend fun syncUserProfileToCloud(profile: UserProfile): RtiqaResult<Unit>

    /**
     * Downloads the latest user profile from the remote backend.
     * Note: Returns a remote representation of UserProfile.
     */
    suspend fun fetchUserProfileFromCloud(userId: String): RtiqaResult<UserProfile?>

    /**
     * Uploads course completion or progress details.
     */
    suspend fun syncCourseProgressToCloud(
        userId: String,
        courseId: String,
        progressPercent: Float,
        completedLessonsCount: Int
    ): RtiqaResult<Unit>

    /**
     * Uploads quiz results log.
     */
    suspend fun syncQuizResultToCloud(
        userId: String,
        quizId: String,
        score: Int,
        totalQuestions: Int
    ): RtiqaResult<Unit>

    /**
     * Uploads user app settings.
     */
    suspend fun syncUserSettingsToCloud(
        userId: String,
        darkTheme: Boolean,
        notificationsEnabled: Boolean,
        offlineMode: Boolean
    ): RtiqaResult<Unit>
    
    /**
     * Pushes a generic JSON payload. Used by background sync queue.
     */
    suspend fun pushSyncPayload(
        collection: String,
        documentId: String,
        payload: Map<String, Any>
    ): RtiqaResult<Unit>
}
