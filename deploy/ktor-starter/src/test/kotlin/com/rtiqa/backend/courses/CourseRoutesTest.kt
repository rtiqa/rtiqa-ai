package com.rtiqa.backend.courses

import com.rtiqa.backend.auth.EnterpriseRole
import com.rtiqa.backend.auth.TenantContext
import com.rtiqa.backend.auth.tenantAuthorization
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.util.UUID

class CourseRoutesTest {

    private fun Application.testCourseModule(
        mockSubject: String?,
        mockRole: EnterpriseRole?,
        mockRepository: CourseRepository,
        mockTransactionRunner: (suspend (TenantContext, suspend (Connection) -> Any?) -> Any?)? = null
    ) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Authentication) {
            provider("auth-jwt") {
                authenticate { context ->
                    if (mockSubject != null) {
                        val principal = JWTPrincipal(com.auth0.jwt.interfaces.Payload::class.java.cast(
                            object : java.lang.reflect.InvocationHandler {
                                override fun invoke(proxy: Any?, method: java.lang.reflect.Method?, args: Array<out Any>?): Any? {
                                    if (method?.name == "getSubject") return mockSubject
                                    return null
                                }
                            }.let { java.lang.reflect.Proxy.newProxyInstance(
                                this::class.java.classLoader,
                                arrayOf(com.auth0.jwt.interfaces.Payload::class.java),
                                it
                            ) }
                        ))
                        context.principal(principal)
                    }
                }
            }
        }

        routing {
            courseRoutes(
                courseRepository = mockRepository,
                authProvider = "auth-jwt",
                checkMembership = { _, _ -> mockRole },
                transactionRunner = mockTransactionRunner
            )
        }
    }

    @Test
    fun `GET courses without JWT returns 401`() = testApplication {
        application {
            testCourseModule(
                mockSubject = null,
                mockRole = null,
                mockRepository = CourseRepository()
            )
        }

        val response = client.get("/api/v1/courses") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET courses without X-Tenant-ID returns 400`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository()
            )
        }

        val response = client.get("/api/v1/courses")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET courses with invalid membership returns 403`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = null,
                mockRepository = CourseRepository()
            )
        }

        val response = client.get("/api/v1/courses") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `GET courses returns 200 with list of courses`() = testApplication {
        val sampleCourses = listOf(
            CourseResponseDto(
                id = UUID.randomUUID().toString(),
                title = "ذكاء اصطناعي",
                description = "مقدمة في الذكاء الاصطناعي",
                category = "الذكاء الاصطناعي والبيانات",
                difficulty = "مبتدئ",
                totalModules = 5,
                completedModules = 2,
                progressPercent = 40.0f
            )
        )

        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> sampleCourses }
            )
        }

        val response = client.get("/api/v1/courses") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("ذكاء اصطناعي"))
    }

    @Test
    fun `GET lessons returns 400 for malformed course UUID`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository()
            )
        }

        val response = client.get("/api/v1/courses/not-a-uuid/lessons") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET lessons returns 404 when course does not exist in tenant`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> null }
            )
        }

        val response = client.get("/api/v1/courses/${UUID.randomUUID()}/lessons") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET lessons returns 200 with list of lessons when course exists`() = testApplication {
        val courseId = UUID.randomUUID().toString()
        val sampleLessons = listOf(
            LessonResponseDto(
                id = UUID.randomUUID().toString(),
                courseId = courseId,
                title = "الدرس الأول: المفاهيم الأساسية",
                content = "محتوى الدرس...",
                moduleOrder = 1,
                estimatedMinutes = 20
            )
        )

        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> sampleLessons }
            )
        }

        val response = client.get("/api/v1/courses/$courseId/lessons") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("الدرس الأول: المفاهيم الأساسية"))
    }

    @Test
    fun `GET courses returns 500 on database error`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> throw RuntimeException("DB Connection down") }
            )
        }

        val response = client.get("/api/v1/courses") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    @Test
    fun `POST complete lesson without JWT returns 401`() = testApplication {
        application {
            testCourseModule(
                mockSubject = null,
                mockRole = null,
                mockRepository = CourseRepository()
            )
        }

        val response = client.post("/api/v1/courses/${UUID.randomUUID()}/lessons/${UUID.randomUUID()}/complete") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST complete lesson without X-Tenant-ID returns 400`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository()
            )
        }

        val response = client.post("/api/v1/courses/${UUID.randomUUID()}/lessons/${UUID.randomUUID()}/complete")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST complete lesson with invalid membership returns 403`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = null,
                mockRepository = CourseRepository()
            )
        }

        val response = client.post("/api/v1/courses/${UUID.randomUUID()}/lessons/${UUID.randomUUID()}/complete") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `POST complete lesson with malformed UUID returns 400`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository()
            )
        }

        val response = client.post("/api/v1/courses/not-a-uuid/lessons/${UUID.randomUUID()}/complete") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST complete lesson when course does not exist returns 404`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> CompleteLessonResult.CourseNotFound }
            )
        }

        val response = client.post("/api/v1/courses/${UUID.randomUUID()}/lessons/${UUID.randomUUID()}/complete") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `POST complete lesson when lesson does not exist returns 404`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> CompleteLessonResult.LessonNotFound }
            )
        }

        val response = client.post("/api/v1/courses/${UUID.randomUUID()}/lessons/${UUID.randomUUID()}/complete") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `POST complete lesson when user is not enrolled returns 403`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> CompleteLessonResult.NotEnrolled }
            )
        }

        val response = client.post("/api/v1/courses/${UUID.randomUUID()}/lessons/${UUID.randomUUID()}/complete") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `POST complete lesson returns 200 with completion progress for authorized user`() = testApplication {
        val courseId = UUID.randomUUID()
        val lessonId = UUID.randomUUID()
        val mockCompletion = LessonCompletionResponseDto(
            success = true,
            lessonId = lessonId.toString(),
            courseId = courseId.toString(),
            completed = true,
            courseProgressPercent = 50.0f,
            completedLessons = 1,
            totalLessons = 2
        )

        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> CompleteLessonResult.Success(mockCompletion) }
            )
        }

        val response = client.post("/api/v1/courses/$courseId/lessons/$lessonId/complete") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("\"success\":true"))
        assertTrue(body.contains("\"completed\":true"))
        assertTrue(body.contains("\"courseProgressPercent\":50.0"))
    }

    @Test
    fun `POST courses without JWT returns 401`() = testApplication {
        application {
            testCourseModule(
                mockSubject = null,
                mockRole = null,
                mockRepository = CourseRepository()
            )
        }

        val response = client.post("/api/v1/courses") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody("""{"title":"دورة برمجة"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST courses without X-Tenant-ID returns 400`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.TEACHER,
                mockRepository = CourseRepository()
            )
        }

        val response = client.post("/api/v1/courses") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"دورة برمجة"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST courses with unauthorized role STUDENT returns 403`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository()
            )
        }

        val response = client.post("/api/v1/courses") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody("""{"title":"دورة برمجة"}""")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `POST courses with blank or missing title returns 400`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.TEACHER,
                mockRepository = CourseRepository()
            )
        }

        // Blank title
        val blankResponse = client.post("/api/v1/courses") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody("""{"title":"   ","description":"وصف"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, blankResponse.status)

        // Missing title
        val missingResponse = client.post("/api/v1/courses") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody("""{"description":"بدون عنوان"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, missingResponse.status)
    }

    @Test
    fun `POST courses with authorized TEACHER returns 201 with created course`() = testApplication {
        val courseId = UUID.randomUUID()
        val mockCreated = CourseResponseDto(
            id = courseId.toString(),
            title = "أساسيات بايثون للمعلمين",
            description = "مقدمة شاملة",
            category = "البرمجة",
            difficulty = "مبتدئ",
            totalModules = 0,
            completedModules = 0,
            progressPercent = 0.0f
        )

        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.TEACHER,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> mockCreated }
            )
        }

        val response = client.post("/api/v1/courses") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody("""{"title":"أساسيات بايثون للمعلمين","description":"مقدمة شاملة","category":"البرمجة","difficulty":"مبتدئ"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("أساسيات بايثون للمعلمين"))
        assertTrue(body.contains(courseId.toString()))
        assertTrue(body.contains("\"progressPercent\":0.0"))
    }

    @Test
    fun `POST courses with authorized ORG_ADMIN returns 201 with created course`() = testApplication {
        val courseId = UUID.randomUUID()
        val mockCreated = CourseResponseDto(
            id = courseId.toString(),
            title = "إدارة المنهج الدراسي",
            description = "منهج المدرسة",
            category = "إدارة تعليمية",
            difficulty = "متقدم",
            totalModules = 0,
            completedModules = 0,
            progressPercent = 0.0f
        )

        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.ORG_ADMIN,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> mockCreated }
            )
        }

        val response = client.post("/api/v1/courses") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody("""{"title":"إدارة المنهج الدراسي","description":"منهج المدرسة","category":"إدارة تعليمية","difficulty":"متقدم"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("إدارة المنهج الدراسي"))
    }
}

