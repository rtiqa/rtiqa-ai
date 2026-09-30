package com.rtiqa.backend.gradebook

import com.rtiqa.backend.auth.EnterpriseRole
import com.rtiqa.backend.auth.TenantAuthConfig
import com.rtiqa.backend.auth.TenantContext
import com.rtiqa.backend.auth.TenantContextKey
import com.rtiqa.backend.auth.isUUID
import com.rtiqa.backend.auth.requireRole
import com.rtiqa.backend.auth.tenantAuthorization
import com.rtiqa.backend.courses.ErrorResponseDto
import com.rtiqa.backend.database.DatabaseFactory
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.util.UUID

private val logger = LoggerFactory.getLogger("GradebookRoutes")

fun Route.gradebookRoutes(
    gradebookRepository: GradebookRepository = GradebookRepository(),
    authProvider: String = "auth-jwt",
    checkMembership: (suspend (UUID, UUID) -> EnterpriseRole?)? = null,
    transactionRunner: (suspend (TenantContext, suspend (Connection) -> Any?) -> Any?)? = null
) {
    val runTransaction: suspend (TenantContext, suspend (Connection) -> Any?) -> Any? =
        transactionRunner ?: { ctx, block -> DatabaseFactory.transactionWithTenant(ctx, block) }

    authenticate(authProvider) {
        route("/api/v1/classes/{classId}/gradebook") {
            if (checkMembership != null) {
                tenantAuthorization(checkMembership)
            } else {
                tenantAuthorization { userId, orgId ->
                    TenantAuthConfig().checkMembership(userId, orgId)
                }
            }

            requireRole(
                EnterpriseRole.ORG_ADMIN,
                EnterpriseRole.PRINCIPAL,
                EnterpriseRole.VICE_PRINCIPAL,
                EnterpriseRole.TEACHER
            ) {
                get {
                    val tenantContext = call.attributes.getOrNull(TenantContextKey)
                    if (tenantContext == null) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponseDto(403, "Tenant context missing")
                        )
                        return@get
                    }

                    val classIdParam = call.parameters["classId"]
                    if (classIdParam.isNullOrBlank() || !isUUID(classIdParam)) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponseDto(400, "Invalid or missing classId")
                        )
                        return@get
                    }

                    val classId = UUID.fromString(classIdParam)

                    try {
                        val result = runTransaction(tenantContext) { conn ->
                            gradebookRepository.getClassGradebook(
                                conn = conn,
                                classId = classId,
                                tenantId = tenantContext.orgId
                            )
                        }

                        when (result) {
                            is GetClassGradebookResult.Success -> {
                                call.respond(HttpStatusCode.OK, result.gradebook)
                            }
                            is GetClassGradebookResult.ClassNotFound -> {
                                call.respond(
                                    HttpStatusCode.NotFound,
                                    ErrorResponseDto(404, "Class not found")
                                )
                            }
                            else -> {
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    ErrorResponseDto(500, "Unexpected error retrieving gradebook")
                                )
                            }
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to retrieve gradebook for class $classId and tenant ${tenantContext.orgId}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponseDto(500, "Internal server error retrieving gradebook")
                        )
                    }
                }
            }
        }
    }
}
