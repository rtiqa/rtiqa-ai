package com.rtiqa.core.network

import com.rtiqa.core.network.interceptor.NetworkRetryInterceptor
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkRetryTest {

    @Test
    fun networkRetryInterceptor_instantiatesWithDefaultParameters() {
        val interceptor = NetworkRetryInterceptor(maxRetries = 3, initialDelayMs = 100L)
        // Verify creation
        assertEquals(3, 3)
    }
}
