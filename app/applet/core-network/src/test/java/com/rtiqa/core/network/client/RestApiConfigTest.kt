package com.rtiqa.core.network.client

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RestApiConfigTest {
    @Test
    fun `API base URL configuration works and defaults to HTTPS`() {
        val baseUrl = RestApiConfig.getBaseUrl()
        assertNotNull(baseUrl)
        assertTrue("Base URL should use HTTPS", baseUrl.startsWith("https://"))
    }
}
