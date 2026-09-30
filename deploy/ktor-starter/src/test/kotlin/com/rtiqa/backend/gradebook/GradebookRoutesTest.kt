package com.rtiqa.backend.gradebook

import com.rtiqa.backend.auth.EnterpriseRole
import com.rtiqa.backend.auth.TenantContext
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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.UUID

class GradebookRoutesTest {

    private fun Application.testGradebookModule(
        mockSubject: String?,
        mockRole: EnterpriseRole?,
        mockRepository: GradebookRepository = GradebookRepository(),
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
            gradebookRoutes(
                gradebookRepository = mockRepository,
                authProvider = "auth-jwt",
                checkMembership = { _, _ -> mockRole },
                transactionRunner = mockTransactionRunner
            )
        }
    }

    @Test
    fun `GET gradebook without JWT returns 401 Unauthorized`() = testApplication {
        application {
            testGradebookModule(
                mockSubject = null,
                mockRole = null
            )
        }

        val response = client.get("/api/v1/classes/${UUID.randomUUID()}/gradebook") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET gradebook without X-Tenant-ID header returns 400 Bad Request`() = testApplication {
        application {
            testGradebookModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.TEACHER
            )
        }

        val response = client.get("/api/v1/classes/${UUID.randomUUID()}/gradebook")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Missing or invalid X-Tenant-ID header"))
    }

    @Test
    fun `GET gradebook with invalid X-Tenant-ID returns 400 Bad Request`() = testApplication {
        application {
            testGradebookModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.TEACHER
            )
        }

        val response = client.get("/api/v1/classes/${UUID.randomUUID()}/gradebook") {
            header("X-Tenant-ID", "not-a-valid-uuid")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Missing or invalid X-Tenant-ID header"))
    }

    @Test
    fun `GET gradebook with inactive membership returns 403 Forbidden`() = testApplication {
        application {
            testGradebookModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = null
            )
        }

        val response = client.get("/api/v1/classes/${UUID.randomUUID()}/gradebook") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `GET gradebook with unauthorized role STUDENT returns 403 Forbidden`() = testApplication {
        application {
            testGradebookModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STUDENT
            )
        }

        val response = client.get("/api/v1/classes/${UUID.randomUUID()}/gradebook") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertTrue(response.bodyAsText().contains("Insufficient permissions"))
    }

    @Test
    fun `GET gradebook with unauthorized role PARENT returns 403 Forbidden`() = testApplication {
        application {
            testGradebookModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.PARENT
            )
        }

        val response = client.get("/api/v1/classes/${UUID.randomUUID()}/gradebook") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertTrue(response.bodyAsText().contains("Insufficient permissions"))
    }

    @Test
    fun `GET gradebook with unauthorized role STAFF returns 403 Forbidden`() = testApplication {
        application {
            testGradebookModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.STAFF
            )
        }

        val response = client.get("/api/v1/classes/${UUID.randomUUID()}/gradebook") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertTrue(response.bodyAsText().contains("Insufficient permissions"))
    }

    @Test
    fun `GET gradebook with malformed classId returns 400 Bad Request`() = testApplication {
        application {
            testGradebookModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.TEACHER
            )
        }

        val response = client.get("/api/v1/classes/invalid-uuid-abc/gradebook") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Invalid or missing classId"))
    }

    @Test
    fun `GET gradebook for nonexistent class returns 404 Not Found`() = testApplication {
        application {
            testGradebookModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.TEACHER,
                mockTransactionRunner = { _, _ -> GetClassGradebookResult.ClassNotFound }
            )
        }

        val response = client.get("/api/v1/classes/${UUID.randomUUID()}/gradebook") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("Class not found"))
    }

    @Test
    fun `GET gradebook for class belonging to another tenant returns neutral 404 Not Found`() = testApplication {
        application {
            testGradebookModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.TEACHER,
                mockTransactionRunner = { _, _ -> GetClassGradebookResult.ClassNotFound }
            )
        }

        val response = client.get("/api/v1/classes/${UUID.randomUUID()}/gradebook") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("Class not found"))
    }

    @Test
    fun `GET gradebook with valid authorized request returns 200 and real classId`() = testApplication {
        val classId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val sampleGradebook = ClassGradebookDto(
            classId = classId.toString(),
            students = listOf(
                GradebookStudentDto(
                    studentId = UUID.randomUUID().toString(),
                    displayName = "عبدالله الشهري",
                    studentNumber = "44101"
                )
            ),
            assessments = listOf(
                GradebookAssessmentDto(
                    assessmentId = UUID.randomUUID().toString(),
                    title = "اختبار الفصلي",
                    maxScore = 30.0,
                    weight = 0.3
                )
            ),
            scores = emptyList()
        )

        application {
            testGradebookModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.TEACHER,
                mockTransactionRunner = { ctx, _ ->
                    assertEquals(tenantId, ctx.orgId)
                    GetClassGradebookResult.Success(sampleGradebook)
                }
            )
        }

        val response = client.get("/api/v1/classes/$classId/gradebook") {
            header("X-Tenant-ID", tenantId.toString())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains(classId.toString()))
        assertTrue(body.contains("عبدالله الشهري"))
        assertTrue(body.contains("اختبار الفصلي"))
    }

    @Test
    fun `GET gradebook allows all authorized staff roles ORG_ADMIN, PRINCIPAL, VICE_PRINCIPAL, TEACHER`() = testApplication {
        val classId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val emptyGradebook = ClassGradebookDto(
            classId = classId.toString(),
            students = emptyList(),
            assessments = emptyList(),
            scores = emptyList()
        )

        val allowedRoles = listOf(
            EnterpriseRole.ORG_ADMIN,
            EnterpriseRole.PRINCIPAL,
            EnterpriseRole.VICE_PRINCIPAL,
            EnterpriseRole.TEACHER
        )

        for (role in allowedRoles) {
            val app = testApplication {
                application {
                    testGradebookModule(
                        mockSubject = UUID.randomUUID().toString(),
                        mockRole = role,
                        mockTransactionRunner = { _, _ -> GetClassGradebookResult.Success(emptyGradebook) }
                    )
                }

                val response = client.get("/api/v1/classes/$classId/gradebook") {
                    header("X-Tenant-ID", tenantId.toString())
                }
                assertEquals(HttpStatusCode.OK, response.status, "Role $role should be authorized")
            }
        }
    }

    @Test
    fun `GET gradebook on database failure returns 500 without leaking internal details`() = testApplication {
        val classId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()

        application {
            testGradebookModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.TEACHER,
                mockTransactionRunner = { _, _ ->
                    throw SQLException("psql: fatal error internal connection table deadlock state at node-03")
                }
            )
        }

        val response = client.get("/api/v1/classes/$classId/gradebook") {
            header("X-Tenant-ID", tenantId.toString())
        }
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Internal server error retrieving gradebook"))
        assertFalse(body.contains("psql"), "Must not leak database engine details")
        assertFalse(body.contains("deadlock"), "Must not leak internal database errors")
        assertFalse(body.contains("node-03"), "Must not leak server topology")
    }

    @Test
    fun `GET gradebook end-to-end with real in-memory database integration`() = testApplication {
        val connection = DriverManager.getConnection("jdbc:h2:mem:route_test_${UUID.randomUUID()};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE")
        
        connection.createStatement().use { stmt ->
            stmt.execute("CREATE TABLE organizations (id UUID PRIMARY KEY, name VARCHAR(255) NOT NULL, slug VARCHAR(100) NOT NULL)")
            stmt.execute("CREATE TABLE profiles (id UUID PRIMARY KEY, name VARCHAR(255) NOT NULL, email VARCHAR(255) NOT NULL)")
            stmt.execute("CREATE TABLE academic_classes (id UUID PRIMARY KEY, organization_id UUID NOT NULL, name VARCHAR(255) NOT NULL, academic_year VARCHAR(100), term VARCHAR(100), created_at TIMESTAMP DEFAULT NOW())")
            stmt.execute("CREATE TABLE class_students (id UUID PRIMARY KEY, class_id UUID NOT NULL, student_id UUID NOT NULL, student_number VARCHAR(100), created_at TIMESTAMP DEFAULT NOW())")
            stmt.execute("CREATE TABLE gradebook_assessments (id UUID PRIMARY KEY, class_id UUID NOT NULL, title VARCHAR(255) NOT NULL, max_score NUMERIC NOT NULL, weight NUMERIC NULL, created_at TIMESTAMP DEFAULT NOW())")
            stmt.execute("CREATE TABLE gradebook_scores (id UUID PRIMARY KEY, assessment_id UUID NOT NULL, student_id UUID NOT NULL, score NUMERIC NULL, updated_at TIMESTAMP DEFAULT NOW())")
        }

        val tenantId = UUID.randomUUID()
        val classId = UUID.randomUUID()
        val studentId = UUID.randomUUID()
        val asmId = UUID.randomUUID()

        // Insert organization
        connection.prepareStatement("INSERT INTO organizations (id, name, slug) VALUES (?, ?, ?)").use {
            it.setObject(1, tenantId)
            it.setString(2, "ثانوية المجد")
            it.setString(3, "al-majd")
            it.executeUpdate()
        }

        // Insert class
        connection.prepareStatement("INSERT INTO academic_classes (id, organization_id, name) VALUES (?, ?, ?)").use {
            it.setObject(1, classId)
            it.setObject(2, tenantId)
            it.setString(3, "الصف الثاني ثانوي")
            it.executeUpdate()
        }

        // Insert student profile and enroll
        connection.prepareStatement("INSERT INTO profiles (id, name, email) VALUES (?, ?, ?)").use {
            it.setObject(1, studentId)
            it.setString(2, "فيصل بن عبدالعزيز")
            it.setString(3, "faisal@school.edu")
            it.executeUpdate()
        }
        connection.prepareStatement("INSERT INTO class_students (id, class_id, student_id, student_number) VALUES (?, ?, ?, ?)").use {
            it.setObject(1, UUID.randomUUID())
            it.setObject(2, classId)
            it.setObject(3, studentId)
            it.setString(4, "44201")
            it.executeUpdate()
        }

        // Insert assessment and score
        connection.prepareStatement("INSERT INTO gradebook_assessments (id, class_id, title, max_score, weight) VALUES (?, ?, ?, ?, ?)").use {
            it.setObject(1, asmId)
            it.setObject(2, classId)
            it.setString(3, "الاختبار القصير 1")
            it.setDouble(4, 15.0)
            it.setDouble(5, 0.15)
            it.executeUpdate()
        }
        connection.prepareStatement("INSERT INTO gradebook_scores (id, assessment_id, student_id, score) VALUES (?, ?, ?, ?)").use {
            it.setObject(1, UUID.randomUUID())
            it.setObject(2, asmId)
            it.setObject(3, studentId)
            it.setDouble(4, 14.5)
            it.executeUpdate()
        }

        application {
            testGradebookModule(
                mockSubject = UUID.randomUUID().toString(),
                mockRole = EnterpriseRole.TEACHER,
                mockRepository = GradebookRepository(),
                mockTransactionRunner = { _, block -> block(connection) }
            )
        }

        val response = client.get("/api/v1/classes/$classId/gradebook") {
            header("X-Tenant-ID", tenantId.toString())
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains(classId.toString()))
        assertTrue(body.contains("فيصل بن عبدالعزيز"))
        assertTrue(body.contains("الاختبار القصير 1"))
        assertTrue(body.contains("14.5"))

        connection.close()
    }
}
