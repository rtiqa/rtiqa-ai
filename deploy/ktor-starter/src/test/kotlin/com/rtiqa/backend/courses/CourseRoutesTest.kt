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
    fun `POST complete lesson with unauthorized role PARENT returns 403`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.PARENT,
                mockRepository = CourseRepository()
            )
        }

        val response = client.post("/api/v1/courses/${UUID.randomUUID()}/lessons/${UUID.randomUUID()}/complete") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `POST complete lesson with negative score returns 400`() = testApplication {
        val courseId = UUID.randomUUID()
        val lessonId = UUID.randomUUID()
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository()
            )
        }

        val response = client.post("/api/v1/courses/$courseId/lessons/$lessonId/complete") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody("""{"score":-10}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST progress lesson returns 200 with completion progress for authorized student`() = testApplication {
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

        val response = client.post("/api/v1/courses/$courseId/lessons/$lessonId/progress") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody("""{"completed":true,"score":95}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("\"success\":true"))
        assertTrue(body.contains("\"completed\":true"))
        assertTrue(body.contains("\"courseProgressPercent\":50.0"))
    }

    @Test
    fun `POST progress lesson with unauthorized role PARENT returns 403`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.PARENT,
                mockRepository = CourseRepository()
            )
        }

        val response = client.post("/api/v1/courses/${UUID.randomUUID()}/lessons/${UUID.randomUUID()}/progress") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
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

    @Test
    fun `POST lessons without JWT returns 401`() = testApplication {
        application {
            testCourseModule(
                mockSubject = null,
                mockRole = null,
                mockRepository = CourseRepository()
            )
        }

        val response = client.post("/api/v1/courses/${UUID.randomUUID()}/lessons") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody("""{"title":"درس جديد"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST lessons without X-Tenant-ID returns 400`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.TEACHER,
                mockRepository = CourseRepository()
            )
        }

        val response = client.post("/api/v1/courses/${UUID.randomUUID()}/lessons") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"درس جديد"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST lessons with unauthorized role STUDENT returns 403`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository()
            )
        }

        val response = client.post("/api/v1/courses/${UUID.randomUUID()}/lessons") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody("""{"title":"درس جديد"}""")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `POST lessons with blank or missing title returns 400`() = testApplication {
        val courseId = UUID.randomUUID()
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.TEACHER,
                mockRepository = CourseRepository()
            )
        }

        // Blank title
        val blankResponse = client.post("/api/v1/courses/$courseId/lessons") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody("""{"title":"   ","content":"محتوى"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, blankResponse.status)

        // Missing title
        val missingResponse = client.post("/api/v1/courses/$courseId/lessons") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody("""{"content":"بدون عنوان"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, missingResponse.status)
    }

    @Test
    fun `POST lessons with invalid course UUID returns 400`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.TEACHER,
                mockRepository = CourseRepository()
            )
        }

        val response = client.post("/api/v1/courses/not-a-valid-uuid/lessons") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody("""{"title":"درس جديد"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST lessons when course does not exist returns 404`() = testApplication {
        val courseId = UUID.randomUUID()
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.TEACHER,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> CreateLessonResult.CourseNotFound }
            )
        }

        val response = client.post("/api/v1/courses/$courseId/lessons") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody("""{"title":"درس جديد"}""")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `POST lessons with authorized TEACHER returns 201 with created lesson`() = testApplication {
        val courseId = UUID.randomUUID()
        val lessonId = UUID.randomUUID()
        val mockCreatedLesson = LessonResponseDto(
            id = lessonId.toString(),
            courseId = courseId.toString(),
            title = "مقدمة في الدوال البرمجية",
            content = "شرح مفصل عن الدوال والمعاملات",
            moduleOrder = 1,
            estimatedMinutes = 20,
            isCompleted = false
        )

        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.TEACHER,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> CreateLessonResult.Success(mockCreatedLesson) }
            )
        }

        val response = client.post("/api/v1/courses/$courseId/lessons") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody("""{"title":"مقدمة في الدوال البرمجية","content":"شرح مفصل عن الدوال والمعاملات","moduleOrder":1,"estimatedMinutes":20}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("مقدمة في الدوال البرمجية"))
        assertTrue(body.contains(courseId.toString()))
        assertTrue(body.contains(lessonId.toString()))
        assertTrue(body.contains("\"moduleOrder\":1"))
    }

    @Test
    fun `POST lessons with authorized ORG_ADMIN returns 201 with created lesson`() = testApplication {
        val courseId = UUID.randomUUID()
        val lessonId = UUID.randomUUID()
        val mockCreatedLesson = LessonResponseDto(
            id = lessonId.toString(),
            courseId = courseId.toString(),
            title = "الدرس الأول من إدارة المدرسة",
            content = "محتوى إداري",
            moduleOrder = 2,
            estimatedMinutes = 30,
            isCompleted = false
        )

        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.ORG_ADMIN,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> CreateLessonResult.Success(mockCreatedLesson) }
            )
        }

        val response = client.post("/api/v1/courses/$courseId/lessons") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody("""{"title":"الدرس الأول من إدارة المدرسة","content":"محتوى إداري","moduleOrder":2,"estimatedMinutes":30}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("الدرس الأول من إدارة المدرسة"))
    }

    @Test
    fun `GET course by id without JWT returns 401`() = testApplication {
        application {
            testCourseModule(
                mockSubject = null,
                mockRole = null,
                mockRepository = CourseRepository()
            )
        }

        val response = client.get("/api/v1/courses/${UUID.randomUUID()}") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET course by id without X-Tenant-ID returns 400`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository()
            )
        }

        val response = client.get("/api/v1/courses/${UUID.randomUUID()}")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET course by id with invalid membership returns 403`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = null,
                mockRepository = CourseRepository()
            )
        }

        val response = client.get("/api/v1/courses/${UUID.randomUUID()}") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `GET course by id with malformed UUID returns 400`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository()
            )
        }

        val response = client.get("/api/v1/courses/not-a-valid-uuid") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET course by id when course does not exist returns 404`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> null }
            )
        }

        val response = client.get("/api/v1/courses/${UUID.randomUUID()}") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET course by id returns 200 with course details and student progress`() = testApplication {
        val courseId = UUID.randomUUID()
        val mockCourse = CourseResponseDto(
            id = courseId.toString(),
            title = "مسار الذكاء الاصطناعي",
            description = "مقدمة شاملة",
            category = "الذكاء الاصطناعي",
            difficulty = "مبتدئ",
            totalModules = 4,
            completedModules = 2,
            progressPercent = 50.0f
        )

        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> mockCourse }
            )
        }

        val response = client.get("/api/v1/courses/$courseId") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("مسار الذكاء الاصطناعي"))
        assertTrue(body.contains(courseId.toString()))
        assertTrue(body.contains("\"totalModules\":4"))
        assertTrue(body.contains("\"completedModules\":2"))
        assertTrue(body.contains("\"progressPercent\":50.0"))
    }

    @Test
    fun `GET lesson by id without JWT returns 401`() = testApplication {
        application {
            testCourseModule(
                mockSubject = null,
                mockRole = null,
                mockRepository = CourseRepository()
            )
        }

        val response = client.get("/api/v1/courses/${UUID.randomUUID()}/lessons/${UUID.randomUUID()}") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET lesson by id without X-Tenant-ID returns 400`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository()
            )
        }

        val response = client.get("/api/v1/courses/${UUID.randomUUID()}/lessons/${UUID.randomUUID()}")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET lesson by id with invalid membership returns 403`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = null,
                mockRepository = CourseRepository()
            )
        }

        val response = client.get("/api/v1/courses/${UUID.randomUUID()}/lessons/${UUID.randomUUID()}") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `GET lesson by id with malformed courseId returns 400`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository()
            )
        }

        val response = client.get("/api/v1/courses/not-a-valid-course-uuid/lessons/${UUID.randomUUID()}") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET lesson by id with malformed lessonId returns 400`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository()
            )
        }

        val response = client.get("/api/v1/courses/${UUID.randomUUID()}/lessons/not-a-valid-lesson-uuid") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET lesson by id when course does not exist returns 404`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> GetLessonResult.CourseNotFound }
            )
        }

        val response = client.get("/api/v1/courses/${UUID.randomUUID()}/lessons/${UUID.randomUUID()}") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Course not found"))
    }

    @Test
    fun `GET lesson by id when lesson does not exist returns 404`() = testApplication {
        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> GetLessonResult.LessonNotFound }
            )
        }

        val response = client.get("/api/v1/courses/${UUID.randomUUID()}/lessons/${UUID.randomUUID()}") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Lesson not found"))
    }

    @Test
    fun `GET lesson by id returns 200 with lesson details and student completion status`() = testApplication {
        val courseId = UUID.randomUUID()
        val lessonId = UUID.randomUUID()
        val mockLesson = LessonResponseDto(
            id = lessonId.toString(),
            courseId = courseId.toString(),
            title = "مقدمة في المصفوفات",
            content = "شرح تفصيلي للمصفوفات في لغة بايثون",
            moduleOrder = 3,
            estimatedMinutes = 25,
            isCompleted = true
        )

        application {
            testCourseModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT,
                mockRepository = CourseRepository(),
                mockTransactionRunner = { _, _ -> GetLessonResult.Success(mockLesson) }
            )
        }

        val response = client.get("/api/v1/courses/$courseId/lessons/$lessonId") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("مقدمة في المصفوفات"))
        assertTrue(body.contains(courseId.toString()))
        assertTrue(body.contains(lessonId.toString()))
        assertTrue(body.contains("\"isCompleted\":true"))
        assertTrue(body.contains("\"moduleOrder\":3"))
    }
}

