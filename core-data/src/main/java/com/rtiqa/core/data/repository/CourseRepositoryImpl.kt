package com.rtiqa.core.data.repository

import com.rtiqa.core.data.firestore.FirestoreSyncManager
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CourseRepositoryImpl(
    private val courseDao: CourseDao,
    private val lessonDao: LessonDao,
    private val firestoreSyncManager: FirestoreSyncManager? = null,
    private val currentUserIdProvider: (suspend () -> String?)? = null,
    private val offlineSyncManager: com.rtiqa.core.domain.repository.OfflineSyncContract? = null
) : CourseRepositoryContract {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDefaultDataIfNeeded()
        }
    }

    private suspend fun seedDefaultDataIfNeeded() {
        if (courseDao.getAllCoursesList().isEmpty()) {
            val defaultCourses = listOf(
                CourseEntity(
                    id = "c_ai_101",
                    title = "الذكاء الاصطناعي والشبكات العصبيّة",
                    description = "تعلم بنى التعلم العميق، ونماذج المحولات، والانتشار العكسي، وبناء نماذج الذكاء الاصطناعي العملية.",
                    category = "الذكاء الاصطناعي والبيانات",
                    totalLessons = 6,
                    durationMinutes = 240,
                    iconUrl = null,
                    isDownloaded = true,
                    progressPercent = 0.35f,
                    isEnrolled = true,
                    isBookmarked = true,
                    schoolId = "school_001"
                ),
                CourseEntity(
                    id = "c_cs_201",
                    title = "كوتلن الحديثة وهندسة أندرويد النظيفة",
                    description = "صمّم تطبيقات أندرويد مؤسسية قابلة للتوسع باستخدام Jetpack Compose والطبقات النظيفة وتدفقات Coroutines.",
                    category = "علوم الحاسوب",
                    totalLessons = 8,
                    durationMinutes = 310,
                    iconUrl = null,
                    isDownloaded = false,
                    progressPercent = 0.10f,
                    isEnrolled = true,
                    isBookmarked = false,
                    schoolId = "school_001"
                ),
                CourseEntity(
                    id = "c_data_301",
                    title = "تحليل البيانات والرياضيات المالية",
                    description = "اتقن استخراج البيانات وتحليل السلاسل الزمنية والنمذجة الإحصائية لتطبيقات الأعمال والتمويل.",
                    category = "البيانات والمالية",
                    totalLessons = 10,
                    durationMinutes = 420,
                    iconUrl = null,
                    isDownloaded = false,
                    progressPercent = 0.0f,
                    isEnrolled = false,
                    isBookmarked = true,
                    schoolId = "school_001"
                ),
                CourseEntity(
                    id = "c_cyber_401",
                    title = "الأمن السيبراني وحماية الشبكات",
                    description = "أساسيات التشفير، واختبار الاختراق الأخلاقي، وتأمين البنى التحتية والتطبيقات.",
                    category = "الأمن السيبراني",
                    totalLessons = 7,
                    durationMinutes = 280,
                    iconUrl = null,
                    isDownloaded = false,
                    progressPercent = 0.0f,
                    isEnrolled = false,
                    isBookmarked = false,
                    schoolId = "school_001"
                )
            )

            val defaultLessons = listOf(
                LessonEntity(
                    id = "l_ai_1",
                    courseId = "c_ai_101",
                    title = "مقدمة في الذكاء الاصطناعي والشبكات العصبية",
                    content = "الخلية العصبية الاصطناعية ومعمارية Deep Learning وطرق التدريب الفعالة.",
                    order = 1,
                    isCompleted = true,
                    audioUrl = null,
                    schoolId = "school_001"
                ),
                LessonEntity(
                    id = "l_ai_2",
                    courseId = "c_ai_101",
                    title = "دوال التنشيط والانتشار الأمامي",
                    content = "فهم ReLu, Sigmoid, Softmax وكيف تنقل الإشارات عبر طبقات الشبكة.",
                    order = 2,
                    isCompleted = false,
                    audioUrl = null,
                    schoolId = "school_001"
                )
            )

            courseDao.insertCourses(defaultCourses)
            lessonDao.insertLessons(defaultLessons)
        }
    }

    override fun getCourses(): Flow<List<Course>> {
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
        return courseDao.getCourseById(courseId).map { it?.toDomain() }
    }

    override fun getLessonsForCourse(courseId: String): Flow<List<Lesson>> {
        return lessonDao.getLessonsForCourse(courseId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLessonById(lessonId: String): Flow<Lesson?> {
        return lessonDao.observeLessonById(lessonId).map { it?.toDomain() }
    }

    override fun getNextLesson(courseId: String, currentLessonId: String): Flow<Lesson?> {
        return lessonDao.getNextLessonEntity(courseId, currentLessonId).map { it?.toDomain() }
    }

    override fun getPagedCourses(request: PageRequest): Flow<PagedData<Course>> {
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
            val lesson = lessonDao.getLessonById(lessonId)
            if (lesson != null) {
                lessonDao.insertLesson(lesson.copy(isCompleted = true))
            }

            val lessons = lessonDao.getLessonsForCourseList(courseId)
            val completedCount = lessons.count { it.isCompleted }
            val totalCount = lessons.size.coerceAtLeast(1)
            val progressPercent = completedCount.toFloat() / totalCount.toFloat()

            courseDao.updateCourseProgress(courseId, progressPercent)

            val userId = currentUserIdProvider?.invoke()
            if (userId != null) {
                firestoreSyncManager?.syncCourseProgressToCloud(
                    userId = userId,
                    courseId = courseId,
                    progressPercent = progressPercent,
                    completedLessonsCount = completedCount
                )
            }

            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.DatabaseError("Failed to mark lesson complete", e))
        }
    }

    override suspend fun updateLessonProgress(
        lessonId: String,
        courseId: String,
        progressPercent: Float
    ): RtiqaResult<Unit> {
        return try {
            val userId = currentUserIdProvider?.invoke()
            if (userId != null) {
                firestoreSyncManager?.syncCourseProgressToCloud(
                    userId = userId,
                    courseId = courseId,
                    progressPercent = progressPercent,
                    completedLessonsCount = lessonDao.getCompletedLessonsCount(courseId)
                )
            }
            offlineSyncManager?.enqueueOfflineAction(
                actionType = "LESSON_PROGRESS_UPDATE",
                payloadJson = "{\"lessonId\":\"$lessonId\",\"courseId\":\"$courseId\",\"progress\":$progressPercent}"
            )
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
                firestoreSyncManager?.syncCourseProgressToCloud(
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
                firestoreSyncManager?.fetchUserProfileFromCloud(userId)
            }
            offlineSyncManager?.syncRemoteCourses() ?: RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.SyncError("Failed to sync courses with cloud", e))
        }
    }
}
