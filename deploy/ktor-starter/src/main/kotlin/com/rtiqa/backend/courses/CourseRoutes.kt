package com.rtiqa.backend.courses

import com.rtiqa.backend.auth.EnterpriseRole
import com.rtiqa.backend.auth.TenantContext
import com.rtiqa.backend.auth.TenantContextKey
import com.rtiqa.backend.auth.isUUID
import com.rtiqa.backend.auth.requireRole
import com.rtiqa.backend.auth.tenantAuthorization
import com.rtiqa.backend.database.DatabaseFactory
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.util.UUID

private val logger = LoggerFactory.getLogger("CourseRoutes")

fun Route.courseRoutes(
    courseRepository: CourseRepository = CourseRepository(),
    authProvider: String = "auth-jwt",
    checkMembership: (suspend (UUID, UUID) -> EnterpriseRole?)? = null,
    transactionRunner: (suspend (TenantContext, suspend (Connection) -> Any?) -> Any?)? = null
) {
    val runTransaction: suspend (TenantContext, suspend (Connection) -> Any?) -> Any? =
        transactionRunner ?: { ctx, block -> DatabaseFactory.transactionWithTenant(ctx, block) }

    authenticate(authProvider) {
        route("/api/v1/courses") {
            if (checkMembership != null) {
                tenantAuthorization(checkMembership)
            } else {
                tenantAuthorization { userId, orgId ->
                    com.rtiqa.backend.auth.TenantAuthConfig().checkMembership(userId, orgId)
                }
            }

            // GET /api/v1/courses
            get {
                val tenantContext = call.attributes.getOrNull(TenantContextKey)
                if (tenantContext == null) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponseDto(403, "Tenant context missing")
                    )
                    return@get
                }

                val category = call.request.queryParameters["category"]

                try {
                    @Suppress("UNCHECKED_CAST")
                    val courses = runTransaction(tenantContext) { conn ->
                        courseRepository.getCoursesForTenant(
                            conn = conn,
                            tenantId = tenantContext.orgId,
                            userId = tenantContext.userId,
                            category = category
                        )
                    } as List<CourseResponseDto>
                    call.respond(HttpStatusCode.OK, courses)
                } catch (e: Exception) {
                    logger.error("Failed to fetch courses for tenant ${tenantContext.orgId}", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponseDto(500, "Internal server error fetching courses")
                    )
                }
            }

            // POST /api/v1/courses
            requireRole(EnterpriseRole.TEACHER, EnterpriseRole.ORG_ADMIN, EnterpriseRole.PRINCIPAL, EnterpriseRole.SUPER_ADMIN) {
                post {
                    val tenantContext = call.attributes.getOrNull(TenantContextKey)
                    if (tenantContext == null) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponseDto(403, "Tenant context missing")
                        )
                        return@post
                    }

                    val request = try {
                        call.receiveNullable<CreateCourseRequestDto>()
                    } catch (e: Exception) {
                        null
                    }

                    if (request == null || request.title.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponseDto(400, "Course title is required and cannot be blank")
                        )
                        return@post
                    }

                    try {
                        val createdCourse = runTransaction(tenantContext) { conn ->
                            courseRepository.createCourse(
                                conn = conn,
                                tenantId = tenantContext.orgId,
                                request = request
                            )
                        } as? CourseResponseDto

                        if (createdCourse != null) {
                            call.respond(HttpStatusCode.Created, createdCourse)
                        } else {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ErrorResponseDto(500, "Failed to create course")
                            )
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to create course for tenant ${tenantContext.orgId}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponseDto(500, "Internal server error creating course")
                        )
                    }
                }
            }

            // GET /api/v1/courses/{courseId}/lessons
            get("/{courseId}/lessons") {
                val tenantContext = call.attributes.getOrNull(TenantContextKey)
                if (tenantContext == null) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponseDto(403, "Tenant context missing")
                    )
                    return@get
                }

                val courseIdParam = call.parameters["courseId"]
                if (!isUUID(courseIdParam)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponseDto(400, "Invalid course ID format")
                    )
                    return@get
                }

                val courseId = UUID.fromString(courseIdParam)

                try {
                    @Suppress("UNCHECKED_CAST")
                    val result = runTransaction(tenantContext) { conn ->
                        val exists = courseRepository.courseExistsInTenant(
                            conn = conn,
                            courseId = courseId,
                            tenantId = tenantContext.orgId
                        )
                        if (!exists) {
                            null
                        } else {
                            courseRepository.getLessonsForCourse(
                                conn = conn,
                                courseId = courseId,
                                tenantId = tenantContext.orgId,
                                userId = tenantContext.userId
                            )
                        }
                    } as? List<LessonResponseDto>

                    if (result == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponseDto(404, "Course not found")
                        )
                    } else {
                        call.respond(HttpStatusCode.OK, result)
                    }
                } catch (e: Exception) {
                    logger.error("Failed to fetch lessons for course $courseIdParam", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponseDto(500, "Internal server error fetching lessons")
                    )
                }
            }

            // POST /api/v1/courses/{courseId}/lessons/{lessonId}/complete
            post("/{courseId}/lessons/{lessonId}/complete") {
                val tenantContext = call.attributes.getOrNull(TenantContextKey)
                if (tenantContext == null) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponseDto(403, "Tenant context missing")
                    )
                    return@post
                }

                val courseIdParam = call.parameters["courseId"]
                val lessonIdParam = call.parameters["lessonId"]
                if (!isUUID(courseIdParam) || !isUUID(lessonIdParam)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponseDto(400, "Invalid UUID format for courseId or lessonId")
                    )
                    return@post
                }

                val courseId = UUID.fromString(courseIdParam)
                val lessonId = UUID.fromString(lessonIdParam)

                val reqDto = try {
                    call.receiveNullable<CompleteLessonRequestDto>()
                } catch (e: Exception) {
                    null
                }
                val score = reqDto?.score ?: 100

                try {
                    val result = runTransaction(tenantContext) { conn ->
                        courseRepository.completeLesson(
                            conn = conn,
                            courseId = courseId,
                            lessonId = lessonId,
                            tenantId = tenantContext.orgId,
                            userId = tenantContext.userId,
                            score = score
                        )
                    } as? CompleteLessonResult

                    when (result) {
                        is CompleteLessonResult.Success -> {
                            call.respond(HttpStatusCode.OK, result.completion)
                        }
                        is CompleteLessonResult.CourseNotFound -> {
                            call.respond(HttpStatusCode.NotFound, ErrorResponseDto(404, "Course not found"))
                        }
                        is CompleteLessonResult.LessonNotFound -> {
                            call.respond(HttpStatusCode.NotFound, ErrorResponseDto(404, "Lesson not found"))
                        }
                        is CompleteLessonResult.NotEnrolled -> {
                            call.respond(HttpStatusCode.Forbidden, ErrorResponseDto(403, "User is not enrolled in this course"))
                        }
                        null -> {
                            call.respond(HttpStatusCode.InternalServerError, ErrorResponseDto(500, "Transaction execution failed"))
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Failed to complete lesson $lessonIdParam for course $courseIdParam", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponseDto(500, "Internal server error completing lesson")
                    )
                }
            }
        }
    }
}
