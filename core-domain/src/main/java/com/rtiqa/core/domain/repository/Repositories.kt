package com.rtiqa.core.domain.repository

import com.rtiqa.core.domain.model.AiInsight
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.core.domain.model.PageRequest
import com.rtiqa.core.domain.model.PagedData
import com.rtiqa.core.domain.model.Quiz
import com.rtiqa.core.domain.model.QuizResult
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.result.RtiqaResult
import kotlinx.coroutines.flow.Flow

/**
 * Contract for Authentication operations.
 */
interface AuthRepositoryContract {
    fun observeUserSession(): Flow<UserProfile?>
    suspend fun login(email: String, pass: String): RtiqaResult<UserProfile>
    suspend fun register(name: String, email: String, pass: String): RtiqaResult<UserProfile>
    suspend fun logout(): RtiqaResult<Unit>
    suspend fun getCurrentUserId(): String?
    suspend fun resetPassword(email: String): RtiqaResult<Unit>
}

/**
 * Contract for Course and Lesson management.
 */
interface CourseRepositoryContract {
    fun getCourses(): Flow<List<Course>>
    fun getCourseById(courseId: String): Flow<Course?>
    fun getLessonsForCourse(courseId: String): Flow<List<Lesson>>
    fun getLessonById(lessonId: String): Flow<Lesson?>
    fun getNextLesson(courseId: String, currentLessonId: String): Flow<Lesson?>
    fun getPagedCourses(request: PageRequest): Flow<PagedData<Course>>
    suspend fun searchCourses(query: String): List<Course>
    suspend fun markLessonCompleted(lessonId: String, courseId: String): RtiqaResult<Unit>
    suspend fun updateLessonProgress(lessonId: String, courseId: String, progressPercent: Float): RtiqaResult<Unit>
    suspend fun saveCourse(course: Course): RtiqaResult<Unit>
    suspend fun deleteCourse(courseId: String): RtiqaResult<Unit>
    suspend fun saveLesson(lesson: Lesson): RtiqaResult<Unit>
    suspend fun enrollInCourse(courseId: String): RtiqaResult<Unit>
    suspend fun toggleBookmark(courseId: String, isBookmarked: Boolean): RtiqaResult<Unit>
    suspend fun toggleCourseDownload(courseId: String, isDownloaded: Boolean): RtiqaResult<Unit>
    suspend fun syncCourses(): RtiqaResult<Unit>
}

/**
 * Contract for Assessment and Quiz evaluation.
 */
interface QuizRepositoryContract {
    fun getQuizzesForCourse(courseId: String): Flow<List<Quiz>>
    fun getQuizForCourse(courseId: String): Flow<Quiz?>
    fun getQuizById(quizId: String): Flow<Quiz?>
    suspend fun submitQuizResult(quizId: String, score: Int, total: Int): RtiqaResult<QuizResult>
    fun getQuizResultsForUser(quizId: String, userId: String): Flow<List<QuizResult>>
    suspend fun saveQuiz(quiz: Quiz): RtiqaResult<Unit>
}

/**
 * Contract for User Profile and Progression.
 */
interface UserRepositoryContract {
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun updateUserProfile(profile: UserProfile): RtiqaResult<Unit>
    suspend fun addXp(amount: Int): RtiqaResult<Unit>
    suspend fun incrementStreak(): RtiqaResult<Unit>
}

/**
 * Contract for AI Tutor and Generation services.
 */
interface AiRepositoryContract {
    suspend fun generateEducationalSummary(prompt: String): String
    suspend fun askAiTutor(question: String, courseContext: String? = null): RtiqaResult<AiInsight>
    fun getAiHistory(): Flow<List<AiInsight>>
}

/**
 * Contract for Offline Synchronization management.
 */
interface OfflineSyncContract {
    suspend fun syncRemoteCourses(): RtiqaResult<Unit>
    suspend fun enqueueOfflineAction(actionType: String, payloadJson: String): RtiqaResult<Unit>
    fun observePendingSyncCount(): Flow<Int>
}

/**
 * Contract for Media and Course Content Download management.
 */
interface DownloadManagerContract {
    fun observeCourseDownloadProgress(courseId: String): Flow<Float>
    suspend fun downloadCourse(courseId: String): RtiqaResult<Unit>
    suspend fun deleteCourseDownload(courseId: String): RtiqaResult<Unit>
}

/**
 * Contract for Dynamic Device Permissions abstraction.
 */
interface PermissionContract {
    fun isPermissionGranted(permissionName: String): Boolean
    fun observePermissionStatus(permissionName: String): Flow<Boolean>
}
