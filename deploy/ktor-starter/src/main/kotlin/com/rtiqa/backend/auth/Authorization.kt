package com.rtiqa.backend.auth

import com.rtiqa.backend.database.DatabaseFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

enum class EnterpriseRole {
    SUPER_ADMIN, ORG_ADMIN, PRINCIPAL, VICE_PRINCIPAL, TEACHER, STUDENT, PARENT, STAFF
}

data class TenantContext(
    val userId: UUID,
    val orgId: UUID,
    val role: EnterpriseRole
)

val TenantContextKey = AttributeKey<TenantContext>("TenantContextKey")

fun isUUID(str: String?): Boolean {
    if (str == null) return false
    return try {
        UUID.fromString(str)
        true
    } catch (e: Exception) {
        false
    }
}

class TenantAuthConfig {
    var checkMembership: suspend (UUID, UUID) -> EnterpriseRole? = { userId, orgId ->
        withContext(Dispatchers.IO) {
            var roleStr: String? = null
            DatabaseFactory.getConnection()?.use { conn ->
                val sql = "SELECT role FROM organization_members WHERE user_id = ? AND organization_id = ? AND status = 'ACTIVE'"
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setObject(1, userId)
                    stmt.setObject(2, orgId)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            roleStr = rs.getString("role")
                        }
                    }
                }
            }
            roleStr?.let {
                try {
                    EnterpriseRole.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }
    }
}

val TenantAuthorizationPlugin = createRouteScopedPlugin(
    name = "TenantAuthorizationPlugin",
    createConfiguration = ::TenantAuthConfig
) {
    val checkMembership = pluginConfig.checkMembership
    
    // We can't use finish() in onCall, but we can just use normal interceptors for this!
    // Actually, createRouteScopedPlugin allows installing standard interceptors inside!
    onCall { call ->
       // unused, we will use route.tenantAuthorization below
    }
}

fun Route.tenantAuthorization(checkMembership: suspend (UUID, UUID) -> EnterpriseRole?) {
    intercept(ApplicationCallPipeline.Call) {
        val principal = call.principal<JWTPrincipal>()
        val subject = principal?.payload?.subject

        if (!isUUID(subject)) {
            call.respondText(
                """{"status":401,"message":"Invalid or missing JWT subject"}""",
                ContentType.Application.Json,
                HttpStatusCode.Unauthorized
            )
            finish()
            return@intercept
        }
        val userId = UUID.fromString(subject)

        val tenantIdHeader = call.request.header("X-Tenant-ID")
        if (!isUUID(tenantIdHeader)) {
            call.respondText(
                """{"status":400,"message":"Missing or invalid X-Tenant-ID header"}""",
                ContentType.Application.Json,
                HttpStatusCode.BadRequest
            )
            finish()
            return@intercept
        }
        val orgId = UUID.fromString(tenantIdHeader)

        val role = checkMembership(userId, orgId)
        
        if (role == null) {
            call.respondText(
                """{"status":403,"message":"Active membership not found for this organization or invalid role"}""",
                ContentType.Application.Json,
                HttpStatusCode.Forbidden
            )
            finish()
            return@intercept
        }

        val tenantContext = TenantContext(userId, orgId, role)
        call.attributes.put(TenantContextKey, tenantContext)
    }
}

fun Route.requireRole(vararg allowedRoles: EnterpriseRole, build: Route.() -> Unit) {
    val authorizedRoute = createChild(object : RouteSelector() {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = RouteSelectorEvaluation.Constant
    })
    
    authorizedRoute.intercept(ApplicationCallPipeline.Call) {
        val tenantContext = call.attributes.getOrNull(TenantContextKey)
        if (tenantContext == null) {
            call.respondText(
                """{"status":403,"message":"Tenant context missing"}""",
                ContentType.Application.Json,
                HttpStatusCode.Forbidden
            )
            finish()
            return@intercept
        }
        if (!allowedRoles.contains(tenantContext.role)) {
            call.respondText(
                """{"status":403,"message":"Insufficient permissions"}""",
                ContentType.Application.Json,
                HttpStatusCode.Forbidden
            )
            finish()
            return@intercept
        }
    }
    
    authorizedRoute.build()
}
