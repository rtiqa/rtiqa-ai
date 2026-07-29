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

class CourseRepositoryImpl(
    private val courseDao: CourseDao,
    private val lessonDao: LessonDao,
    private val firestoreSyncManager: FirestoreSyncManager? = null,
    private val currentUserIdProvider: (suspend () -> String?)? = null
) : CourseRepositoryContract {

    override fun getCourses(): Flow<List<Course>> {
        return courseDao.getAllCourses().map { entities ->
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

    override fun getPagedCourses(request: PageRequest): Flow<PagedData<Course>> {
        val filterCat = request.filterCategory
        val query = request.searchQuery
        return courseDao.getAllCourses().map { entities ->
            val domainList = entities.map { it.toDomain() }
                .filter { course ->
                    filterCat == null || course.category.equals(filterCat, ignoreCase = true)
                }
                .filter { course ->
                    query == null || course.title.contains(query, ignoreCase = true)
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

            val userId = currentUserIdProvider?.invoke()
            if (userId != null) {
                val lessons = lessonDao.getLessonsForCourseList(courseId)
                val completedCount = lessons.count { it.isCompleted }
                val totalCount = lessons.size.coerceAtLeast(1)
                val progressPercent = completedCount.toFloat() / totalCount.toFloat()

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
}
