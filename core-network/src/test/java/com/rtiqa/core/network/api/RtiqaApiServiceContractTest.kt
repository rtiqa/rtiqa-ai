package com.rtiqa.core.network.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

class RtiqaApiServiceContractTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: RtiqaApiService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val okHttpClient = OkHttpClient.Builder().build()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RtiqaApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun getCourse_sendsCorrectPathAndReturnsCourseDetails() = runTest {
        val courseJsonResponse = """
            {
              "id": "c_ai_101",
              "title": "ذكاء اصطناعي",
              "description": "مقدمة شاملة",
              "category": "AI",
              "difficulty": "Beginner",
              "totalModules": 8,
              "completedModules": 2,
              "progressPercent": 25.0
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(courseJsonResponse)
                .addHeader("Content-Type", "application/json")
        )

        val response = apiService.getCourse("c_ai_101")
        val recordedRequest = mockWebServer.takeRequest()

        assertEquals("GET", recordedRequest.method)
        assertEquals("/api/v1/courses/c_ai_101", recordedRequest.path)
        assertTrue(response.isSuccessful)

        val course = response.body()
        assertNotNull(course)
        assertEquals("c_ai_101", course?.id)
        assertEquals("ذكاء اصطناعي", course?.title)
        assertEquals(8, course?.totalModules)
        assertEquals(25.0f, course?.progressPercent ?: 0f, 0.01f)
    }

    @Test
    fun getLesson_sendsCorrectPathAndReturnsLessonDetails() = runTest {
        val lessonJsonResponse = """
            {
              "id": "l_mod_01",
              "courseId": "c_ai_101",
              "title": "مقدمة في الشبكات العصبية",
              "content": "محتوى الدرس التدريبي",
              "moduleOrder": 1,
              "estimatedMinutes": 20,
              "isCompleted": false
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(lessonJsonResponse)
                .addHeader("Content-Type", "application/json")
        )

        val response = apiService.getLesson(courseId = "c_ai_101", lessonId = "l_mod_01")
        val recordedRequest = mockWebServer.takeRequest()

        assertEquals("GET", recordedRequest.method)
        assertEquals("/api/v1/courses/c_ai_101/lessons/l_mod_01", recordedRequest.path)
        assertTrue(response.isSuccessful)

        val lesson = response.body()
        assertNotNull(lesson)
        assertEquals("l_mod_01", lesson?.id)
        assertEquals("c_ai_101", lesson?.courseId)
        assertEquals("مقدمة في الشبكات العصبية", lesson?.title)
        assertEquals(1, lesson?.moduleOrder)
        assertEquals(20, lesson?.estimatedMinutes)
        assertEquals(false, lesson?.isCompleted)
    }

    @Test
    fun updateLessonProgress_sendsPostWithBodyAndReturnsProgressResponse() = runTest {
        val progressJsonResponse = """
            {
              "success": true,
              "lessonId": "l_mod_01",
              "courseId": "c_ai_101",
              "completed": true,
              "courseProgressPercent": 50.0,
              "completedLessons": 4,
              "totalLessons": 8
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(progressJsonResponse)
                .addHeader("Content-Type", "application/json")
        )

        val requestPayload = LessonProgressRequestDto(completed = true, score = 88)
        val response = apiService.updateLessonProgress(
            courseId = "c_ai_101",
            lessonId = "l_mod_01",
            request = requestPayload
        )
        val recordedRequest = mockWebServer.takeRequest()

        assertEquals("POST", recordedRequest.method)
        assertEquals("/api/v1/courses/c_ai_101/lessons/l_mod_01/progress", recordedRequest.path)

        val recordedBody = recordedRequest.body.readUtf8()
        assertTrue(recordedBody.contains(""""completed":true"""))
        assertTrue(recordedBody.contains(""""score":88"""))

        assertTrue(response.isSuccessful)
        val result = response.body()
        assertNotNull(result)
        assertTrue(result?.success == true)
        assertEquals("l_mod_01", result?.lessonId)
        assertEquals("c_ai_101", result?.courseId)
        assertTrue(result?.completed == true)
        assertEquals(50.0f, result?.courseProgressPercent ?: 0f, 0.01f)
        assertEquals(4, result?.completedLessons)
        assertEquals(8, result?.totalLessons)
    }

    @Test
    fun updateLessonProgress_worksWithDefaultRequestPayload() = runTest {
        val progressJsonResponse = """
            {
              "success": true,
              "lessonId": "l_mod_02",
              "courseId": "c_ai_101",
              "completed": true,
              "courseProgressPercent": 62.5,
              "completedLessons": 5,
              "totalLessons": 8
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(progressJsonResponse)
                .addHeader("Content-Type", "application/json")
        )

        val response = apiService.updateLessonProgress(
            courseId = "c_ai_101",
            lessonId = "l_mod_02"
        )
        val recordedRequest = mockWebServer.takeRequest()

        assertEquals("POST", recordedRequest.method)
        assertEquals("/api/v1/courses/c_ai_101/lessons/l_mod_02/progress", recordedRequest.path)

        val recordedBody = recordedRequest.body.readUtf8()
        assertTrue(recordedBody.contains(""""completed":true"""))

        assertTrue(response.isSuccessful)
        val result = response.body()
        assertNotNull(result)
        assertEquals(62.5f, result?.courseProgressPercent ?: 0f, 0.01f)
    }
}
