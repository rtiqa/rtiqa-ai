package com.rtiqa.core.data.repository

import com.rtiqa.core.database.dao.CourseDao
import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.repository.DownloadManagerContract
import com.rtiqa.core.domain.result.RtiqaResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.ConcurrentHashMap

/**
 * Manager handling offline content downloading and progress tracking.
 */
class DownloadManagerImpl(
    private val courseDao: CourseDao
) : DownloadManagerContract {

    private val progressFlows = ConcurrentHashMap<String, MutableStateFlow<Float>>()

    override fun observeCourseDownloadProgress(courseId: String): Flow<Float> {
        return progressFlows.getOrPut(courseId) { MutableStateFlow(0f) }.asStateFlow()
    }

    override suspend fun downloadCourse(courseId: String): RtiqaResult<Unit> {
        return try {
            val progressState = progressFlows.getOrPut(courseId) { MutableStateFlow(0f) }
            
            // Simulate chunk download progress
            for (progress in 1..10) {
                delay(100)
                progressState.value = progress / 10f
            }

            val entity = courseDao.getCourseById(courseId).firstOrNull()
            if (entity != null) {
                courseDao.insertCourses(listOf(entity.copy(isDownloaded = true)))
            }
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.DatabaseError("Failed to download course package.", e))
        }
    }

    override suspend fun deleteCourseDownload(courseId: String): RtiqaResult<Unit> {
        return try {
            progressFlows[courseId]?.value = 0f
            val entity = courseDao.getCourseById(courseId).firstOrNull()
            if (entity != null) {
                courseDao.insertCourses(listOf(entity.copy(isDownloaded = false)))
            }
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.DatabaseError("Failed to remove downloaded course package.", e))
        }
    }
}
