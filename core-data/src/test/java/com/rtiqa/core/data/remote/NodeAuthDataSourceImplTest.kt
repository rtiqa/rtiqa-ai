package com.rtiqa.core.data.remote

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.network.api.RestApiContract
import com.rtiqa.core.network.api.RestLoginRequest
import com.rtiqa.core.network.api.RestLoginResponse
import com.rtiqa.core.network.api.RestUserDto
import com.rtiqa.core.network.session.RestSessionStore
import com.rtiqa.core.logging.RtiqaLog
import com.rtiqa.core.logging.RtiqaLogger
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NodeAuthDataSourceImplTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var mockApi: RestApiContract
    private lateinit var mockSessionStore: MockRestSessionStore
    private lateinit var dataSource: NodeAuthDataSourceImpl

    @Before
    fun setup() {
        RtiqaLog.initialize(object : RtiqaLogger {
            override fun d(tag: String, message: String) {}
            override fun i(tag: String, message: String) {}
            override fun w(tag: String, message: String, throwable: Throwable?) {}
            override fun e(tag: String, message: String, throwable: Throwable?) {}
        })

        mockWebServer = MockWebServer()
        mockWebServer.start()

        mockApi = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()))
            .build()
            .create(RestApiContract::class.java)

        mockSessionStore = MockRestSessionStore()
        dataSource = NodeAuthDataSourceImpl(mockApi, mockSessionStore)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `successful login persists REST token and active tenant`() = runTest {
        val successBody = """
            {
                "token": "fake_token",
                "user": {
                    "id": "user123",
                    "email": "test@example.com",
                    "role": "student",
                    "full_name": "Test User"
                },
                "organization_id": "org_456"
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(successBody))
        
        val result = dataSource.login("test@example.com", "password")
        
        assertTrue(result is RtiqaResult.Success)
        val data = (result as RtiqaResult.Success).data
        assertEquals("user123", data.uid)
        assertEquals("test@example.com", data.email)
        assertEquals("Test User", data.displayName)
        
        assertEquals("fake_token", mockSessionStore.getSessionToken())
        assertEquals("org_456", mockSessionStore.getActiveOrganizationId())
    }

    @Test
    fun `invalid credentials map correctly to AuthError`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(401).setBody("Unauthorized"))
        
        val result = dataSource.login("test@example.com", "wrong")
        
        assertTrue(result is RtiqaResult.Error)
        val error = (result as RtiqaResult.Error).error
        assertTrue(error is RtiqaError.AuthError)
        assertTrue(error.message.contains("Invalid credentials"))
    }

    @Test
    fun `server error maps correctly to NetworkError`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Server Error"))
        
        val result = dataSource.login("test@example.com", "password")
        
        assertTrue(result is RtiqaResult.Error)
        val error = (result as RtiqaResult.Error).error
        assertTrue(error is RtiqaError.NetworkError)
        assertTrue(error.message.contains("Temporary server error"))
    }
    
    @Test
    fun `logout clears REST session`() = runTest {
        mockSessionStore.saveSession("token123", "org123")
        
        val result = dataSource.logout()
        assertTrue(result is RtiqaResult.Success)
        
        assertEquals(null, mockSessionStore.getSessionToken())
        assertEquals(null, mockSessionStore.getActiveOrganizationId())
    }

    class MockRestSessionStore : RestSessionStore {
        private var token: String? = null
        private var orgId: String? = null
        override fun saveSession(token: String, organizationId: String?) {
            this.token = token
            this.orgId = organizationId
        }
        override fun getSessionToken(): String? = token
        override fun getActiveOrganizationId(): String? = orgId
        override fun updateActiveOrganizationId(organizationId: String?) { this.orgId = organizationId }
        var _sessionId: String? = null
        override fun generateAndSaveSessionId(): String {
            val newId = java.util.UUID.randomUUID().toString()
            _sessionId = newId
            return newId
        }
        override fun getSessionId(): String? = _sessionId

        override fun clearSession() {
            this.token = null
            this.orgId = null
        }
    }
}
