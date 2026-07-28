package com.rtiqa.core.di

import android.content.Context
import com.rtiqa.core.crash.CrashReporter
import com.rtiqa.core.crash.ProductionCrashReporter
import com.rtiqa.core.dispatcher.DefaultDispatcherProvider
import com.rtiqa.core.dispatcher.DispatcherProvider
import com.rtiqa.core.logging.ProductionLogger
import com.rtiqa.core.logging.RtiqaLogger
import com.rtiqa.core.security.EncryptedSecurityManager
import com.rtiqa.core.security.SecurityManager

/**
 * Core Dependency Injection Container providing app-wide singleton instances.
 */
class RtiqaCoreDiContainer(
    val context: Context
) {
    val logger: RtiqaLogger by lazy {
        ProductionLogger(isDebug = true)
    }

    val crashReporter: CrashReporter by lazy {
        ProductionCrashReporter()
    }

    val securityManager: SecurityManager by lazy {
        EncryptedSecurityManager(context)
    }

    val dispatcherProvider: DispatcherProvider by lazy {
        DefaultDispatcherProvider()
    }
}
