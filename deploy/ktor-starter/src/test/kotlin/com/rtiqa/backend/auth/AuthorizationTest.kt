package com.rtiqa.backend.auth

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class AuthorizationTest {

    private fun Application.testModule(
        mockSubject: String?,
        mockMembershipRole: EnterpriseRole?
    ) {
        install(Authentication) {
            provider {
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
            authenticate {
                route("/protected") {
                    tenantAuthorization { _, _ -> mockMembershipRole }
                    requireRole(EnterpriseRole.TEACHER, EnterpriseRole.ORG_ADMIN) {
                        get {
                            call.respondText(
                                """{"status":200,"message":"Success"}""",
                                ContentType.Application.Json,
                                HttpStatusCode.OK
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `JWT missing or invalid subject returns 401`() = testApplication {
        application { testModule(null, null) }
        val response = client.get("/protected") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `Invalid X-Tenant-ID header returns 400`() = testApplication {
        application { testModule(UUID.randomUUID().toString(), null) }
        val response = client.get("/protected") {
            header("X-Tenant-ID", "not-a-uuid")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `Valid UUIDs but no membership returns 403`() = testApplication {
        application { testModule(UUID.randomUUID().toString(), null) }
        val response = client.get("/protected") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Valid membership but wrong role returns 403`() = testApplication {
        application { testModule(UUID.randomUUID().toString(), EnterpriseRole.STUDENT) }
        val response = client.get("/protected") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Valid membership and right role allows access`() = testApplication {
        application { testModule(UUID.randomUUID().toString(), EnterpriseRole.TEACHER) }
        val response = client.get("/protected") {
            header("X-Tenant-ID", UUID.randomUUID().toString())
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
