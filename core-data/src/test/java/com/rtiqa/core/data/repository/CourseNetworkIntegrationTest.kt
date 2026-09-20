package com.rtiqa.core.data.repository

import com.rtiqa.core.database.dao.CourseDao
import com.rtiqa.core.database.dao.LessonDao
import com.rtiqa.core.database.entity.CourseEntity
import com.rtiqa.core.database.entity.LessonEntity
import com.rtiqa.core.network.api.RtiqaApiService
import com.rtiqa.core.network.interceptor.RestAuthInterceptor
import com.rtiqa.core.network.interceptor.RestTenantInterceptor
import com.rtiqa.core.network.session.RestSessionStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class CourseNetworkIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: RtiqaApiService
    private lateinit var fakeCourseDao: InMemoryCourseDao
    private lateinit var fakeLessonDao: InMemoryLessonDao

    private val fakeSessionStore = object : RestSessionStore {
        override fun saveSession(token: String, organizationId: String?) {}
        override fun getSessionToken(): String? = "test_jwt_bearer_token_123"
        override fun getActiveOrganizationId(): String? = "00000000-0000-0000-0000-000000000001"
        override fun updateActiveOrganizationId(organizationId: String?) {}
        override fun generateAndSaveSessionId(): String = "session_1"
        override fun getSessionId(): String? = "session_1"
        override fun clearSession() {}
    }

    private class InMemoryCourseDao : CourseDao {
        val courseStateFlow = MutableStateFlow<List<CourseEntity>>(emptyList())

        override fun getAllCourses(): Flow<List<CourseEntity>> = courseStateFlow
        override fun getCoursesForSchool(schoolId: String): Flow<List<CourseEntity>> = flowOf(emptyList())
        override suspend fun getAllCoursesList(): List<CourseEntity> = courseStateFlow.value
        override fun getCourseById(id: String): Flow<CourseEntity?> = flowOf(courseStateFlow.value.find { it.id == id })
        override suspend fun insertCourse(course: CourseEntity) {
            courseStateFlow.value = courseStateFlow.value.filter { it.id != course.id } + course
        }
        override suspend fun insertCourses(courses: List<CourseEntity>) {
            val map = courseStateFlow.value.associateBy { it.id }.toMutableMap()
            courses.forEach { map[it.id] = it }
            courseStateFlow.value = map.values.toList()
        }
        override suspend fun deleteCourseById(id: String) {
            courseStateFlow.value = courseStateFlow.value.filter { it.id != id }
        }
        override suspend fun updateEnrollmentStatus(id: String, isEnrolled: Boolean) {}
        override suspend fun updateBookmarkStatus(id: String, isBookmarked: Boolean) {}
        override suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean) {}
        override suspend fun updateCourseProgress(id: String, progressPercent: Float) {}
    }

    private class InMemoryLessonDao : LessonDao {
        val lessonStateFlow = MutableStateFlow<List<LessonEntity>>(emptyList())

        override fun getLessonsForCourse(courseId: String): Flow<List<LessonEntity>> =
            lessonStateFlow
        override suspend fun getLessonsForCourseList(courseId: String): List<LessonEntity> =
            lessonStateFlow.value.filter { it.courseId == courseId }
        override suspend fun getLessonById(id: String): LessonEntity? =
            lessonStateFlow.value.find { it.id == id }
        override fun observeLessonById(id: String): Flow<LessonEntity?> =
            flowOf(lessonStateFlow.value.find { it.id == id })
        override fun getNextLessonEntity(courseId: String, currentLessonId: String): Flow<LessonEntity?> =
            flowOf(null)
        override suspend fun insertLesson(lesson: LessonEntity) {
            lessonStateFlow.value = lessonStateFlow.value.filter { it.id != lesson.id } + lesson
        }
        override suspend fun insertLessons(lessons: List<LessonEntity>) {
            val map = lessonStateFlow.value.associateBy { it.id }.toMutableMap()
            lessons.forEach { map[it.id] = it }
            lessonStateFlow.value = map.values.toList()
        }
        override suspend fun deleteLessonsForCourse(courseId: String) {
            lessonStateFlow.value = lessonStateFlow.value.filter { it.courseId != courseId }
        }
        override suspend fun updateLessonCompletion(id: String, isCompleted: Boolean) {}
        override suspend fun getTotalLessonsCount(courseId: String): Int = lessonStateFlow.value.size
        override suspend fun getCompletedLessonsCount(courseId: String): Int = lessonStateFlow.value.count { it.isCompleted }
    }

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(RestAuthInterceptor(fakeSessionStore))
            .addInterceptor(RestTenantInterceptor(fakeSessionStore))
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RtiqaApiService::class.java)

        fakeCourseDao = InMemoryCourseDao()
        fakeLessonDao = InMemoryLessonDao()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun getCourses_callsEndpoint_withJwtAndTenantId_andUpdatesRepository() = runTest {
        // Given Ktor JSON response format
        val ktorJsonResponse = """
            [
              {
                "id": "c_ai_101",
                "title": "الذكاء الاصطناعي",
                "description": "مقدمة شاملة",
                "category": "AI",
                "difficulty": "Beginner",
                "totalModules": 5,
                "completedModules": 1,
                "progressPercent": 0.2
              }
            ]
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(ktorJsonResponse)
                .addHeader("Content-Type", "application/json")
        )

        val repository = CourseRepositoryImpl(
            courseDao = fakeCourseDao,
            lessonDao = fakeLessonDao,
            apiService = apiService
        )

        // When observing getCourses
        val coursesFlow = repository.getCourses()

        // Wait until remote fetch inserts and flow emits non-empty
        val courses = coursesFlow.filter { it.isNotEmpty() }.first()

        // Then verify request sent to MockWebServer
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("GET", recordedRequest.method)
        assertEquals("/api/v1/courses", recordedRequest.path)
        assertEquals("Bearer test_jwt_bearer_token_123", recordedRequest.getHeader("Authorization"))
        assertEquals("00000000-0000-0000-0000-000000000001", recordedRequest.getHeader("X-Tenant-Id"))

        // Then verify repository received and mapped data correctly
        assertEquals(1, courses.size)
        assertEquals("c_ai_101", courses[0].id)
        assertEquals("الذكاء الاصطناعي", courses[0].title)
        assertEquals("AI", courses[0].category)
        assertEquals(5, courses[0].totalLessons)
    }

    @Test
    fun getCourseLessons_callsEndpoint_withJwtAndTenantId_andUpdatesRepository() = runTest {
        // Given Ktor JSON response format
        val ktorJsonResponse = """
            [
              {
                "id": "l_ai_101_1",
                "courseId": "c_ai_101",
                "title": "مقدمة في الذكاء الاصطناعي",
                "content": "محتوى الدرس الأول",
                "moduleOrder": 1,
                "estimatedMinutes": 20
              }
            ]
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(ktorJsonResponse)
                .addHeader("Content-Type", "application/json")
        )

        val repository = CourseRepositoryImpl(
            courseDao = fakeCourseDao,
            lessonDao = fakeLessonDao,
            apiService = apiService
        )

        // When observing getLessonsForCourse
        val lessonsFlow = repository.getLessonsForCourse("c_ai_101")

        // Wait until remote fetch inserts and flow emits non-empty
        val lessons = lessonsFlow.filter { it.isNotEmpty() }.first()

        // Then verify request sent to MockWebServer
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("GET", recordedRequest.method)
        assertEquals("/api/v1/courses/c_ai_101/lessons", recordedRequest.path)
        assertEquals("Bearer test_jwt_bearer_token_123", recordedRequest.getHeader("Authorization"))
        assertEquals("00000000-0000-0000-0000-000000000001", recordedRequest.getHeader("X-Tenant-Id"))

        // Then verify repository received and mapped lesson correctly
        assertEquals(1, lessons.size)
        assertEquals("l_ai_101_1", lessons[0].id)
        assertEquals("c_ai_101", lessons[0].courseId)
        assertEquals("مقدمة في الذكاء الاصطناعي", lessons[0].title)
        assertEquals("محتوى الدرس الأول", lessons[0].content)
        assertEquals(1, lessons[0].order)
    }

    @Test
    fun markLessonCompleted_sendsPostRequestWithJwtAndTenantId_andUpdatesLocalCache() = runTest {
        // Given initial lesson in Dao
        val initialLesson = LessonEntity(
            id = "l_ai_101_1",
            courseId = "c_ai_101",
            title = "مقدمة",
            content = "محتوى",
            order = 1,
            isCompleted = false,
            audioUrl = null
        )
        fakeLessonDao.insertLesson(initialLesson)

        // Given server responds with 200 and completion payload
        val ktorCompletionResponse = """
            {
              "success": true,
              "lessonId": "l_ai_101_1",
              "courseId": "c_ai_101",
              "completed": true,
              "courseProgressPercent": 50.0,
              "completedLessons": 1,
              "totalLessons": 2
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(ktorCompletionResponse)
                .addHeader("Content-Type", "application/json")
        )

        val repository = CourseRepositoryImpl(
            courseDao = fakeCourseDao,
            lessonDao = fakeLessonDao,
            apiService = apiService
        )

        // When
        val result = repository.markLessonCompleted("l_ai_101_1", "c_ai_101")

        // Then verify HTTP request
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertEquals("/api/v1/courses/c_ai_101/lessons/l_ai_101_1/complete", recordedRequest.path)
        assertEquals("Bearer test_jwt_bearer_token_123", recordedRequest.getHeader("Authorization"))
        assertEquals("00000000-0000-0000-0000-000000000001", recordedRequest.getHeader("X-Tenant-Id"))

        // Then verify result and cache update
        assertTrue(result is com.rtiqa.core.domain.result.RtiqaResult.Success)
        val updatedLesson = fakeLessonDao.getLessonById("l_ai_101_1")
        assertNotNull(updatedLesson)
        assertTrue(updatedLesson!!.isCompleted)
    }

    @Test
    fun completeLessonUseCase_invokesRepositoryAndAwardsXp() = runTest {
        fakeLessonDao.insertLesson(
            LessonEntity(
                id = "l_ai_101_1",
                courseId = "c_ai_101",
                title = "مقدمة",
                content = "محتوى",
                order = 1,
                isCompleted = false,
                audioUrl = null
            )
        )

        val ktorCompletionResponse = """
            {
              "success": true,
              "lessonId": "l_ai_101_1",
              "courseId": "c_ai_101",
              "completed": true,
              "courseProgressPercent": 100.0,
              "completedLessons": 1,
              "totalLessons": 1
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(ktorCompletionResponse)
                .addHeader("Content-Type", "application/json")
        )

        val repository = CourseRepositoryImpl(
            courseDao = fakeCourseDao,
            lessonDao = fakeLessonDao,
            apiService = apiService
        )

        var awardedXp = 0
        val fakeUserRepository = object : com.rtiqa.core.domain.repository.UserRepositoryContract {
            override fun getUserProfile(): Flow<com.rtiqa.core.domain.model.UserProfile?> = flowOf(null)
            override suspend fun updateUserProfile(profile: com.rtiqa.core.domain.model.UserProfile): com.rtiqa.core.domain.result.RtiqaResult<Unit> = com.rtiqa.core.domain.result.RtiqaResult.Success(Unit)
            override suspend fun addXp(amount: Int): com.rtiqa.core.domain.result.RtiqaResult<Unit> {
                awardedXp += amount
                return com.rtiqa.core.domain.result.RtiqaResult.Success(Unit)
            }
            override suspend fun incrementStreak(): com.rtiqa.core.domain.result.RtiqaResult<Unit> = com.rtiqa.core.domain.result.RtiqaResult.Success(Unit)
        }

        val useCase = com.rtiqa.core.domain.usecase.CompleteLessonUseCase(
            courseRepository = repository,
            userRepository = fakeUserRepository
        )

        val result = useCase.invoke("l_ai_101_1", "c_ai_101")
        assertTrue(result is com.rtiqa.core.domain.result.RtiqaResult.Success)
        assertEquals(25, awardedXp)
    }

    @Test
    fun markLessonCompleted_handles401Unauthorized() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"status":401,"message":"Unauthorized"}""")
        )

        val repository = CourseRepositoryImpl(
            courseDao = fakeCourseDao,
            lessonDao = fakeLessonDao,
            apiService = apiService
        )

        val result = repository.markLessonCompleted("l_ai_101_1", "c_ai_101")
        assertTrue(result is com.rtiqa.core.domain.result.RtiqaResult.Error)
        val error = (result as com.rtiqa.core.domain.result.RtiqaResult.Error).error
        assertTrue(error is com.rtiqa.core.domain.error.RtiqaError.AuthError)
        assertEquals("401", (error as com.rtiqa.core.domain.error.RtiqaError.AuthError).errorCode)
    }

    @Test
    fun markLessonCompleted_handles403Forbidden() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(403)
                .setBody("""{"status":403,"message":"User not enrolled in course"}""")
        )

        val repository = CourseRepositoryImpl(
            courseDao = fakeCourseDao,
            lessonDao = fakeLessonDao,
            apiService = apiService
        )

        val result = repository.markLessonCompleted("l_ai_101_1", "c_ai_101")
        assertTrue(result is com.rtiqa.core.domain.result.RtiqaResult.Error)
        val error = (result as com.rtiqa.core.domain.result.RtiqaResult.Error).error
        assertTrue(error is com.rtiqa.core.domain.error.RtiqaError.AuthError)
        assertEquals("403", (error as com.rtiqa.core.domain.error.RtiqaError.AuthError).errorCode)
    }

    @Test
    fun markLessonCompleted_handles404NotFound() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("""{"status":404,"message":"Course or lesson not found"}""")
        )

        val repository = CourseRepositoryImpl(
            courseDao = fakeCourseDao,
            lessonDao = fakeLessonDao,
            apiService = apiService
        )

        val result = repository.markLessonCompleted("l_nonexistent", "c_nonexistent")
        assertTrue(result is com.rtiqa.core.domain.result.RtiqaResult.Error)
        val error = (result as com.rtiqa.core.domain.result.RtiqaResult.Error).error
        assertTrue(error is com.rtiqa.core.domain.error.RtiqaError.NetworkError)
        assertEquals(404, (error as com.rtiqa.core.domain.error.RtiqaError.NetworkError).statusCode)
    }
}
