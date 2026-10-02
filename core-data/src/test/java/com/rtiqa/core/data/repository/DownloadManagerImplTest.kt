package com.rtiqa.core.data.repository

import com.rtiqa.core.database.dao.CourseDao
import com.rtiqa.core.database.dao.LessonDao
import com.rtiqa.core.database.entity.CourseEntity
import com.rtiqa.core.database.entity.LessonEntity
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.network.api.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class DownloadManagerImplTest {
    private val operations = mutableListOf<String>()
    private val courseDao = FakeCourseDao(operations)
    private val lessonDao = FakeLessonDao(operations)
    private val api = FakeApi(operations)
    private val manager = DownloadManagerImpl(courseDao, lessonDao, api)

    @Test
    fun successfulDownload_fetchesCourseAndLessons_beforeMarkingDownloaded() = runTest {
        val result = manager.downloadCourse("c1")

        assertTrue(result is RtiqaResult.Success)
        assertTrue(operations.indexOf("fetch-course") < operations.indexOf("fetch-lessons"))
        assertTrue(operations.indexOf("persist-lessons") < operations.lastIndexOf("download-true"))
    }

    @Test
    fun successfulDownload_persistsLessonsToCoreRoom() = runTest {
        manager.downloadCourse("c1")
        assertEquals("Real lesson content", lessonDao.lessons.value.single().content)
        assertEquals("audio.mp3", lessonDao.lessons.value.single().audioUrl)
    }

    @Test
    fun successfulDownload_preservesCompletedLessonState() = runTest {
        lessonDao.lessons.value = listOf(lessonEntity(isCompleted = true))
        manager.downloadCourse("c1")
        assertTrue(lessonDao.lessons.value.single().isCompleted)
    }

    @Test
    fun courseHttpFailure_doesNotMarkDownloaded() = runTest {
        api.courseResponse = Response.error(503, "unavailable".toResponseBody())
        assertTrue(manager.downloadCourse("c1") is RtiqaResult.Error)
        assertFalse(courseDao.courses.value.single().isDownloaded)
    }

    @Test
    fun lessonHttpFailure_doesNotMarkDownloaded() = runTest {
        api.lessonsResponse = Response.error(503, "unavailable".toResponseBody())
        assertTrue(manager.downloadCourse("c1") is RtiqaResult.Error)
        assertFalse(courseDao.courses.value.single().isDownloaded)
    }

    @Test
    fun missingRequiredBody_doesNotMarkDownloaded() = runTest {
        api.courseResponse = Response.success(null)
        assertTrue(manager.downloadCourse("c1") is RtiqaResult.Error)
        assertFalse(courseDao.courses.value.single().isDownloaded)
    }

    @Test
    fun nonEmptyCourseWithEmptyLessons_doesNotMarkDownloaded() = runTest {
        api.lessonsResponse = Response.success(emptyList())
        assertTrue(manager.downloadCourse("c1") is RtiqaResult.Error)
        assertFalse(courseDao.courses.value.single().isDownloaded)
    }

    @Test
    fun failure_doesNotReachCompletedProgress() = runTest {
        api.lessonsResponse = Response.error(500, "error".toResponseBody())
        manager.downloadCourse("c1")
        assertEquals(0f, manager.observeCourseDownloadProgress("c1").first())
    }

    @Test
    fun success_reachesCompletedProgress() = runTest {
        manager.downloadCourse("c1")
        assertEquals(1f, manager.observeCourseDownloadProgress("c1").first())
    }

    @Test
    fun deleteDownload_clearsDownloadedFlag() = runTest {
        courseDao.courses.value = listOf(courseEntity(isDownloaded = true))
        assertTrue(manager.deleteCourseDownload("c1") is RtiqaResult.Success)
        assertFalse(courseDao.courses.value.single().isDownloaded)
    }

    private fun courseEntity(isDownloaded: Boolean = false) = CourseEntity(
        "c1", "Old", "Old", "AI", 1, 10, null, isDownloaded, .4f, true, true
    )

    private fun lessonEntity(isCompleted: Boolean) = LessonEntity(
        "l1", "c1", "Old", "Old", 1, isCompleted, null
    )

    private inner class FakeCourseDao(private val log: MutableList<String>) : CourseDao {
        val courses = MutableStateFlow(listOf(courseEntity()))
        override fun getAllCourses(): Flow<List<CourseEntity>> = courses
        override fun getCoursesForSchool(schoolId: String): Flow<List<CourseEntity>> = courses
        override suspend fun getAllCoursesList() = courses.value
        override fun getCourseById(id: String) = courses.map { list -> list.find { it.id == id } }
        override suspend fun insertCourse(course: CourseEntity) {
            log += "persist-course"
            courses.value = courses.value.filterNot { it.id == course.id } + course
        }
        override suspend fun insertCourses(courses: List<CourseEntity>) = courses.forEach { insertCourse(it) }
        override suspend fun deleteCourseById(id: String) { courses.value = courses.value.filterNot { it.id == id } }
        override suspend fun updateEnrollmentStatus(id: String, isEnrolled: Boolean) = Unit
        override suspend fun updateBookmarkStatus(id: String, isBookmarked: Boolean) = Unit
        override suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean) {
            log += "download-$isDownloaded"
            courses.value = courses.value.map { if (it.id == id) it.copy(isDownloaded = isDownloaded) else it }
        }
        override suspend fun updateCourseProgress(id: String, progressPercent: Float) = Unit
    }

    private class FakeLessonDao(private val log: MutableList<String>) : LessonDao {
        val lessons = MutableStateFlow<List<LessonEntity>>(emptyList())
        override fun getLessonsForCourse(courseId: String) = lessons.map { it.filter { lesson -> lesson.courseId == courseId } }
        override suspend fun getLessonsForCourseList(courseId: String) = lessons.value.filter { it.courseId == courseId }
        override suspend fun getLessonById(id: String) = lessons.value.find { it.id == id }
        override fun observeLessonById(id: String) = lessons.map { list -> list.find { it.id == id } }
        override fun getNextLessonEntity(courseId: String, currentLessonId: String) = MutableStateFlow<LessonEntity?>(null)
        override suspend fun insertLesson(lesson: LessonEntity) { insertLessons(listOf(lesson)) }
        override suspend fun insertLessons(lessons: List<LessonEntity>) {
            log += "persist-lessons"
            val incoming = lessons.associateBy { it.id }
            this.lessons.value = (this.lessons.value.filterNot { it.id in incoming } + lessons)
        }
        override suspend fun deleteLessonsForCourse(courseId: String) { lessons.value = lessons.value.filterNot { it.courseId == courseId } }
        override suspend fun updateLessonCompletion(id: String, isCompleted: Boolean) = Unit
        override suspend fun getTotalLessonsCount(courseId: String) = lessons.value.count { it.courseId == courseId }
        override suspend fun getCompletedLessonsCount(courseId: String) = lessons.value.count { it.courseId == courseId && it.isCompleted }
    }

    private class FakeApi(private val log: MutableList<String>) : RtiqaApiService {
        var courseResponse: Response<NetworkCourseDto> = Response.success(
            NetworkCourseDto("c1", "Real course", "Description", "AI", "Beginner", 1, 0, .2f)
        )
        var lessonsResponse: Response<List<NetworkLessonDto>> = Response.success(
            listOf(NetworkLessonDto("l1", "c1", "Real lesson", "Real lesson content", 1, 12, false, "audio.mp3"))
        )
        override suspend fun getCourse(courseId: String): Response<NetworkCourseDto> {
            log += "fetch-course"; return courseResponse
        }
        override suspend fun getCourseLessons(courseId: String): Response<List<NetworkLessonDto>> {
            log += "fetch-lessons"; return lessonsResponse
        }
        override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> = error("unused")
        override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> = error("unused")
        override suspend fun getUserProfile(): Response<NetworkUserDto> = error("unused")
        override suspend fun getCourses(category: String?): Response<List<NetworkCourseDto>> = error("unused")
        override suspend fun getLesson(courseId: String, lessonId: String): Response<NetworkLessonDto> = error("unused")
        override suspend fun completeLesson(courseId: String, lessonId: String): Response<LessonCompletionResponseDto> = error("unused")
        override suspend fun updateLessonProgress(courseId: String, lessonId: String, request: LessonProgressRequestDto): Response<LessonProgressResponseDto> = error("unused")
        override suspend fun syncOfflineData(payload: NetworkSyncPayloadDto): Response<NetworkSyncResponseDto> = error("unused")
        override suspend fun getClassGradebook(classId: String): Response<ClassGradebookDto> = error("unused")
    }
}
