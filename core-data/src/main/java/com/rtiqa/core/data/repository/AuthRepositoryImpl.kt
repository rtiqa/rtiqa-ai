package com.rtiqa.core.data.repository

import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.data.mapper.toDomain
import com.rtiqa.core.database.dao.UserProfileDao
import com.rtiqa.core.database.entity.UserProfileEntity
import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.repository.AuthRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.network.api.LoginRequestDto
import com.rtiqa.core.network.api.RegisterRequestDto
import com.rtiqa.core.network.api.RtiqaApiService
import com.rtiqa.core.security.SecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Production implementation of AuthRepositoryContract managing authentication state,
 * secure token storage, and offline fallback authentication.
 */
class AuthRepositoryImpl(
    private val apiService: RtiqaApiService,
    private val userProfileDao: UserProfileDao,
    private val preferencesDataStore: RtiqaPreferencesDataStore,
    private val securityManager: SecurityManager
) : AuthRepositoryContract {

    override fun observeUserSession(): Flow<UserProfile?> {
        return userProfileDao.getUserProfile().map { it?.toDomain() }
    }

    override suspend fun login(email: String, pass: String): RtiqaResult<UserProfile> {
        return try {
            val response = apiService.login(LoginRequestDto(email = email, passwordHash = pass))
            if (response.isSuccessful && response.body() != null) {
                val authBody = response.body()!!
                val netUser = authBody.user

                securityManager.putEncryptedString(KEY_AUTH_TOKEN, authBody.token)
                securityManager.putEncryptedString(KEY_USER_ID, netUser.id)
                preferencesDataStore.setActiveUserId(netUser.id)

                val entity = UserProfileEntity(
                    id = netUser.id,
                    name = netUser.name,
                    email = netUser.email,
                    levelXp = netUser.totalXp,
                    streakDays = netUser.streakCount
                )
                userProfileDao.insertOrUpdateProfile(entity)
                RtiqaResult.Success(entity.toDomain())
            } else {
                // Offline fallback authentication check
                val existingLocalProfile = userProfileDao.getUserProfile().firstOrNull()?.toDomain()
                if (existingLocalProfile != null && existingLocalProfile.email.equals(email, ignoreCase = true)) {
                    preferencesDataStore.setActiveUserId(existingLocalProfile.id)
                    securityManager.putEncryptedString(KEY_USER_ID, existingLocalProfile.id)
                    RtiqaResult.Success(existingLocalProfile)
                } else {
                    RtiqaResult.Error(RtiqaError.AuthError("Invalid credentials or user not found offline."))
                }
            }
        } catch (e: Exception) {
            // Network failure fallback
            val existingLocalProfile = userProfileDao.getUserProfile().firstOrNull()?.toDomain()
            if (existingLocalProfile != null && existingLocalProfile.email.equals(email, ignoreCase = true)) {
                preferencesDataStore.setActiveUserId(existingLocalProfile.id)
                securityManager.putEncryptedString(KEY_USER_ID, existingLocalProfile.id)
                RtiqaResult.Success(existingLocalProfile)
            } else {
                RtiqaResult.Error(RtiqaError.NetworkError("Authentication failed due to connectivity.", cause = e))
            }
        }
    }

    override suspend fun register(name: String, email: String, pass: String): RtiqaResult<UserProfile> {
        return try {
            val response = apiService.register(RegisterRequestDto(name = name, email = email, passwordHash = pass))
            if (response.isSuccessful && response.body() != null) {
                val authBody = response.body()!!
                val netUser = authBody.user

                securityManager.putEncryptedString(KEY_AUTH_TOKEN, authBody.token)
                securityManager.putEncryptedString(KEY_USER_ID, netUser.id)
                preferencesDataStore.setActiveUserId(netUser.id)

                val entity = UserProfileEntity(
                    id = netUser.id,
                    name = netUser.name,
                    email = netUser.email,
                    levelXp = netUser.totalXp,
                    streakDays = netUser.streakCount
                )
                userProfileDao.insertOrUpdateProfile(entity)
                RtiqaResult.Success(entity.toDomain())
            } else {
                // Local registration creation for offline availability
                val newUserId = UUID.randomUUID().toString()
                val entity = UserProfileEntity(
                    id = newUserId,
                    name = name,
                    email = email,
                    levelXp = 0,
                    streakDays = 0
                )
                userProfileDao.insertOrUpdateProfile(entity)
                preferencesDataStore.setActiveUserId(newUserId)
                securityManager.putEncryptedString(KEY_USER_ID, newUserId)
                securityManager.putEncryptedString(KEY_AUTH_TOKEN, "offline_token_$newUserId")
                RtiqaResult.Success(entity.toDomain())
            }
        } catch (e: Exception) {
            // Local offline registration creation
            val newUserId = UUID.randomUUID().toString()
            val entity = UserProfileEntity(
                id = newUserId,
                name = name,
                email = email,
                levelXp = 0,
                streakDays = 0
            )
            userProfileDao.insertOrUpdateProfile(entity)
            preferencesDataStore.setActiveUserId(newUserId)
            securityManager.putEncryptedString(KEY_USER_ID, newUserId)
            securityManager.putEncryptedString(KEY_AUTH_TOKEN, "offline_token_$newUserId")
            RtiqaResult.Success(entity.toDomain())
        }
    }

    override suspend fun logout(): RtiqaResult<Unit> {
        return try {
            securityManager.removeKey(KEY_AUTH_TOKEN)
            securityManager.removeKey(KEY_USER_ID)
            preferencesDataStore.setActiveUserId(null)
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.UnknownError("Failed to logout cleanly.", e))
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return securityManager.getEncryptedString(KEY_USER_ID)
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
    }
}
