package com.rtiqa.backend.courses

import com.rtiqa.backend.auth.EnterpriseRole
import com.rtiqa.backend.auth.TenantContext
import com.rtiqa.backend.auth.TenantContextKey
import com.rtiqa.backend.auth.isUUID
import com.rtiqa.backend.auth.requireRole
import com.rtiqa.backend.auth.tenantAuthorization
import com.rtiqa.backend.database.DatabaseFactory
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
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

            // GET /api/v1/courses/{courseId}
            get("/{courseId}") {
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
                    val course = runTransaction(tenantContext) { conn ->
                        courseRepository.getCourseById(
                            conn = conn,
                            courseId = courseId,
                            tenantId = tenantContext.orgId,
                            userId = tenantContext.userId
                        )
                    } as? CourseResponseDto

                    if (course != null) {
                        call.respond(HttpStatusCode.OK, course)
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponseDto(404, "Course not found")
                        )
                    }
                } catch (e: Exception) {
                    logger.error("Failed to fetch course $courseIdParam for tenant ${tenantContext.orgId}", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponseDto(500, "Internal server error fetching course")
                    )
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

            // GET /api/v1/courses/{courseId}/lessons/{lessonId}
            get("/{courseId}/lessons/{lessonId}") {
                val tenantContext = call.attributes.getOrNull(TenantContextKey)
                if (tenantContext == null) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponseDto(403, "Tenant context missing")
                    )
                    return@get
                }

                val courseIdParam = call.parameters["courseId"]
                val lessonIdParam = call.parameters["lessonId"]
                if (!isUUID(courseIdParam)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponseDto(400, "Invalid course ID format")
                    )
                    return@get
                }
                if (!isUUID(lessonIdParam)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponseDto(400, "Invalid lesson ID format")
                    )
                    return@get
                }

                val courseId = UUID.fromString(courseIdParam)
                val lessonId = UUID.fromString(lessonIdParam)

                try {
                    val result = runTransaction(tenantContext) { conn ->
                        courseRepository.getLesson(
                            conn = conn,
                            courseId = courseId,
                            lessonId = lessonId,
                            tenantId = tenantContext.orgId,
                            userId = tenantContext.userId
                        )
                    } as? GetLessonResult

                    when (result) {
                        is GetLessonResult.Success -> {
                            call.respond(HttpStatusCode.OK, result.lesson)
                        }
                        is GetLessonResult.CourseNotFound -> {
                            call.respond(
                                HttpStatusCode.NotFound,
                                ErrorResponseDto(404, "Course not found")
                            )
                        }
                        is GetLessonResult.LessonNotFound -> {
                            call.respond(
                                HttpStatusCode.NotFound,
                                ErrorResponseDto(404, "Lesson not found")
                            )
                        }
                        null -> {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ErrorResponseDto(500, "Transaction execution failed")
                            )
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Failed to fetch lesson $lessonIdParam for course $courseIdParam", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponseDto(500, "Internal server error fetching lesson")
                    )
                }
            }

            // POST /api/v1/courses/{courseId}/lessons
            requireRole(EnterpriseRole.TEACHER, EnterpriseRole.ORG_ADMIN, EnterpriseRole.PRINCIPAL, EnterpriseRole.SUPER_ADMIN) {
                post("/{courseId}/lessons") {
                    val tenantContext = call.attributes.getOrNull(TenantContextKey)
                    if (tenantContext == null) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponseDto(403, "Tenant context missing")
                        )
                        return@post
                    }

                    val courseIdParam = call.parameters["courseId"]
                    if (!isUUID(courseIdParam)) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponseDto(400, "Invalid course ID format")
                        )
                        return@post
                    }

                    val courseId = UUID.fromString(courseIdParam)

                    val request = try {
                        call.receiveNullable<CreateLessonRequestDto>()
                    } catch (e: Exception) {
                        null
                    }

                    if (request == null || request.title.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponseDto(400, "Lesson title is required and cannot be blank")
                        )
                        return@post
                    }

                    try {
                        val result = runTransaction(tenantContext) { conn ->
                            courseRepository.createLesson(
                                conn = conn,
                                courseId = courseId,
                                tenantId = tenantContext.orgId,
                                request = request
                            )
                        } as? CreateLessonResult

                        when (result) {
                            is CreateLessonResult.Success -> {
                                call.respond(HttpStatusCode.Created, result.lesson)
                            }
                            is CreateLessonResult.CourseNotFound -> {
                                call.respond(
                                    HttpStatusCode.NotFound,
                                    ErrorResponseDto(404, "Course not found")
                                )
                            }
                            null -> {
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    ErrorResponseDto(500, "Failed to create lesson")
                                )
                            }
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to create lesson for course $courseIdParam in tenant ${tenantContext.orgId}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponseDto(500, "Internal server error creating lesson")
                        )
                    }
                }
            }

            // POST /api/v1/courses/{courseId}/lessons/{lessonId}/complete
            // POST /api/v1/courses/{courseId}/lessons/{lessonId}/progress
            requireRole(EnterpriseRole.STUDENT) {
                post("/{courseId}/lessons/{lessonId}/complete") {
                    handleLessonCompletion(call, runTransaction, courseRepository)
                }

                post("/{courseId}/lessons/{lessonId}/progress") {
                    handleLessonCompletion(call, runTransaction, courseRepository)
                }
            }
        }
    }
}

private suspend fun handleLessonCompletion(
    call: ApplicationCall,
    runTransaction: suspend (TenantContext, suspend (Connection) -> Any?) -> Any?,
    courseRepository: CourseRepository
) {
    val tenantContext = call.attributes.getOrNull(TenantContextKey)
    if (tenantContext == null) {
        call.respond(
            HttpStatusCode.Forbidden,
            ErrorResponseDto(403, "Tenant context missing")
        )
        return
    }

    val courseIdParam = call.parameters["courseId"]
    val lessonIdParam = call.parameters["lessonId"]
    if (!isUUID(courseIdParam) || !isUUID(lessonIdParam)) {
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponseDto(400, "Invalid UUID format for courseId or lessonId")
        )
        return
    }

    val courseId = UUID.fromString(courseIdParam)
    val lessonId = UUID.fromString(lessonIdParam)

    val reqDto = try {
        call.receiveNullable<CompleteLessonRequestDto>()
    } catch (e: Exception) {
        null
    }

    if (reqDto?.score != null && reqDto.score < 0) {
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponseDto(400, "Score cannot be negative")
        )
        return
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
