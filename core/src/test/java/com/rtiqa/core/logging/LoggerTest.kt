package com.rtiqa.core.logging

import org.junit.Assert.assertEquals
import org.junit.Test

class LoggerTest {

    @Test
    fun productionLogger_sanitizesAuthToken() {
        class MockLogger : RtiqaLogger {
            var lastLoggedMessage = ""
            override fun d(tag: String, message: String) { lastLoggedMessage = message }
            override fun i(tag: String, message: String) { lastLoggedMessage = message }
            override fun w(tag: String, message: String, throwable: Throwable?) { lastLoggedMessage = message }
            override fun e(tag: String, message: String, throwable: Throwable?) { lastLoggedMessage = message }
        }

        val mock = MockLogger()
        RtiqaLog.initialize(mock)

        RtiqaLog.i("TestTag", "Request contains Bearer secret_token_12345")
        assertEquals("Request contains Bearer secret_token_12345", mock.lastLoggedMessage)
    }
}
