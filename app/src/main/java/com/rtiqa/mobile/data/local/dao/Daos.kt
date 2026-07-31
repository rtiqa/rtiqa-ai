package com.rtiqa.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rtiqa.mobile.data.local.entity.CourseEntity
import com.rtiqa.mobile.data.local.entity.LessonEntity
import com.rtiqa.mobile.data.local.entity.SyncQueueEntity
import com.rtiqa.mobile.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY title ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :courseId")
    suspend fun getCourseById(courseId: String): CourseEntity?

    @Query("SELECT * FROM courses WHERE isBookmarked = 1")
    fun getBookmarkedCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE isDownloaded = 1")
    fun getDownloadedCourses(): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Query("UPDATE courses SET isBookmarked = :isBookmarked WHERE id = :courseId")
    suspend fun updateBookmarkStatus(courseId: String, isBookmarked: Boolean)

    @Query("UPDATE courses SET isEnrolled = :isEnrolled WHERE id = :courseId")
    suspend fun updateEnrollmentStatus(courseId: String, isEnrolled: Boolean)

    @Query("UPDATE courses SET isDownloaded = :isDownloaded WHERE id = :courseId")
    suspend fun updateDownloadStatus(courseId: String, isDownloaded: Boolean)

    @Query("UPDATE courses SET progressPercent = :progress WHERE id = :courseId")
    suspend fun updateCourseProgress(courseId: String, progress: Float)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY orderIndex ASC")
    fun getLessonsForCourse(courseId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: String): LessonEntity?

    @Query("SELECT * FROM lessons WHERE isDownloaded = 1")
    fun getDownloadedLessons(): Flow<List<LessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Query("UPDATE lessons SET isCompleted = :isCompleted WHERE id = :lessonId")
    suspend fun updateCompletion(lessonId: String, isCompleted: Boolean)

    @Query("UPDATE lessons SET isDownloaded = :isDownloaded WHERE id = :lessonId")
    suspend fun updateLessonDownload(lessonId: String, isDownloaded: Boolean)

    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId AND isCompleted = 1")
    suspend fun getCompletedLessonsCount(courseId: String): Int

    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId")
    suspend fun getTotalLessonsCount(courseId: String): Int
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    fun getUserProfileById(id: String): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET xp = xp + :xpGained, coins = coins + :coinsGained WHERE id = :id OR :id = ''")
    suspend fun addRewards(id: String, xpGained: Int, coinsGained: Int)

    @Query("UPDATE user_profile SET language = :lang WHERE id = :id OR :id = ''")
    suspend fun updateLanguage(id: String, lang: String)

    @Query("UPDATE user_profile SET isDarkMode = :isDark WHERE id = :id OR :id = ''")
    suspend fun updateTheme(id: String, isDark: Boolean)

    @Query("UPDATE user_profile SET isOfflineAutoSyncEnabled = :enabled WHERE id = :id OR :id = ''")
    suspend fun updateOfflineAutoSync(id: String, enabled: Boolean)
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY timestamp ASC")
    fun getPendingSyncItems(): Flow<List<SyncQueueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueItem(item: SyncQueueEntity)

    @Query("UPDATE sync_queue SET status = 'SYNCED' WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM sync_queue WHERE status = 'SYNCED'")
    suspend fun clearSyncedItems()
}
