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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
        override fun getCourseById(id: String): Flow<CourseEntity?> = courseStateFlow.map { list -> list.find { it.id == id } }
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
        override suspend fun updateCourseProgress(id: String, progressPercent: Float) {
            courseStateFlow.value = courseStateFlow.value.map {
                if (it.id == id) it.copy(progressPercent = progressPercent) else it
            }
        }
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
            lessonStateFlow.map { list -> list.find { it.id == id } }
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
        override suspend fun updateLessonCompletion(id: String, isCompleted: Boolean) {
            lessonStateFlow.value = lessonStateFlow.value.map {
                if (it.id == id) it.copy(isCompleted = isCompleted) else it
            }
        }
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

    @Test
    fun getCourseById_callsEndpoint_withJwtAndTenantId_andUpdatesRepository() = runTest {
        // Given Ktor JSON response format for a single course
        val ktorJsonResponse = """
            {
              "id": "c_ai_101",
              "title": "الذكاء الاصطناعي المتقدم",
              "description": "مقدمة شاملة",
              "category": "AI",
              "difficulty": "Intermediate",
              "totalModules": 8,
              "completedModules": 2,
              "progressPercent": 25.0
            }
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

        // When observing getCourseById
        val courseFlow = repository.getCourseById("c_ai_101")

        // Wait until remote fetch inserts and flow emits non-null
        val course = courseFlow.filterNotNull().first()

        // Then verify request sent to MockWebServer
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("GET", recordedRequest.method)
        assertEquals("/api/v1/courses/c_ai_101", recordedRequest.path)
        assertEquals("Bearer test_jwt_bearer_token_123", recordedRequest.getHeader("Authorization"))
        assertEquals("00000000-0000-0000-0000-000000000001", recordedRequest.getHeader("X-Tenant-Id"))

        // Then verify repository received and mapped data correctly
        assertEquals("c_ai_101", course.id)
        assertEquals("الذكاء الاصطناعي المتقدم", course.title)
        assertEquals("AI", course.category)
        assertEquals(8, course.totalLessons)
        assertEquals(25.0f, course.progressPercent, 0.01f)
    }

    @Test
    fun getCourseById_fallbackToLocalRoom_whenNetworkFails() = runTest {
        // Given existing cached course in Room
        val localCourse = CourseEntity(
            id = "c_offline_01",
            title = "دورة بدون اتصال",
            description = "محتوى محلي مسبق الحفظ",
            category = "General",
            totalLessons = 4,
            durationMinutes = 45,
            iconUrl = null,
            isDownloaded = true,
            progressPercent = 50.0f,
            isEnrolled = true,
            isBookmarked = true,
            schoolId = "sch_1"
        )
        fakeCourseDao.insertCourse(localCourse)

        // Network returns 500 error
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"error":"Internal Server Error"}""")
        )

        val repository = CourseRepositoryImpl(
            courseDao = fakeCourseDao,
            lessonDao = fakeLessonDao,
            apiService = apiService
        )

        // When observing getCourseById
        val course = repository.getCourseById("c_offline_01").filterNotNull().first()

        // Then verify request was attempted with correct path and headers
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("GET", recordedRequest.method)
        assertEquals("/api/v1/courses/c_offline_01", recordedRequest.path)
        assertEquals("Bearer test_jwt_bearer_token_123", recordedRequest.getHeader("Authorization"))
        assertEquals("00000000-0000-0000-0000-000000000001", recordedRequest.getHeader("X-Tenant-Id"))

        // And verify fallback to Room cached course
        assertEquals("c_offline_01", course.id)
        assertEquals("دورة بدون اتصال", course.title)
        assertEquals("محتوى محلي مسبق الحفظ", course.description)
        assertEquals(4, course.totalLessons)
        assertEquals(true, course.isEnrolled)
        assertEquals(true, course.isBookmarked)
    }

    @Test
    fun getCourseById_handles404NotFound_preservesLocalState() = runTest {
        // Server returns 404
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("""{"error":"Course not found"}""")
        )

        val repository = CourseRepositoryImpl(
            courseDao = fakeCourseDao,
            lessonDao = fakeLessonDao,
            apiService = apiService
        )

        // When course does not exist locally either
        val course = repository.getCourseById("c_nonexistent").first()

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("GET", recordedRequest.method)
        assertEquals("/api/v1/courses/c_nonexistent", recordedRequest.path)
        org.junit.Assert.assertNull(course)
    }

    @Test
    fun getLessonById_callsEndpoint_withJwtAndTenantId_andUpdatesRepositoryAndRoom() = runTest {
        // Given existing cached lesson in Room stub
        fakeLessonDao.insertLesson(
            LessonEntity(
                id = "l_ai_101_1",
                courseId = "c_ai_101",
                title = "عنوان أولي",
                content = "محتوى أولي",
                order = 1,
                isCompleted = false,
                audioUrl = null
            )
        )

        val ktorJsonResponse = """
            {
              "id": "l_ai_101_1",
              "courseId": "c_ai_101",
              "title": "مقدمة في الشبكات العصبية",
              "content": "شرح مفصل للشبكات العصبية والخوارزميات",
              "moduleOrder": 1,
              "estimatedMinutes": 15,
              "isCompleted": true
            }
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

        // When observing getLessonById
        val lessonFlow = repository.getLessonById("l_ai_101_1")

        // Wait until remote fetch updates and flow emits updated lesson
        val lesson = lessonFlow.filter { it?.content == "شرح مفصل للشبكات العصبية والخوارزميات" }.first()

        // Then verify request sent to MockWebServer
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("GET", recordedRequest.method)
        assertEquals("/api/v1/courses/c_ai_101/lessons/l_ai_101_1", recordedRequest.path)
        assertEquals("Bearer test_jwt_bearer_token_123", recordedRequest.getHeader("Authorization"))
        assertEquals("00000000-0000-0000-0000-000000000001", recordedRequest.getHeader("X-Tenant-Id"))

        // Then verify repository received and mapped lesson correctly
        assertNotNull(lesson)
        assertEquals("l_ai_101_1", lesson?.id)
        assertEquals("c_ai_101", lesson?.courseId)
        assertEquals("مقدمة في الشبكات العصبية", lesson?.title)
        assertEquals("شرح مفصل للشبكات العصبية والخوارزميات", lesson?.content)
        assertEquals(true, lesson?.isCompleted)

        // And verify Room database is updated
        val roomEntity = fakeLessonDao.getLessonById("l_ai_101_1")
        assertNotNull(roomEntity)
        assertEquals("شرح مفصل للشبكات العصبية والخوارزميات", roomEntity?.content)
        assertEquals(true, roomEntity?.isCompleted)
    }

    @Test
    fun getLessonById_fallbackToLocalRoom_whenNetworkFails() = runTest {
        // Given existing cached lesson in Room
        fakeLessonDao.insertLesson(
            LessonEntity(
                id = "l_offline_1",
                courseId = "c_ai_101",
                title = "درس مسبق الحفظ",
                content = "محتوى محلي غير متصل",
                order = 2,
                isCompleted = true,
                audioUrl = null
            )
        )

        // Network returns 500 error
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"error":"Internal Server Error"}""")
        )

        val repository = CourseRepositoryImpl(
            courseDao = fakeCourseDao,
            lessonDao = fakeLessonDao,
            apiService = apiService
        )

        // When observing getLessonById
        val lesson = repository.getLessonById("l_offline_1").filterNotNull().first()

        // Then verify request was attempted with correct path and headers
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("GET", recordedRequest.method)
        assertEquals("/api/v1/courses/c_ai_101/lessons/l_offline_1", recordedRequest.path)
        assertEquals("Bearer test_jwt_bearer_token_123", recordedRequest.getHeader("Authorization"))
        assertEquals("00000000-0000-0000-0000-000000000001", recordedRequest.getHeader("X-Tenant-Id"))

        // And verify fallback to Room cached lesson
        assertEquals("l_offline_1", lesson.id)
        assertEquals("درس مسبق الحفظ", lesson.title)
        assertEquals("محتوى محلي غير متصل", lesson.content)
        assertEquals(true, lesson.isCompleted)
    }

    @Test
    fun getLessonById_preservesCompletedStatusWhenAlreadyCompletedLocally() = runTest {
        // Local lesson was already marked complete
        fakeLessonDao.insertLesson(
            LessonEntity(
                id = "l_ai_101_2",
                courseId = "c_ai_101",
                title = "الدرس الثاني",
                content = "محتوى سابق",
                order = 2,
                isCompleted = true,
                audioUrl = null
            )
        )

        // Remote response returns isCompleted = false (e.g. out-of-sync backend)
        val ktorJsonResponse = """
            {
              "id": "l_ai_101_2",
              "courseId": "c_ai_101",
              "title": "الدرس الثاني المحدث",
              "content": "محتوى محدث من الخادم",
              "moduleOrder": 2,
              "estimatedMinutes": 15,
              "isCompleted": false
            }
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

        // When observing getLessonById
        val lesson = repository.getLessonById("l_ai_101_2")
            .filter { it?.content == "محتوى محدث من الخادم" }
            .first()

        // Then verify isCompleted remains true
        assertNotNull(lesson)
        assertEquals(true, lesson?.isCompleted)
        val roomEntity = fakeLessonDao.getLessonById("l_ai_101_2")
        assertEquals(true, roomEntity?.isCompleted)
    }

    @Test
    fun getLessonById_withDirectCourseId_fetchesAndInsertsWhenNotLocallyCached() = runTest {
        // Lesson does not exist locally yet
        val ktorJsonResponse = """
            {
              "id": "l_ai_brand_new",
              "courseId": "c_ai_101",
              "title": "درس جديد تماما",
              "content": "محتوى جديد لم يسبق حفظه",
              "moduleOrder": 5,
              "estimatedMinutes": 15,
              "isCompleted": false
            }
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

        // When calling getLessonById with courseId and lessonId
        val lesson = repository.getLessonById("c_ai_101", "l_ai_brand_new")
            .filterNotNull()
            .first()

        // Then verify request sent
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/api/v1/courses/c_ai_101/lessons/l_ai_brand_new", recordedRequest.path)

        // Then verify lesson was inserted into Room and mapped correctly
        assertEquals("l_ai_brand_new", lesson.id)
        assertEquals("درس جديد تماما", lesson.title)
        assertEquals("محتوى جديد لم يسبق حفظه", lesson.content)
        assertEquals(false, lesson.isCompleted)
    }

    @Test
    fun updateLessonProgress_callsEndpoint_withCompletedAndScore_andUpdatesRoom() = runTest {
        fakeCourseDao.insertCourse(
            CourseEntity(
                id = "c_ai_101",
                title = "ذكاء اصطناعي",
                description = "وصف",
                category = "Technology",
                totalLessons = 4,
                durationMinutes = 60,
                iconUrl = null,
                isDownloaded = false,
                progressPercent = 0.25f,
                isEnrolled = true
            )
        )
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

        val ktorJsonResponse = """
            {
              "success": true,
              "lessonId": "l_ai_101_1",
              "courseId": "c_ai_101",
              "completed": true,
              "courseProgressPercent": 100.0,
              "completedLessons": 4,
              "totalLessons": 4
            }
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

        // When updating progress to 100%
        val result = repository.updateLessonProgress("l_ai_101_1", "c_ai_101", 1.0f)

        // Then verify request method, path, headers, and body
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertEquals("/api/v1/courses/c_ai_101/lessons/l_ai_101_1/progress", recordedRequest.path)
        assertEquals("Bearer test_jwt_bearer_token_123", recordedRequest.getHeader("Authorization"))
        assertEquals("00000000-0000-0000-0000-000000000001", recordedRequest.getHeader("X-Tenant-Id"))

        val requestBody = recordedRequest.body.readUtf8()
        assertTrue(requestBody.contains(""""completed":true"""))
        assertTrue(requestBody.contains(""""score":100"""))

        // Then verify result is success
        assertTrue(result is com.rtiqa.core.domain.result.RtiqaResult.Success)

        // Then verify Room state was updated
        val updatedLesson = fakeLessonDao.getLessonById("l_ai_101_1")
        assertNotNull(updatedLesson)
        assertTrue(updatedLesson!!.isCompleted)

        val updatedCourse = fakeCourseDao.courseStateFlow.value.find { it.id == "c_ai_101" }
        assertNotNull(updatedCourse)
        assertEquals(1.0f, updatedCourse!!.progressPercent, 0.01f)
        assertTrue(updatedCourse.isEnrolled)
    }

    @Test
    fun updateLessonProgress_preservesIsCompleted_whenAlreadyCompleted() = runTest {
        fakeCourseDao.insertCourse(
            CourseEntity(
                id = "c_ai_101",
                title = "ذكاء اصطناعي",
                description = "وصف",
                category = "Technology",
                totalLessons = 4,
                durationMinutes = 60,
                iconUrl = null,
                isDownloaded = false,
                progressPercent = 0.5f,
                isEnrolled = true
            )
        )
        fakeLessonDao.insertLesson(
            LessonEntity(
                id = "l_ai_101_1",
                courseId = "c_ai_101",
                title = "مقدمة",
                content = "محتوى",
                order = 1,
                isCompleted = true,
                audioUrl = null
            )
        )

        val ktorJsonResponse = """
            {
              "success": true,
              "lessonId": "l_ai_101_1",
              "courseId": "c_ai_101",
              "completed": true,
              "courseProgressPercent": 50.0,
              "completedLessons": 2,
              "totalLessons": 4
            }
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

        // Partial progress update (0.4f), but lesson was already completed
        val result = repository.updateLessonProgress("l_ai_101_1", "c_ai_101", 0.4f)

        val recordedRequest = mockWebServer.takeRequest()
        val requestBody = recordedRequest.body.readUtf8()
        assertTrue(requestBody.contains(""""completed":true"""))
        assertTrue(requestBody.contains(""""score":40"""))

        assertTrue(result is com.rtiqa.core.domain.result.RtiqaResult.Success)

        val updatedLesson = fakeLessonDao.getLessonById("l_ai_101_1")
        assertNotNull(updatedLesson)
        assertTrue(updatedLesson!!.isCompleted)
    }

    @Test
    fun updateLessonProgress_doesNotCreateDuplicateRecords() = runTest {
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

        val ktorJsonResponse = """
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

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(ktorJsonResponse))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(ktorJsonResponse))

        val repository = CourseRepositoryImpl(
            courseDao = fakeCourseDao,
            lessonDao = fakeLessonDao,
            apiService = apiService
        )

        // Call twice
        repository.updateLessonProgress("l_ai_101_1", "c_ai_101", 0.5f)
        repository.updateLessonProgress("l_ai_101_1", "c_ai_101", 1.0f)

        // Verify lessons count in Room is still 1 (no duplicates)
        val lessons = fakeLessonDao.getLessonsForCourseList("c_ai_101")
        assertEquals(1, lessons.size)
        assertEquals("l_ai_101_1", lessons[0].id)
    }

    @Test
    fun updateLessonProgress_fallsBackToLocalRoom_onNetworkFailure() = runTest {
        fakeCourseDao.insertCourse(
            CourseEntity(
                id = "c_ai_101",
                title = "ذكاء اصطناعي",
                description = "وصف",
                category = "Technology",
                totalLessons = 2,
                durationMinutes = 60,
                iconUrl = null,
                isDownloaded = false,
                progressPercent = 0.0f
            )
        )
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

        // Server responds with 500 error
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"error":"Internal Server Error"}""")
        )

        var enqueuedAction: String? = null
        val fakeOfflineSync = object : com.rtiqa.core.domain.repository.OfflineSyncContract {
            override suspend fun syncRemoteCourses(): com.rtiqa.core.domain.result.RtiqaResult<Unit> =
                com.rtiqa.core.domain.result.RtiqaResult.Success(Unit)

            override suspend fun enqueueOfflineAction(actionType: String, payloadJson: String): com.rtiqa.core.domain.result.RtiqaResult<Unit> {
                enqueuedAction = actionType
                return com.rtiqa.core.domain.result.RtiqaResult.Success(Unit)
            }

            override fun observePendingSyncCount(): Flow<Int> = flowOf(0)
        }

        val repository = CourseRepositoryImpl(
            courseDao = fakeCourseDao,
            lessonDao = fakeLessonDao,
            apiService = apiService,
            offlineSyncManager = fakeOfflineSync
        )

        // When network fails, method must not crash and fallback to local Room
        val result = repository.updateLessonProgress("l_ai_101_1", "c_ai_101", 1.0f)

        // Then verify result is success (local persistence succeeded)
        assertTrue(result is com.rtiqa.core.domain.result.RtiqaResult.Success)

        // Verify Room updated locally
        val lesson = fakeLessonDao.getLessonById("l_ai_101_1")
        assertNotNull(lesson)
        assertTrue(lesson!!.isCompleted)

        val course = fakeCourseDao.courseStateFlow.value.find { it.id == "c_ai_101" }
        assertNotNull(course)
        assertEquals(1.0f, course!!.progressPercent, 0.01f)

        // Verify offline sync action was queued
        assertEquals("LESSON_PROGRESS_UPDATE", enqueuedAction)
    }

    @Test
    fun getLessonById_serverAudioUrl_reachesStoredRoomEntityAndDomainLesson() = runTest {
        val ktorJsonResponse = """
            {
              "id": "l_audio_101",
              "courseId": "c_ai_101",
              "title": "درس الذكاء الاصطناعي مع صوت",
              "content": "شرح الدرس المرفق به مقطع صوتي",
              "moduleOrder": 1,
              "estimatedMinutes": 15,
              "isCompleted": false,
              "audioUrl": "https://example.com/audio/server_ai_101.mp3"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(ktorJsonResponse)
                .addHeader("Content-Type", "application/json")
        )

        // Seed course in fakeCourseDao so findCourseIdForLesson can locate it if needed
        fakeCourseDao.insertCourse(
            CourseEntity(
                id = "c_ai_101",
                title = "ذكاء اصطناعي",
                description = "مقدمة",
                category = "AI",
                totalLessons = 1,
                durationMinutes = 60,
                iconUrl = null,
                isDownloaded = false,
                progressPercent = 0f,
                isEnrolled = true
            )
        )

        val repository = CourseRepositoryImpl(
            courseDao = fakeCourseDao,
            lessonDao = fakeLessonDao,
            apiService = apiService
        )

        // Observe lesson flow until audioUrl is emitted from remote fetch
        val lessonFlow = repository.getLessonById(courseId = "c_ai_101", lessonId = "l_audio_101")
        val lesson = lessonFlow.filter { it?.audioUrl != null }.first()

        assertNotNull(lesson)
        assertEquals("https://example.com/audio/server_ai_101.mp3", lesson?.audioUrl)

        // Verify stored in Room database entity as well
        val storedEntity = fakeLessonDao.getLessonById("l_audio_101")
        assertNotNull(storedEntity)
        assertEquals("https://example.com/audio/server_ai_101.mp3", storedEntity?.audioUrl)
    }

    @Test
    fun getLessonById_preservesExistingLocalAudioUrl_whenServerReturnsNoAudioUrl() = runTest {
        // Given existing local cached lesson with audioUrl
        fakeLessonDao.insertLesson(
            LessonEntity(
                id = "l_cached_01",
                courseId = "c_ai_101",
                title = "عنوان قديم",
                content = "محتوى قديم",
                order = 1,
                isCompleted = false,
                audioUrl = "https://example.com/audio/local_cached.mp3"
            )
        )

        // When server returns updated title but omits audioUrl
        val ktorJsonResponse = """
            {
              "id": "l_cached_01",
              "courseId": "c_ai_101",
              "title": "عنوان محدث من السيرفر",
              "content": "محتوى محدث من السيرفر",
              "moduleOrder": 1,
              "estimatedMinutes": 15,
              "isCompleted": false
            }
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

        val lessonFlow = repository.getLessonById("l_cached_01")
        val lesson = lessonFlow.filter { it?.title == "عنوان محدث من السيرفر" }.first()

        // Then verify local audioUrl is preserved in both Domain and Room
        assertNotNull(lesson)
        assertEquals("https://example.com/audio/local_cached.mp3", lesson?.audioUrl)

        val storedEntity = fakeLessonDao.getLessonById("l_cached_01")
        assertNotNull(storedEntity)
        assertEquals("https://example.com/audio/local_cached.mp3", storedEntity?.audioUrl)
    }

    @Test
    fun getCourseLessons_serverAudioUrl_reachesStoredAndDomainLessons() = runTest {
        val ktorJsonResponse = """
            [
              {
                "id": "l_course_audio_1",
                "courseId": "c_course_audio",
                "title": "الدرس الصوتي الأول",
                "content": "محتوى الدرس الصوتي",
                "moduleOrder": 1,
                "estimatedMinutes": 10,
                "audioUrl": "https://example.com/audio/course_stream.mp3"
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

        val lessonsFlow = repository.getLessonsForCourse("c_course_audio")
        val lessons = lessonsFlow.filter { it.isNotEmpty() && it[0].audioUrl != null }.first()

        assertEquals(1, lessons.size)
        assertEquals("https://example.com/audio/course_stream.mp3", lessons[0].audioUrl)

        val storedLessons = fakeLessonDao.getLessonsForCourseList("c_course_audio")
        assertEquals(1, storedLessons.size)
        assertEquals("https://example.com/audio/course_stream.mp3", storedLessons[0].audioUrl)
    }

    @Test
    fun remoteCourses_withNoSchoolIdentity_areNotStoredAsSchool001_andHaveNullSchoolId() = runTest {
        val ktorJsonResponse = """
            [
              {
                "id": "c_no_school_1",
                "title": "دورة عامة بدون مدرسة",
                "description": "وصف الدورة",
                "category": "Technology",
                "difficulty": "BEGINNER",
                "totalModules": 3,
                "completedModules": 0,
                "progressPercent": 0.0
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

        val coursesFlow = repository.getCourses()
        val courses = coursesFlow.filter { it.isNotEmpty() }.first()

        assertEquals(1, courses.size)
        val course = courses[0]
        assertNull(course.schoolId)
        assertNotEquals("school_001", course.schoolId)

        val stored = fakeCourseDao.getAllCoursesList().find { it.id == "c_no_school_1" }
        assertNotNull(stored)
        assertNull(stored?.schoolId)
        assertNotEquals("school_001", stored?.schoolId)
    }

    @Test
    fun remoteLesson_withNoSchoolIdentity_isNotAssignedSchool001_andHasNullSchoolId() = runTest {
        val ktorJsonResponse = """
            {
              "id": "l_no_school_1",
              "courseId": "c_no_school_1",
              "title": "درس بدون مدرسة",
              "content": "محتوى الدرس",
              "moduleOrder": 1,
              "estimatedMinutes": 10,
              "isCompleted": false
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(ktorJsonResponse)
                .addHeader("Content-Type", "application/json")
        )

        fakeCourseDao.insertCourse(
            CourseEntity(
                id = "c_no_school_1",
                title = "دورة عامة",
                description = "وصف",
                category = "Technology",
                totalLessons = 1,
                durationMinutes = 10,
                iconUrl = null,
                isDownloaded = false,
                progressPercent = 0f,
                isEnrolled = true,
                schoolId = null
            )
        )

        val repository = CourseRepositoryImpl(
            courseDao = fakeCourseDao,
            lessonDao = fakeLessonDao,
            apiService = apiService
        )

        val lessonFlow = repository.getLessonById(courseId = "c_no_school_1", lessonId = "l_no_school_1")
        val lesson = lessonFlow.filterNotNull().first()

        assertNull(lesson.schoolId)
        assertNotEquals("school_001", lesson.schoolId)

        val stored = fakeLessonDao.getLessonById("l_no_school_1")
        assertNotNull(stored)
        assertNull(stored?.schoolId)
        assertNotEquals("school_001", stored?.schoolId)
    }

    @Test
    fun remoteRefresh_preservesExistingLocalSchoolId_whenServerPayloadHasNoSchoolIdentity() = runTest {
        fakeCourseDao.insertCourse(
            CourseEntity(
                id = "c_legit_school",
                title = "دورة خاصة بمدرسة النور",
                description = "وصف قديم",
                category = "Science",
                totalLessons = 2,
                durationMinutes = 45,
                iconUrl = null,
                isDownloaded = false,
                progressPercent = 0f,
                isEnrolled = true,
                schoolId = "school_al_noor_999"
            )
        )
        fakeLessonDao.insertLesson(
            LessonEntity(
                id = "l_legit_school",
                courseId = "c_legit_school",
                title = "الدرس الأول القديم",
                content = "محتوى محلي",
                order = 1,
                isCompleted = false,
                audioUrl = "https://example.com/audio/al_noor_intro.mp3",
                schoolId = "school_al_noor_999"
            )
        )

        val ktorJsonResponse = """
            {
              "id": "l_legit_school",
              "courseId": "c_legit_school",
              "title": "الدرس الأول المحدث من السيرفر",
              "content": "محتوى محدث من السيرفر بدون حقل مدرسة",
              "moduleOrder": 1,
              "estimatedMinutes": 20,
              "isCompleted": false
            }
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

        val lessonFlow = repository.getLessonById(courseId = "c_legit_school", lessonId = "l_legit_school")
        val lesson = lessonFlow.filter { it?.title == "الدرس الأول المحدث من السيرفر" }.first()

        assertNotNull(lesson)
        assertEquals("school_al_noor_999", lesson?.schoolId)
        assertNotEquals("school_001", lesson?.schoolId)
        assertEquals("https://example.com/audio/al_noor_intro.mp3", lesson?.audioUrl)

        val stored = fakeLessonDao.getLessonById("l_legit_school")
        assertNotNull(stored)
        assertEquals("school_al_noor_999", stored?.schoolId)
        assertEquals("https://example.com/audio/al_noor_intro.mp3", stored?.audioUrl)
    }
}
