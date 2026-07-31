package com.rtiqa.mobile.data.repository

import com.rtiqa.mobile.data.local.dao.CourseDao
import com.rtiqa.mobile.data.local.dao.LessonDao
import com.rtiqa.mobile.data.local.dao.SyncQueueDao
import com.rtiqa.mobile.data.local.entity.CourseEntity
import com.rtiqa.mobile.data.local.entity.LessonEntity
import com.rtiqa.mobile.data.local.entity.SyncQueueEntity
import com.rtiqa.mobile.domain.model.Course
import com.rtiqa.mobile.domain.model.Lesson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class CourseRepository(
    private val courseDao: CourseDao,
    private val lessonDao: LessonDao,
    private val syncQueueDao: SyncQueueDao
) {

    val allCourses: Flow<List<Course>> = courseDao.getAllCourses().map { entities ->
        entities.map { it.toDomain() }
    }

    val bookmarkedCourses: Flow<List<Course>> = courseDao.getBookmarkedCourses().map { entities ->
        entities.map { it.toDomain() }
    }

    val downloadedCourses: Flow<List<Course>> = courseDao.getDownloadedCourses().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getLessonsForCourse(courseId: String): Flow<List<Lesson>> {
        return lessonDao.getLessonsForCourse(courseId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    val downloadedLessons: Flow<List<Lesson>> = lessonDao.getDownloadedLessons().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun toggleBookmark(courseId: String, isBookmarked: Boolean) {
        courseDao.updateBookmarkStatus(courseId, isBookmarked)
        // Enqueue offline sync item
        syncQueueDao.enqueueItem(
            SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                actionType = "TOGGLE_BOOKMARK",
                payloadJson = "{\"courseId\":\"$courseId\", \"isBookmarked\":$isBookmarked}",
                timestamp = System.currentTimeMillis(),
                status = "PENDING"
            )
        )
    }

    suspend fun toggleCourseDownload(courseId: String, isDownloaded: Boolean) {
        courseDao.updateDownloadStatus(courseId, isDownloaded)
    }

    suspend fun toggleLessonCompletion(lessonId: String, courseId: String, isCompleted: Boolean) {
        lessonDao.updateCompletion(lessonId, isCompleted)
        
        // Recalculate course progress dynamically from Room
        val totalCount = lessonDao.getTotalLessonsCount(courseId)
        if (totalCount > 0) {
            val completedCount = lessonDao.getCompletedLessonsCount(courseId)
            val newProgress = completedCount.toFloat() / totalCount.toFloat()
            courseDao.updateCourseProgress(courseId, newProgress)
        }

        syncQueueDao.enqueueItem(
            SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                actionType = "LESSON_COMPLETED",
                payloadJson = "{\"lessonId\":\"$lessonId\", \"isCompleted\":$isCompleted}",
                timestamp = System.currentTimeMillis(),
                status = "PENDING"
            )
        )
    }

    suspend fun flushSyncQueue() {
        syncQueueDao.clearSyncedItems()
    }

    suspend fun toggleLessonDownload(lessonId: String, isDownloaded: Boolean) {
        lessonDao.updateLessonDownload(lessonId, isDownloaded)
    }

    suspend fun toggleEnrollment(courseId: String, isEnrolled: Boolean) {
        courseDao.updateEnrollmentStatus(courseId, isEnrolled)
        syncQueueDao.enqueueItem(
            SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                actionType = "TOGGLE_ENROLLMENT",
                payloadJson = "{\"courseId\":\"$courseId\", \"isEnrolled\":$isEnrolled}",
                timestamp = System.currentTimeMillis(),
                status = "PENDING"
            )
        )
    }

    private fun CourseEntity.toDomain() = Course(
        id = id,
        title = title,
        titleAr = titleAr,
        category = category,
        categoryAr = categoryAr,
        description = description,
        descriptionAr = descriptionAr,
        rating = rating,
        durationMinutes = durationMinutes,
        totalLessons = totalLessons,
        enrolledCount = enrolledCount,
        imageResName = imageResName,
        level = level,
        tags = tagsCsv.split(",").map { it.trim() },
        progressPercent = progressPercent,
        isBookmarked = isBookmarked,
        isDownloaded = isDownloaded,
        isEnrolled = isEnrolled
    )

    private fun LessonEntity.toDomain() = Lesson(
        id = id,
        courseId = courseId,
        title = title,
        titleAr = titleAr,
        durationMinutes = durationMinutes,
        order = orderIndex,
        videoUrl = videoUrl,
        audioUrl = audioUrl,
        contentMarkdown = contentMarkdown,
        contentMarkdownAr = contentMarkdownAr,
        isCompleted = isCompleted,
        isDownloaded = isDownloaded
    )
}
