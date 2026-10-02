package com.rtiqa.core.data.repository

import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.data.mapper.toDomain
import com.rtiqa.core.database.dao.UserProfileDao
import com.rtiqa.core.database.entity.UserProfileEntity
import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.repository.AuthRemoteDataSource
import com.rtiqa.core.domain.repository.AuthRepositoryContract
import com.rtiqa.core.domain.repository.RemoteSyncDataSource
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.network.api.LoginRequestDto
import com.rtiqa.core.network.api.NetworkUserDto
import com.rtiqa.core.network.api.RegisterRequestDto
import com.rtiqa.core.network.api.RtiqaApiService
import com.rtiqa.core.security.SecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

import com.rtiqa.core.network.session.RestSessionStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Production implementation of AuthRepositoryContract managing Remote Authentication,
 * Cloud synchronization of user profile, secure token storage, and offline-first fallback authentication.
 */
class AuthRepositoryImpl(
    private val database: com.rtiqa.core.database.RtiqaDatabase,
    private val apiService: RtiqaApiService,
    private val userProfileDao: UserProfileDao,
    private val preferencesDataStore: RtiqaPreferencesDataStore,
    private val securityManager: SecurityManager,
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val remoteSyncDataSource: RemoteSyncDataSource? = null,
    private val sessionStore: RestSessionStore,
    private val syncMutex: Mutex
) : AuthRepositoryContract {

    override fun observeUserSession(): Flow<UserProfile?> {
        return combine(
            userProfileDao.getUserProfile(),
            preferencesDataStore.userPreferencesFlow
        ) { profile, preferences ->
            val token = sessionStore.getSessionToken()
            val storedUserId = securityManager.getEncryptedString(KEY_USER_ID)
            if (
                profile != null &&
                !token.isNullOrBlank() &&
                preferences.activeUserId == profile.id &&
                storedUserId == profile.id
            ) {
                profile.toDomain()
            } else {
                null
            }
        }
    }

    private suspend fun persistAuthenticatedUser(
        token: String,
        netUser: NetworkUserDto,
        organizationId: String?
    ): RtiqaResult.Success<UserProfile> {
        sessionStore.saveSession(token, organizationId)
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
        sessionStore.generateAndSaveSessionId()
        return RtiqaResult.Success(entity.toDomain())
    }

    override suspend fun login(email: String, pass: String): RtiqaResult<UserProfile> {
        val remoteAuth = authRemoteDataSource.login(email, pass)
        if (remoteAuth is RtiqaResult.Success) {
            val remoteUser = remoteAuth.data
            val uid = remoteUser.uid
            val name = remoteUser.displayName.takeIf { !it.isNullOrEmpty() } ?: email.substringBefore("@")
            
            if (sessionStore.getSessionToken().isNullOrBlank()) {
                return RtiqaResult.Error(RtiqaError.AuthError("Authentication succeeded without a valid server session."))
            }
            securityManager.putEncryptedString(KEY_USER_ID, uid)
            preferencesDataStore.setActiveUserId(uid)

            // Fetch remote profile if present
            val cloudFetch = remoteSyncDataSource?.fetchUserProfileFromCloud(uid)
            val cloudProfile = if (cloudFetch is RtiqaResult.Success) cloudFetch.data else null

            val entity = UserProfileEntity(
                id = uid,
                name = cloudProfile?.name ?: name,
                email = remoteUser.email ?: email,
                levelXp = cloudProfile?.levelXp ?: 100,
                streakDays = cloudProfile?.streakDays ?: 1
            )
            userProfileDao.insertOrUpdateProfile(entity)
            remoteSyncDataSource?.syncUserProfileToCloud(entity.toDomain())
            sessionStore.generateAndSaveSessionId()
            return RtiqaResult.Success(entity.toDomain())
        }
        if (remoteAuth is RtiqaResult.Error &&
            (remoteAuth.error is RtiqaError.AuthError || remoteAuth.error is RtiqaError.ValidationError)
        ) {
            return remoteAuth
        }

        // Fallback: REST API authentication
        return try {
            val response = apiService.login(LoginRequestDto(email = email, passwordHash = pass))
            if (response.isSuccessful && response.body() != null) {
                val authBody = response.body()!!
                persistAuthenticatedUser(authBody.token, authBody.user, authBody.organizationId)
            } else {
                RtiqaResult.Error(loginHttpError(response.code()))
            }
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.NetworkError("Authentication failed due to connectivity.", cause = e))
        }
    }

    override suspend fun register(name: String, email: String, pass: String): RtiqaResult<UserProfile> {
        return try {
            val response = apiService.register(RegisterRequestDto(name = name, email = email, passwordHash = pass))
            if (response.isSuccessful && response.body() != null) {
                val authBody = response.body()!!
                persistAuthenticatedUser(authBody.token, authBody.user, authBody.organizationId)
            } else {
                RtiqaResult.Error(registrationHttpError(response.code()))
            }
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.NetworkError("Registration failed due to connectivity.", cause = e))
        }
    }

    override suspend fun resetPassword(email: String): RtiqaResult<Unit> {
        return authRemoteDataSource.resetPassword(email)
    }

    override suspend fun logout(): RtiqaResult<Unit> {
        val remoteResult = try {
            authRemoteDataSource.logout()
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.UnknownError("Remote logout failed.", e))
        }

        return try {
            syncMutex.withLock {
                sessionStore.clearSession()
                securityManager.removeKey(KEY_AUTH_TOKEN)
                securityManager.removeKey(KEY_USER_ID)
                preferencesDataStore.setActiveUserId(null)
                userProfileDao.clearUserProfile()
                database.clearSensitiveData()
            }
            remoteResult
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.UnknownError("Failed to logout cleanly.", e))
        }
    }

    override suspend fun getCurrentUserId(): String? {
        val remoteUid = authRemoteDataSource.getCurrentUserId()
        if (remoteUid != null) return remoteUid
        return securityManager.getEncryptedString(KEY_USER_ID)
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
    }

    private fun loginHttpError(statusCode: Int): RtiqaError = when (statusCode) {
        400, 401, 403, 404, 422 -> RtiqaError.AuthError("Authentication rejected by server.", cause = null)
        else -> RtiqaError.NetworkError("Authentication request failed.", statusCode = statusCode)
    }

    private fun registrationHttpError(statusCode: Int): RtiqaError = when (statusCode) {
        400, 403, 409, 422 -> RtiqaError.AuthError("Registration rejected by server.", cause = null)
        else -> RtiqaError.NetworkError("Registration request failed.", statusCode = statusCode)
    }
}
