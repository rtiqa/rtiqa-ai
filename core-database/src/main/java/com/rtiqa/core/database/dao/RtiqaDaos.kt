package com.rtiqa.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rtiqa.core.database.entity.AiInsightEntity
import com.rtiqa.core.database.entity.CourseEntity
import com.rtiqa.core.database.entity.LessonEntity
import com.rtiqa.core.database.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE schoolId = :schoolId")
    fun getCoursesForSchool(schoolId: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses")
    suspend fun getAllCoursesList(): List<CourseEntity>

    @Query("SELECT * FROM courses WHERE id = :id")
    fun getCourseById(id: String): Flow<CourseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourseById(id: String)

    @Query("UPDATE courses SET isEnrolled = :isEnrolled WHERE id = :id")
    suspend fun updateEnrollmentStatus(id: String, isEnrolled: Boolean)

    @Query("UPDATE courses SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmarkStatus(id: String, isBookmarked: Boolean)

    @Query("UPDATE courses SET isDownloaded = :isDownloaded WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean)

    @Query("UPDATE courses SET progressPercent = :progressPercent WHERE id = :id")
    suspend fun updateCourseProgress(id: String, progressPercent: Float)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY `order` ASC")
    fun getLessonsForCourse(courseId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY `order` ASC")
    suspend fun getLessonsForCourseList(courseId: String): List<LessonEntity>

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    suspend fun getLessonById(id: String): LessonEntity?

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    fun observeLessonById(id: String): Flow<LessonEntity?>

    @Query("SELECT * FROM lessons WHERE courseId = :courseId AND `order` > (SELECT `order` FROM lessons WHERE id = :currentLessonId) ORDER BY `order` ASC LIMIT 1")
    fun getNextLessonEntity(courseId: String, currentLessonId: String): Flow<LessonEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Query("DELETE FROM lessons WHERE courseId = :courseId")
    suspend fun deleteLessonsForCourse(courseId: String)

    @Query("UPDATE lessons SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateLessonCompletion(id: String, isCompleted: Boolean)

    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId")
    suspend fun getTotalLessonsCount(courseId: String): Int

    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId AND isCompleted = 1")
    suspend fun getCompletedLessonsCount(courseId: String): Int
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profiles")
    suspend fun clearUserProfile()
}

@Dao
interface AiInsightDao {
    @Query("SELECT * FROM ai_insights ORDER BY timestamp DESC")
    fun getAllInsights(): Flow<List<AiInsightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: AiInsightEntity)
}

@Dao
interface SyncDao {
    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    fun getAllPendingSyncItems(): Flow<List<com.rtiqa.core.database.entity.SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    suspend fun getPendingSyncItemsList(): List<com.rtiqa.core.database.entity.SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncItem(item: com.rtiqa.core.database.entity.SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteSyncItem(id: String)

    @Query("DELETE FROM sync_queue")
    suspend fun clearAll()
}
