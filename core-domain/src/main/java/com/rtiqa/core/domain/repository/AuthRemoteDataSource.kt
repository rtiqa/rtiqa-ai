package com.rtiqa.core.domain.repository

import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.result.RtiqaResult

/**
 * Abstraction for remote authentication operations.
 * Allows the application to authenticate users without being tightly coupled
 * to a specific backend provider (e.g., Firebase, Supabase, or a custom REST API).
 */
interface AuthRemoteDataSource {
    suspend fun login(email: String, pass: String): RtiqaResult<RemoteAuthUser>
    suspend fun register(name: String, email: String, pass: String): RtiqaResult<RemoteAuthUser>
    suspend fun resetPassword(email: String): RtiqaResult<Unit>
    suspend fun logout(): RtiqaResult<Unit>
    suspend fun getCurrentUserId(): String?
}

/**
 * Representation of a user returned from a remote authentication service.
 */
data class RemoteAuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?
)
