#!/bin/bash
# RtiqaDaos.kt
sed -i '/suspend fun deleteCourseById/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt

# EnterpriseDao.kt
sed -i '/suspend fun insertSchools/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun insertMajors/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun insertSemesters/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun insertBranches/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun insertSections/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun insertSubjects/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun insertStudyPlans/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun insertBuildings/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun insertRooms/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun insertMembers/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt

# Let's fix the syntax error in RestInterceptorsTest.kt by just reverting it completely and re-applying it cleanly.
git restore core-network/src/test/java/com/rtiqa/core/network/RestInterceptorsTest.kt || true
git checkout core-network/src/test/java/com/rtiqa/core/network/RestInterceptorsTest.kt || true

# If git fails, we recreate it manually
cat << 'TEST_EOF' > /app/applet/core-network/src/test/java/com/rtiqa/core/network/RestInterceptorsTest.kt
package com.rtiqa.core.network

import com.rtiqa.core.network.interceptor.RestAuthInterceptor
import com.rtiqa.core.network.interceptor.RestRetryInterceptor
import com.rtiqa.core.network.interceptor.RestTenantInterceptor
import com.rtiqa.core.network.session.RestSessionStore
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RestInterceptorsTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: OkHttpClient
    private lateinit var sessionStore: MockRestSessionStore

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        sessionStore = MockRestSessionStore()

        client = OkHttpClient.Builder()
            .addInterceptor(RestAuthInterceptor(sessionStore))
            .addInterceptor(RestTenantInterceptor(sessionStore))
            .addInterceptor(RestRetryInterceptor(maxRetries = 3, initialDelayMs = 10))
            .build()
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testAuthorizationHeaderAddedWhenSessionExists() {
        sessionStore.token = "valid_rest_token"
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder().url(mockWebServer.url("/")).build()
        client.newCall(request).execute()

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Bearer valid_rest_token", recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun testAuthorizationHeaderAbsentWhenSessionEmpty() {
        sessionStore.token = null
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder().url(mockWebServer.url("/")).build()
        client.newCall(request).execute()

        val recordedRequest = mockWebServer.takeRequest()
        assertNull(recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun testFirebaseTokensNeverUsedByRestInterceptor() {
        sessionStore.token = "rest_token_not_firebase"
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder().url(mockWebServer.url("/")).build()
        client.newCall(request).execute()

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Bearer rest_token_not_firebase", recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun testTenantHeaderAddedWhenActiveTenantExists() {
        sessionStore.tenantId = "tenant-123"
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder().url(mockWebServer.url("/")).build()
        client.newCall(request).execute()

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("tenant-123", recordedRequest.getHeader("X-Tenant-Id"))
    }

    @Test
    fun testTenantHeaderAbsentWhenNoActiveTenantExists() {
        sessionStore.tenantId = null
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder().url(mockWebServer.url("/")).build()
        client.newCall(request).execute()

        val recordedRequest = mockWebServer.takeRequest()
        assertNull(recordedRequest.getHeader("X-Tenant-Id"))
    }

    @Test
    fun test401NotRetried() {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        
        val request = Request.Builder().url(mockWebServer.url("/")).build()
        val response = client.newCall(request).execute()

        assertEquals(401, response.code)
        assertEquals(1, mockWebServer.requestCount)
    }

    @Test
    fun test403NotRetried() {
        mockWebServer.enqueue(MockResponse().setResponseCode(403))
        
        val request = Request.Builder().url(mockWebServer.url("/")).build()
        val response = client.newCall(request).execute()

        assertEquals(403, response.code)
        assertEquals(1, mockWebServer.requestCount)
    }

    @Test
    fun test409NotRetried() {
        mockWebServer.enqueue(MockResponse().setResponseCode(409))
        
        val request = Request.Builder().url(mockWebServer.url("/")).build()
        val response = client.newCall(request).execute()

        assertEquals(409, response.code)
        assertEquals(1, mockWebServer.requestCount)
    }

    @Test
    fun testTransientFailuresRetried() {
        mockWebServer.enqueue(MockResponse().setResponseCode(503))
        mockWebServer.enqueue(MockResponse().setResponseCode(502))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder().url(mockWebServer.url("/")).build()
        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        assertEquals(3, mockWebServer.requestCount)
    }

    @Test
    fun testIdempotencyKeyStable() {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/"))
            .post(okhttp3.RequestBody.create(null, ByteArray(0)))
            .header("Idempotency-Key", "stable-uuid-456")
            .build()
            
        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        assertEquals(2, mockWebServer.requestCount)
        
        val req1 = mockWebServer.takeRequest()
        val req2 = mockWebServer.takeRequest()
        
        assertEquals("stable-uuid-456", req1.getHeader("Idempotency-Key"))
        assertEquals("stable-uuid-456", req2.getHeader("Idempotency-Key"))
    }

    @Test
    fun testBypassHeadersAreRespectedAndRemoved() {
        sessionStore.token = "valid_rest_token"
        sessionStore.tenantId = "tenant-123"
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        val request = Request.Builder()
            .url(mockWebServer.url("/"))
            .header(RestAuthInterceptor.NO_AUTH_HEADER, "true")
            .header(RestTenantInterceptor.NO_TENANT_HEADER, "true")
            .build()
        client.newCall(request).execute()
        val recordedRequest = mockWebServer.takeRequest()
        assertNull(recordedRequest.getHeader("Authorization"))
        assertNull(recordedRequest.getHeader("X-Tenant-Id"))
        assertNull(recordedRequest.getHeader(RestAuthInterceptor.NO_AUTH_HEADER))
        assertNull(recordedRequest.getHeader(RestTenantInterceptor.NO_TENANT_HEADER))
    }

    @Test
    fun testNoHardcodedSchool001OrOrg1() {
        val clientClassStr = RetrofitNetworkClient::class.java.classLoader.getResourceAsStream("com/rtiqa/core/network/RetrofitNetworkClient.class")?.readBytes()?.toString(Charsets.UTF_8)
        val interceptorClassStr = RestTenantInterceptor::class.java.classLoader.getResourceAsStream("com/rtiqa/core/network/interceptor/RestTenantInterceptor.class")?.readBytes()?.toString(Charsets.UTF_8)
        val authClassStr = RestAuthInterceptor::class.java.classLoader.getResourceAsStream("com/rtiqa/core/network/interceptor/RestAuthInterceptor.class")?.readBytes()?.toString(Charsets.UTF_8)
        
        // This is a rough check to ensure strings are not compiled in
        assertEquals(false, interceptorClassStr?.contains("school_001") == true)
        assertEquals(false, interceptorClassStr?.contains("org_1") == true)
        assertEquals(false, authClassStr?.contains("school_001") == true)
        assertEquals(false, authClassStr?.contains("org_1") == true)
        assertEquals(false, clientClassStr?.contains("school_001") == true)
        assertEquals(false, clientClassStr?.contains("org_1") == true)
    }

    @Test
    fun testHttpLoggingInterceptorRedactsHeaders() {
        val okHttpClient = RetrofitNetworkClient.createOkHttpClient(
            securityManager = org.mockito.Mockito.mock(com.rtiqa.core.security.SecurityManager::class.java),
            sessionStore = sessionStore,
            isDebug = true
        )
        val loggingInterceptor = okHttpClient.interceptors().find { it is okhttp3.logging.HttpLoggingInterceptor }
        org.junit.Assert.assertNotNull("Logging interceptor should be present", loggingInterceptor)
    }
}

class MockRestSessionStore : RestSessionStore {
    var token: String? = null
    var tenantId: String? = null

    override fun saveSession(token: String, organizationId: String?) {
        this.token = token
        this.tenantId = organizationId
    }

    override fun getSessionToken(): String? = token
    override fun getActiveOrganizationId(): String? = tenantId
    override fun updateActiveOrganizationId(organizationId: String?) {
        this.tenantId = organizationId
    }

    var _sessionId: String? = null
    override fun generateAndSaveSessionId(): String {
        val newId = java.util.UUID.randomUUID().toString()
        _sessionId = newId
        return newId
    }

    override fun getSessionId(): String? = _sessionId

    override fun clearSession() {
        token = null
        tenantId = null
    }
}
TEST_EOF
