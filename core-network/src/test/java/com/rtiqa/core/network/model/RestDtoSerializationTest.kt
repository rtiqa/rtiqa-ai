package com.rtiqa.core.network.model

import com.rtiqa.core.network.api.LessonProgressRequestDto
import com.rtiqa.core.network.api.LessonProgressResponseDto
import com.rtiqa.core.network.api.AuthResponseDto
import com.rtiqa.core.network.api.NetworkCourseDto
import com.rtiqa.core.network.api.NetworkLessonDto
import com.rtiqa.core.network.api.RestLoginRequest
import com.rtiqa.core.network.RetrofitNetworkClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RestDtoSerializationTest {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun `AuthResponseDto parses organization id with production Moshi configuration`() {
        val json = """
            {
              "token": "server-token",
              "organization_id": "org-42",
              "user": {
                "id": "u1",
                "email": "user@example.com",
                "name": "User",
                "streakCount": 1,
                "totalXp": 100
              }
            }
        """.trimIndent()

        val adapter = RetrofitNetworkClient.createMoshi().adapter(AuthResponseDto::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals("server-token", response?.token)
        assertEquals("org-42", response?.organizationId)
        assertEquals("u1", response?.user?.id)
    }

    @Test
    fun `DTO serialization and deserialization works`() {
        val adapter = moshi.adapter(RestLoginRequest::class.java)
        val request = RestLoginRequest("test@rtiqa.com", "secure123")
        val json = adapter.toJson(request)
        val expectedJson = """{"email":"test@rtiqa.com","password":"secure123"}"""
        assertEquals(expectedJson, json)
        val deserialized = adapter.fromJson(json)
        assertEquals(request.email, deserialized?.email)
        assertEquals(request.password, deserialized?.password)
    }

    @Test
    fun `NetworkCourseDto deserializes correctly matching Ktor response`() {
        val ktorJson = """
            {
              "id": "c_ai_101",
              "title": "ذكاء اصطناعي 101",
              "description": "مقدمة شاملة لعلم البيانات والذكاء الاصطناعي",
              "category": "Technology",
              "difficulty": "Intermediate",
              "totalModules": 12,
              "completedModules": 4,
              "progressPercent": 33.33
            }
        """.trimIndent()

        val adapter = moshi.adapter(NetworkCourseDto::class.java)
        val dto = adapter.fromJson(ktorJson)

        assertNotNull(dto)
        assertEquals("c_ai_101", dto?.id)
        assertEquals("ذكاء اصطناعي 101", dto?.title)
        assertEquals("مقدمة شاملة لعلم البيانات والذكاء الاصطناعي", dto?.description)
        assertEquals("Technology", dto?.category)
        assertEquals("Intermediate", dto?.difficulty)
        assertEquals(12, dto?.totalModules)
        assertEquals(4, dto?.completedModules)
        assertEquals(33.33f, dto?.progressPercent ?: 0f, 0.01f)
    }

    @Test
    fun `NetworkLessonDto deserializes correctly matching Ktor response`() {
        val ktorJson = """
            {
              "id": "l_mod_01",
              "courseId": "c_ai_101",
              "title": "مقدمة في الشبكات العصبية",
              "content": "شرح مبسط للبنية الأساسية للشبكة العصبية",
              "moduleOrder": 1,
              "estimatedMinutes": 25,
              "isCompleted": true,
              "audioUrl": "https://example.com/audio/lesson1.mp3"
            }
        """.trimIndent()

        val adapter = moshi.adapter(NetworkLessonDto::class.java)
        val dto = adapter.fromJson(ktorJson)

        assertNotNull(dto)
        assertEquals("l_mod_01", dto?.id)
        assertEquals("c_ai_101", dto?.courseId)
        assertEquals("مقدمة في الشبكات العصبية", dto?.title)
        assertEquals("شرح مبسط للبنية الأساسية للشبكة العصبية", dto?.content)
        assertEquals(1, dto?.moduleOrder)
        assertEquals(25, dto?.estimatedMinutes)
        assertTrue(dto?.isCompleted == true)
        assertEquals("https://example.com/audio/lesson1.mp3", dto?.audioUrl)
    }

    @Test
    fun `NetworkLessonDto defaults isCompleted to false when field is omitted`() {
        val ktorJson = """
            {
              "id": "l_mod_02",
              "courseId": "c_ai_101",
              "title": "التعلم العميق",
              "content": "مفاهيم التعلم العميق",
              "moduleOrder": 2,
              "estimatedMinutes": 30
            }
        """.trimIndent()

        val adapter = moshi.adapter(NetworkLessonDto::class.java)
        val dto = adapter.fromJson(ktorJson)

        assertNotNull(dto)
        assertEquals("l_mod_02", dto?.id)
        assertFalse(dto?.isCompleted ?: true)
        assertNull(dto?.audioUrl)
    }

    @Test
    fun `LessonProgressRequestDto serializes and deserializes correctly with default and custom values`() {
        val adapter = moshi.adapter(LessonProgressRequestDto::class.java)

        // Custom values
        val customRequest = LessonProgressRequestDto(completed = true, score = 95)
        val json = adapter.toJson(customRequest)
        assertTrue(json.contains(""""completed":true"""))
        assertTrue(json.contains(""""score":95"""))

        val deserializedCustom = adapter.fromJson(json)
        assertNotNull(deserializedCustom)
        assertEquals(true, deserializedCustom?.completed)
        assertEquals(95, deserializedCustom?.score)

        // Default values
        val defaultRequest = LessonProgressRequestDto()
        assertEquals(true, defaultRequest.completed)
        assertNull(defaultRequest.score)

        // From Ktor JSON with score omitted
        val ktorJsonWithoutScore = """{"completed":true}"""
        val deserializedFromKtor = adapter.fromJson(ktorJsonWithoutScore)
        assertNotNull(deserializedFromKtor)
        assertEquals(true, deserializedFromKtor?.completed)
        assertNull(deserializedFromKtor?.score)

        // Empty JSON object
        val emptyJson = "{}"
        val deserializedEmpty = adapter.fromJson(emptyJson)
        assertNotNull(deserializedEmpty)
        assertEquals(true, deserializedEmpty?.completed)
        assertNull(deserializedEmpty?.score)
    }

    @Test
    fun `LessonProgressResponseDto deserializes correctly matching Ktor LessonCompletionResponseDto`() {
        val ktorJson = """
            {
              "success": true,
              "lessonId": "l_mod_01",
              "courseId": "c_ai_101",
              "completed": true,
              "courseProgressPercent": 50.0,
              "completedLessons": 6,
              "totalLessons": 12
            }
        """.trimIndent()

        val adapter = moshi.adapter(LessonProgressResponseDto::class.java)
        val dto = adapter.fromJson(ktorJson)

        assertNotNull(dto)
        assertTrue(dto?.success == true)
        assertEquals("l_mod_01", dto?.lessonId)
        assertEquals("c_ai_101", dto?.courseId)
        assertTrue(dto?.completed == true)
        assertEquals(50.0f, dto?.courseProgressPercent ?: 0f, 0.01f)
        assertEquals(6, dto?.completedLessons)
        assertEquals(12, dto?.totalLessons)
    }
}
