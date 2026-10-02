package com.rtiqa.core.data.repository

import com.rtiqa.core.database.dao.CourseDao
import com.rtiqa.core.database.dao.LessonDao
import com.rtiqa.core.database.entity.CourseEntity
import com.rtiqa.core.database.entity.LessonEntity
import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.repository.DownloadManagerContract
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.network.api.RtiqaApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.ConcurrentHashMap

/**
 * Manager handling offline content downloading and progress tracking.
 */
class DownloadManagerImpl(
    private val courseDao: CourseDao,
    private val lessonDao: LessonDao,
    private val apiService: RtiqaApiService
) : DownloadManagerContract {

    private val progressFlows = ConcurrentHashMap<String, MutableStateFlow<Float>>()

    override fun observeCourseDownloadProgress(courseId: String): Flow<Float> {
        return progressFlows.getOrPut(courseId) { MutableStateFlow(0f) }.asStateFlow()
    }

    override suspend fun downloadCourse(courseId: String): RtiqaResult<Unit> {
        val progressState = progressFlows.getOrPut(courseId) { MutableStateFlow(0f) }
        progressState.value = 0f
        return try {
            val existingCourse = courseDao.getCourseById(courseId).firstOrNull()
            courseDao.updateDownloadStatus(courseId, false)

            val courseResponse = apiService.getCourse(courseId)
            if (!courseResponse.isSuccessful) {
                return RtiqaResult.Error(
                    RtiqaError.NetworkError(
                        message = "Failed to fetch course for offline use.",
                        statusCode = courseResponse.code()
                    )
                )
            }
            val remoteCourse = courseResponse.body()
                ?: return RtiqaResult.Error(RtiqaError.NetworkError("Course response was empty."))
            if (remoteCourse.id != courseId) {
                return RtiqaResult.Error(RtiqaError.NetworkError("Course response did not match the requested course."))
            }

            val lessonsResponse = apiService.getCourseLessons(courseId)
            if (!lessonsResponse.isSuccessful) {
                return RtiqaResult.Error(
                    RtiqaError.NetworkError(
                        message = "Failed to fetch course lessons for offline use.",
                        statusCode = lessonsResponse.code()
                    )
                )
            }
            val remoteLessons = lessonsResponse.body()
                ?: return RtiqaResult.Error(RtiqaError.NetworkError("Course lessons response was empty."))
            if (remoteCourse.totalModules > 0 && remoteLessons.isEmpty()) {
                return RtiqaResult.Error(RtiqaError.NetworkError("Course lesson package is incomplete."))
            }
            if (remoteLessons.any { it.courseId != courseId }) {
                return RtiqaResult.Error(RtiqaError.NetworkError("Course lesson package contains invalid content."))
            }

            val existingLessons = lessonDao.getLessonsForCourseList(courseId).associateBy { it.id }
            val cachedCourse = CourseEntity(
                id = remoteCourse.id,
                title = remoteCourse.title,
                description = remoteCourse.description,
                category = remoteCourse.category,
                totalLessons = remoteCourse.totalModules,
                durationMinutes = existingCourse?.durationMinutes
                    ?: remoteLessons.sumOf { it.estimatedMinutes },
                iconUrl = existingCourse?.iconUrl,
                isDownloaded = false,
                progressPercent = existingCourse?.progressPercent ?: remoteCourse.progressPercent,
                isEnrolled = existingCourse?.isEnrolled ?: false,
                isBookmarked = existingCourse?.isBookmarked ?: false,
                schoolId = existingCourse?.schoolId
            )
            val cachedLessons = remoteLessons.map { remote ->
                LessonEntity(
                    id = remote.id,
                    courseId = remote.courseId,
                    title = remote.title,
                    content = remote.content,
                    order = remote.moduleOrder,
                    isCompleted = remote.isCompleted || existingLessons[remote.id]?.isCompleted == true,
                    audioUrl = remote.audioUrl,
                    schoolId = existingLessons[remote.id]?.schoolId ?: existingCourse?.schoolId
                )
            }

            courseDao.insertCourse(cachedCourse)
            lessonDao.insertLessons(cachedLessons)
            courseDao.updateDownloadStatus(courseId, true)
            progressState.value = 1f
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            progressState.value = 0f
            runCatching { courseDao.updateDownloadStatus(courseId, false) }
            RtiqaResult.Error(RtiqaError.NetworkError("Failed to cache course for offline use.", cause = e))
        }
    }

    override suspend fun deleteCourseDownload(courseId: String): RtiqaResult<Unit> {
        return try {
            progressFlows[courseId]?.value = 0f
            courseDao.updateDownloadStatus(courseId, false)
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.DatabaseError("Failed to remove downloaded course package.", e))
        }
    }
}
