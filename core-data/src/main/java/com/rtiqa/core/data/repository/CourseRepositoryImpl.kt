package com.rtiqa.core.data.repository

import com.rtiqa.core.domain.repository.RemoteSyncDataSource
import com.rtiqa.core.data.mapper.toDomain
import com.rtiqa.core.database.dao.CourseDao
import com.rtiqa.core.database.dao.LessonDao
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.core.domain.model.PageRequest
import com.rtiqa.core.domain.model.PagedData
import com.rtiqa.core.domain.repository.CourseRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.rtiqa.core.data.mapper.toEntity

import com.rtiqa.core.database.entity.CourseEntity
import com.rtiqa.core.database.entity.LessonEntity
import com.rtiqa.core.network.api.LessonProgressRequestDto
import com.rtiqa.core.network.api.NetworkCourseDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CourseRepositoryImpl(
    private val courseDao: CourseDao,
    private val lessonDao: LessonDao,
    private val apiService: com.rtiqa.core.network.api.RtiqaApiService? = null,
    private val remoteSyncDataSource: RemoteSyncDataSource? = null,
    private val currentUserIdProvider: (suspend () -> String?)? = null,
    private val offlineSyncManager: com.rtiqa.core.domain.repository.OfflineSyncContract? = null
) : CourseRepositoryContract {

    private suspend fun saveNetworkCoursesToDatabase(dtos: List<NetworkCourseDto>) {
        val existingCourses = courseDao.getAllCoursesList().associateBy { it.id }
        val entities = dtos.map { dto ->
            val existing = existingCourses[dto.id]
            CourseEntity(
                id = dto.id,
                title = dto.title,
                description = dto.description,
                category = dto.category,
                totalLessons = dto.totalModules,
                durationMinutes = existing?.durationMinutes ?: 30,
                iconUrl = existing?.iconUrl,
                isDownloaded = existing?.isDownloaded ?: false,
                progressPercent = dto.progressPercent,
                isEnrolled = existing?.isEnrolled ?: false,
                isBookmarked = existing?.isBookmarked ?: false,
                schoolId = existing?.schoolId
            )
        }
        courseDao.insertCourses(entities)
    }

    private suspend fun markLessonCompletedLocally(lessonId: String) {
        val lesson = lessonDao.getLessonById(lessonId)
        if (lesson != null) {
            lessonDao.insertLesson(lesson.copy(isCompleted = true))
        }
    }

    private fun refreshRemoteCourses(category: String? = null) {
        val service = apiService ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val catParam = if (category.isNullOrBlank() || category == "الكل") null else category
                val response = service.getCourses(catParam)
                if (response.isSuccessful) {
                    saveNetworkCoursesToDatabase(response.body().orEmpty())
                }
            } catch (e: Exception) {
                // Ignore network error to keep local offline cache
            }
        }
    }

    private fun refreshRemoteCourse(courseId: String) {
        val service = apiService ?: return
        if (courseId.isBlank()) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getCourse(courseId)
                if (response.isSuccessful) {
                    response.body()?.let { dto ->
                        saveNetworkCoursesToDatabase(listOf(dto))
                    }
                }
            } catch (e: Exception) {
                // Ignore network error to keep local offline cache
            }
        }
    }

    private fun refreshRemoteLessons(courseId: String) {
        val service = apiService ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getCourseLessons(courseId)
                if (response.isSuccessful) {
                    val dtos = response.body().orEmpty()
                    val existingLessons = lessonDao.getLessonsForCourseList(courseId).associateBy { it.id }
                    val entities = dtos.map { dto ->
                        val existing = existingLessons[dto.id]
                        LessonEntity(
                            id = dto.id,
                            courseId = dto.courseId,
                            title = dto.title,
                            content = dto.content,
                            order = dto.moduleOrder,
                            isCompleted = dto.isCompleted || (existing?.isCompleted ?: false),
                            audioUrl = dto.audioUrl?.takeIf { it.isNotBlank() } ?: existing?.audioUrl,
                            schoolId = existing?.schoolId
                        )
                    }
                    lessonDao.insertLessons(entities)
                }
            } catch (e: Exception) {
                // Ignore network error to keep local offline cache
            }
        }
    }

    private fun refreshRemoteLesson(courseId: String? = null, lessonId: String) {
        val service = apiService ?: return
        if (lessonId.isBlank()) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resolvedCourseId = courseId
                    ?: lessonDao.getLessonById(lessonId)?.courseId
                    ?: findCourseIdForLesson(lessonId)
                if (resolvedCourseId.isNullOrBlank()) return@launch

                val response = service.getLesson(courseId = resolvedCourseId, lessonId = lessonId)
                if (response.isSuccessful) {
                    val dto = response.body() ?: return@launch
                    val existing = lessonDao.getLessonById(lessonId)
                    val entity = LessonEntity(
                        id = dto.id,
                        courseId = dto.courseId,
                        title = dto.title,
                        content = dto.content,
                        order = dto.moduleOrder,
                        isCompleted = dto.isCompleted || (existing?.isCompleted ?: false),
                        audioUrl = dto.audioUrl?.takeIf { it.isNotBlank() } ?: existing?.audioUrl,
                        schoolId = existing?.schoolId
                    )
                    lessonDao.insertLesson(entity)
                }
            } catch (e: Exception) {
                // Ignore network error to keep local offline cache
            }
        }
    }

    private suspend fun findCourseIdForLesson(lessonId: String): String? {
        val courses = courseDao.getAllCoursesList()
        for (course in courses) {
            try {
                val resp = apiService?.getLesson(courseId = course.id, lessonId = lessonId)
                if (resp != null && resp.isSuccessful) {
                    val dto = resp.body()
                    if (dto != null) {
                        val existing = lessonDao.getLessonById(lessonId)
                        val entity = LessonEntity(
                            id = dto.id,
                            courseId = dto.courseId,
                            title = dto.title,
                            content = dto.content,
                            order = dto.moduleOrder,
                            isCompleted = dto.isCompleted || (existing?.isCompleted ?: false),
                            audioUrl = dto.audioUrl?.takeIf { it.isNotBlank() } ?: existing?.audioUrl,
                            schoolId = existing?.schoolId
                        )
                        lessonDao.insertLesson(entity)
                        return course.id
                    }
                }
            } catch (_: Exception) {}
        }
        return null
    }

    override fun getCourses(): Flow<List<Course>> {
        refreshRemoteCourses()
        return courseDao.getAllCourses().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCoursesForSchool(schoolId: String): Flow<List<Course>> {
        return courseDao.getCoursesForSchool(schoolId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCourseById(courseId: String): Flow<Course?> {
        refreshRemoteCourse(courseId)
        return courseDao.getCourseById(courseId).map { it?.toDomain() }
    }

    override fun getLessonsForCourse(courseId: String): Flow<List<Lesson>> {
        refreshRemoteLessons(courseId)
        return lessonDao.getLessonsForCourse(courseId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLessonById(lessonId: String): Flow<Lesson?> {
        refreshRemoteLesson(courseId = null, lessonId = lessonId)
        return lessonDao.observeLessonById(lessonId).map { it?.toDomain() }
    }

    fun getLessonById(courseId: String, lessonId: String): Flow<Lesson?> {
        refreshRemoteLesson(courseId = courseId, lessonId = lessonId)
        return lessonDao.observeLessonById(lessonId).map { it?.toDomain() }
    }

    override fun getNextLesson(courseId: String, currentLessonId: String): Flow<Lesson?> {
        return lessonDao.getNextLessonEntity(courseId, currentLessonId).map { it?.toDomain() }
    }

    override fun getPagedCourses(request: PageRequest): Flow<PagedData<Course>> {
        refreshRemoteCourses(request.filterCategory)
        val filterCat = request.filterCategory
        val query = request.searchQuery
        return courseDao.getAllCourses().map { entities ->
            val domainList = entities.map { it.toDomain() }
                .filter { course ->
                    filterCat == null ||
                    filterCat.isBlank() ||
                    filterCat.equals("الكل", ignoreCase = true) ||
                    course.category.contains(filterCat, ignoreCase = true) ||
                    (filterCat.equals("AI", true) && (course.category.contains("ذكاء", true) || course.category.contains("AI", true))) ||
                    (filterCat.equals("Programming", true) && (course.category.contains("برمج", true) || course.category.contains("كوتلن", true))) ||
                    (filterCat.equals("ComputerScience", true) && (course.category.contains("حاسوب", true) || course.category.contains("حاسب", true) || course.category.contains("Computer", true))) ||
                    (filterCat.equals("Networking", true) && (course.category.contains("شبكات", true) || course.category.contains("Network", true))) ||
                    (filterCat.equals("Security", true) && (course.category.contains("أمن", true) || course.category.contains("Cyber", true)))
                }
                .filter { course ->
                    query == null ||
                    query.isBlank() ||
                    course.title.contains(query, ignoreCase = true) ||
                    course.description.contains(query, ignoreCase = true)
                }
            
            val totalItems = domainList.size
            val pageSize = request.pageSize.coerceAtLeast(1)
            val totalPages = (totalItems + pageSize - 1) / pageSize
            val startIndex = ((request.page - 1) * pageSize).coerceAtLeast(0)
            val pagedItems = if (startIndex < totalItems) {
                domainList.subList(startIndex, (startIndex + pageSize).coerceAtMost(totalItems))
            } else emptyList()

            PagedData(
                items = pagedItems,
                page = request.page,
                totalPages = totalPages,
                totalItems = totalItems,
                hasNextPage = request.page < totalPages
            )
        }
    }

    override suspend fun searchCourses(query: String): List<Course> {
        return courseDao.getAllCoursesList().map { it.toDomain() }
            .filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
    }

    override suspend fun markLessonCompleted(lessonId: String, courseId: String): RtiqaResult<Unit> {
        return try {
            if (apiService != null) {
                val response = apiService.completeLesson(courseId = courseId, lessonId = lessonId)
                if (!response.isSuccessful) {
                    val code = response.code()
                    val errorMsg = response.errorBody()?.string() ?: response.message()
                    return when (code) {
                        401 -> RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.AuthError("Unauthorized: $errorMsg", errorCode = "401"))
                        403 -> RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.AuthError("Forbidden: $errorMsg", errorCode = "403"))
                        404 -> RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.NetworkError("Lesson or course not found: $errorMsg", statusCode = 404))
                        else -> RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.NetworkError("HTTP $code: $errorMsg", statusCode = code))
                    }
                }

                val completion = response.body()
                if (completion != null) {
                    markLessonCompletedLocally(lessonId)
                    val normalizedProgress = if (completion.courseProgressPercent > 1f) {
                        completion.courseProgressPercent / 100f
                    } else {
                        completion.courseProgressPercent
                    }
                    val existingCourse = courseDao.getAllCoursesList().find { it.id == courseId }
                    val finalProgress = maxOf(existingCourse?.progressPercent ?: 0f, normalizedProgress)
                    courseDao.updateCourseProgress(courseId, finalProgress)

                    // Trigger background refresh to sync PostgreSQL state across caches
                    refreshRemoteCourses()
                    refreshRemoteLessons(courseId)
                }
                RtiqaResult.Success(Unit)
            } else {
                markLessonCompletedLocally(lessonId)

                val lessons = lessonDao.getLessonsForCourseList(courseId)
                val completedCount = lessons.count { it.isCompleted }
                val totalCount = lessons.size.coerceAtLeast(1)
                val progressPercent = completedCount.toFloat() / totalCount.toFloat()

                val existingCourse = courseDao.getAllCoursesList().find { it.id == courseId }
                val finalProgress = maxOf(existingCourse?.progressPercent ?: 0f, progressPercent)
                courseDao.updateCourseProgress(courseId, finalProgress)

                val userId = currentUserIdProvider?.invoke()
                if (userId != null) {
                    remoteSyncDataSource?.syncCourseProgressToCloud(
                        userId = userId,
                        courseId = courseId,
                        progressPercent = finalProgress,
                        completedLessonsCount = completedCount
                    )
                }

                RtiqaResult.Success(Unit)
            }
        } catch (e: Exception) {
            RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.NetworkError("Failed to mark lesson complete: ${e.message}", cause = e))
        }
    }

    override suspend fun updateLessonProgress(
        lessonId: String,
        courseId: String,
        progressPercent: Float
    ): RtiqaResult<Unit> {
        return try {
            val normalizedProgress = if (progressPercent > 1f) {
                (progressPercent / 100f).coerceIn(0f, 1f)
            } else {
                progressPercent.coerceIn(0f, 1f)
            }
            val score = (normalizedProgress * 100).toInt().coerceIn(0, 100)
            val existingLesson = lessonDao.getLessonById(lessonId)
                ?: return RtiqaResult.Error(
                    com.rtiqa.core.domain.error.RtiqaError.DatabaseError("Lesson is not available locally.")
                )
            val shouldBeCompleted = normalizedProgress >= 1.0f || existingLesson.isCompleted

            var remoteSuccess = false
            if (apiService != null) {
                try {
                    val requestDto = LessonProgressRequestDto(
                        completed = shouldBeCompleted,
                        score = score
                    )
                    val response = apiService.updateLessonProgress(
                        courseId = courseId,
                        lessonId = lessonId,
                        request = requestDto
                    )
                    if (response.isSuccessful) {
                        val completion = response.body()
                        if (completion != null) {
                            val finalCompleted = existingLesson.isCompleted || shouldBeCompleted
                            lessonDao.updateLessonCompletion(lessonId, finalCompleted)
                            val serverProgress = if (completion.courseProgressPercent > 1f) {
                                completion.courseProgressPercent / 100f
                            } else {
                                completion.courseProgressPercent
                            }
                            val existingCourse = courseDao.getAllCoursesList().find { it.id == courseId }
                            val finalProgress = maxOf(existingCourse?.progressPercent ?: 0f, serverProgress)
                            courseDao.updateCourseProgress(courseId, finalProgress)

                            refreshRemoteCourses()
                            refreshRemoteLessons(courseId)
                            remoteSuccess = true
                        }
                    }
                } catch (e: Exception) {
                    // Network failure: fallback to local Room update and offline action queue
                }
            }

            if (!remoteSuccess) {
                // Local-first fallback when offline or when network call fails
                val finalCompleted = existingLesson.isCompleted || shouldBeCompleted
                lessonDao.updateLessonCompletion(lessonId, finalCompleted)

                // Recalculate course progress from completed lessons without overwriting with individual lesson progress
                val lessons = lessonDao.getLessonsForCourseList(courseId)
                val completedCount = lessons.count { it.isCompleted }
                val totalCount = lessons.size.coerceAtLeast(1)
                val calculatedProgress = completedCount.toFloat() / totalCount.toFloat()
                val existingCourse = courseDao.getAllCoursesList().find { it.id == courseId }
                val finalProgress = maxOf(existingCourse?.progressPercent ?: 0f, calculatedProgress)
                courseDao.updateCourseProgress(courseId, finalProgress)

                offlineSyncManager?.enqueueOfflineAction(
                    actionType = "LESSON_PROGRESS_UPDATE",
                    payloadJson = "{\"lessonId\":\"$lessonId\",\"courseId\":\"$courseId\",\"progress\":$progressPercent}"
                )
            }

            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.DatabaseError("Failed to update lesson progress", e))
        }
    }

    override suspend fun saveCourse(course: Course): RtiqaResult<Unit> {
        return try {
            courseDao.insertCourse(course.toEntity())
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.DatabaseError("Failed to save course", e))
        }
    }

    override suspend fun deleteCourse(courseId: String): RtiqaResult<Unit> {
        return try {
            courseDao.deleteCourseById(courseId)
            lessonDao.deleteLessonsForCourse(courseId)
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.DatabaseError("Failed to delete course", e))
        }
    }

    override suspend fun saveLesson(lesson: Lesson): RtiqaResult<Unit> {
        return try {
            lessonDao.insertLesson(lesson.toEntity())
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.DatabaseError("Failed to save lesson", e))
        }
    }

    override suspend fun enrollInCourse(courseId: String): RtiqaResult<Unit> {
        return try {
            courseDao.updateEnrollmentStatus(courseId, true)
            val userId = currentUserIdProvider?.invoke()
            if (userId != null) {
                remoteSyncDataSource?.syncCourseProgressToCloud(
                    userId = userId,
                    courseId = courseId,
                    progressPercent = 0f,
                    completedLessonsCount = 0
                )
            }
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.DatabaseError("Failed to enroll in course", e))
        }
    }

    override suspend fun toggleBookmark(courseId: String, isBookmarked: Boolean): RtiqaResult<Unit> {
        return try {
            courseDao.updateBookmarkStatus(courseId, isBookmarked)
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.DatabaseError("Failed to update bookmark", e))
        }
    }

    override suspend fun toggleCourseDownload(courseId: String, isDownloaded: Boolean): RtiqaResult<Unit> {
        return try {
            courseDao.updateDownloadStatus(courseId, isDownloaded)
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.DatabaseError("Failed to update download status", e))
        }
    }

    override suspend fun syncCourses(): RtiqaResult<Unit> {
        return try {
            val userId = currentUserIdProvider?.invoke()
            if (userId != null) {
                remoteSyncDataSource?.fetchUserProfileFromCloud(userId)
            }
            if (apiService != null) {
                val response = apiService.getCourses()
                if (response.isSuccessful) {
                    saveNetworkCoursesToDatabase(response.body().orEmpty())
                    return RtiqaResult.Success(Unit)
                } else {
                    return RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.NetworkError("HTTP ${response.code()}: ${response.message()}", statusCode = response.code()))
                }
            }
            offlineSyncManager?.syncRemoteCourses() ?: RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.SyncError("Failed to sync courses with cloud", e))
        }
    }
}
