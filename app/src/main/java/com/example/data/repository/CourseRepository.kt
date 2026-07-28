package com.example.data.repository

import com.example.data.local.dao.CourseDao
import com.example.data.local.dao.LessonDao
import com.example.data.local.dao.SyncQueueDao
import com.example.data.local.entity.CourseEntity
import com.example.data.local.entity.LessonEntity
import com.example.data.local.entity.SyncQueueEntity
import com.example.domain.model.Course
import com.example.domain.model.Lesson
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
        
        // Recalculate course progress
        val lessons = lessonDao.getLessonById(lessonId)
        val course = courseDao.getCourseById(courseId)
        if (course != null) {
            val total = course.totalLessons.coerceAtLeast(1)
            val completedCount = if (isCompleted) 1 else 0 // simplified or query count
            val newProgress = (course.progressPercent + (if (isCompleted) 0.25f else -0.25f)).coerceIn(0f, 1f)
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

    suspend fun toggleLessonDownload(lessonId: String, isDownloaded: Boolean) {
        lessonDao.updateLessonDownload(lessonId, isDownloaded)
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
        isDownloaded = isDownloaded
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
