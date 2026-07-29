package com.rtiqa.core.domain.usecase

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.core.domain.model.PageRequest
import com.rtiqa.core.domain.model.PagedData
import com.rtiqa.core.domain.repository.CourseRepositoryContract
import com.rtiqa.core.domain.repository.DownloadManagerContract
import com.rtiqa.core.domain.repository.UserRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import kotlinx.coroutines.flow.Flow

/**
 * Use case to retrieve all available courses.
 */
class GetCoursesUseCase(
    private val courseRepository: CourseRepositoryContract
) {
    operator fun invoke(): Flow<List<Course>> = courseRepository.getCourses()
}

/**
 * Use case to retrieve paged courses with optional filter.
 */
class GetPagedCoursesUseCase(
    private val courseRepository: CourseRepositoryContract
) {
    operator fun invoke(request: PageRequest): Flow<PagedData<Course>> {
        return courseRepository.getPagedCourses(request)
    }
}

/**
 * Use case to retrieve course detail by ID.
 */
class GetCourseDetailUseCase(
    private val courseRepository: CourseRepositoryContract
) {
    operator fun invoke(courseId: String): Flow<Course?> = courseRepository.getCourseById(courseId)
}

/**
 * Use case to retrieve lessons associated with a course.
 */
class GetLessonsForCourseUseCase(
    private val courseRepository: CourseRepositoryContract
) {
    operator fun invoke(courseId: String): Flow<List<Lesson>> = courseRepository.getLessonsForCourse(courseId)
}

/**
 * Use case to mark a lesson as completed, triggering XP rewards for the user.
 */
class CompleteLessonUseCase(
    private val courseRepository: CourseRepositoryContract,
    private val userRepository: UserRepositoryContract
) {
    suspend operator fun invoke(lessonId: String, courseId: String): RtiqaResult<Unit> {
        if (lessonId.isBlank() || courseId.isBlank()) {
            return RtiqaResult.Error(RtiqaError.ValidationError(listOf("Invalid lesson or course identifier.")))
        }
        val completeResult = courseRepository.markLessonCompleted(lessonId, courseId)
        if (completeResult is RtiqaResult.Success) {
            // Reward 25 XP for completing a lesson
            userRepository.addXp(25)
        }
        return completeResult
    }
}

/**
 * Use case to search courses by query keyword.
 */
class SearchCoursesUseCase(
    private val courseRepository: CourseRepositoryContract
) {
    suspend operator fun invoke(query: String): List<Course> {
        if (query.trim().isEmpty()) return emptyList()
        return courseRepository.searchCourses(query.trim())
    }
}

/**
 * Use case to trigger course content download for offline availability.
 */
class DownloadCourseUseCase(
    private val downloadManager: DownloadManagerContract
) {
    suspend operator fun invoke(courseId: String): RtiqaResult<Unit> {
        if (courseId.isBlank()) {
            return RtiqaResult.Error(RtiqaError.ValidationError(listOf("Course ID cannot be blank.")))
        }
        return downloadManager.downloadCourse(courseId)
    }
}

/**
 * Use case to save/create a course in the platform.
 */
class SaveCourseUseCase(
    private val courseRepository: CourseRepositoryContract
) {
    suspend operator fun invoke(course: Course): RtiqaResult<Unit> {
        if (course.title.isBlank()) {
            return RtiqaResult.Error(RtiqaError.ValidationError(listOf("Course title cannot be blank.")))
        }
        return courseRepository.saveCourse(course)
    }
}

/**
 * Use case to delete a course from the platform.
 */
class DeleteCourseUseCase(
    private val courseRepository: CourseRepositoryContract
) {
    suspend operator fun invoke(courseId: String): RtiqaResult<Unit> {
        if (courseId.isBlank()) {
            return RtiqaResult.Error(RtiqaError.ValidationError(listOf("Course ID cannot be blank.")))
        }
        return courseRepository.deleteCourse(courseId)
    }
}
