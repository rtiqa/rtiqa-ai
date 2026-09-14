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
