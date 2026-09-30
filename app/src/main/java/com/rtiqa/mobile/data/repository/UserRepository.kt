package com.rtiqa.mobile.data.repository

import com.rtiqa.core.domain.repository.AuthRemoteDataSource
import com.rtiqa.core.domain.repository.RemoteSyncDataSource
import com.rtiqa.mobile.data.local.dao.UserProfileDao
import com.rtiqa.mobile.data.local.entity.UserProfileEntity
import com.rtiqa.mobile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(
    private val userProfileDao: UserProfileDao,
    private val authDataSource: AuthRemoteDataSource,
    private val syncDataSource: RemoteSyncDataSource
) {

    val userProfile: Flow<UserProfile> = userProfileDao.getUserProfile().map { entity ->
        entity?.toDomain() ?: getCurrentRemoteUserProfile() ?: UserProfile()
    }

    private suspend fun getCurrentRemoteUserProfile(): UserProfile? {
        val uid = authDataSource.getCurrentUserId() ?: return null
        return UserProfile(
            id = uid,
            name = "",
            email = "",
            avatarResName = "img_ai_tutor_avatar_1785095337393",
            xp = 0,
            coins = 0,
            level = 1,
            streakDays = 1,
            isAdmin = false
        )
    }

    suspend fun createUser(id: String, name: String, email: String): UserProfile {
        val entity = UserProfileEntity(
            id = id,
            name = name,
            email = email,
            avatarResName = "img_ai_tutor_avatar_1785095337393",
            xp = 0,
            coins = 0,
            level = 1,
            streakDays = 1,
            currentGoal = "",
            language = "ar",
            isOfflineAutoSyncEnabled = true,
            isDarkMode = true
        )
        userProfileDao.saveUserProfile(entity)
        syncWithFirestore(entity.toDomain())
        return entity.toDomain()
    }

    suspend fun fetchUserProfileFromFirestore(userId: String): UserProfile? {
        val result = syncDataSource.fetchUserProfileFromCloud(userId)
        if (result is com.rtiqa.core.domain.result.RtiqaResult.Success && result.data != null) {
            val coreProfile = result.data!!
            val entity = UserProfileEntity(
                id = coreProfile.id,
                name = coreProfile.name,
                email = coreProfile.email,
                avatarResName = "img_ai_tutor_avatar_1785095337393",
                xp = coreProfile.levelXp,
                coins = 0,
                level = (coreProfile.levelXp / 100).coerceAtLeast(1),
                streakDays = coreProfile.streakDays,
                currentGoal = "",
                language = "ar",
                isOfflineAutoSyncEnabled = true,
                isDarkMode = true
            )
            userProfileDao.saveUserProfile(entity)
            return entity.toDomain(isAdmin = coreProfile.isAdmin)
        }
        return null
    }

    suspend fun syncWithFirestore(profile: UserProfile) {
        val coreProfile = com.rtiqa.core.domain.model.UserProfile(
            id = profile.id,
            name = profile.name,
            email = profile.email,
            avatarUrl = null,
            levelXp = profile.xp,
            streakDays = profile.streakDays,
            isAdmin = profile.isAdmin,
            isOfflineModeEnabled = profile.isOfflineAutoSyncEnabled
        )
        syncDataSource.syncUserProfileToCloud(coreProfile)
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        val entity = UserProfileEntity(
            id = profile.id,
            name = profile.name,
            email = profile.email,
            avatarResName = profile.avatarResName,
            xp = profile.xp,
            coins = profile.coins,
            level = profile.level,
            streakDays = profile.streakDays,
            currentGoal = profile.currentGoal,
            language = profile.language,
            isOfflineAutoSyncEnabled = profile.isOfflineAutoSyncEnabled,
            isDarkMode = profile.isDarkMode
        )
        userProfileDao.saveUserProfile(entity)
        syncWithFirestore(profile)
    }

    suspend fun addRewards(xpGained: Int, coinsGained: Int) {
        val activeId = authDataSource.getCurrentUserId() ?: ""
        userProfileDao.addRewards(activeId, xpGained, coinsGained)
    }

    suspend fun updateLanguage(language: String) {
        val activeId = authDataSource.getCurrentUserId() ?: ""
        userProfileDao.updateLanguage(activeId, language)
    }

    suspend fun updateTheme(isDark: Boolean) {
        val activeId = authDataSource.getCurrentUserId() ?: ""
        userProfileDao.updateTheme(activeId, isDark)
    }

    suspend fun updateOfflineAutoSync(enabled: Boolean) {
        val activeId = authDataSource.getCurrentUserId() ?: ""
        userProfileDao.updateOfflineAutoSync(activeId, enabled)
    }

    private fun UserProfileEntity.toDomain(isAdmin: Boolean = false) = UserProfile(
        id = id,
        name = name,
        email = email,
        avatarResName = avatarResName,
        xp = xp,
        coins = coins,
        level = level,
        streakDays = streakDays,
        currentGoal = currentGoal,
        language = language,
        isOfflineAutoSyncEnabled = isOfflineAutoSyncEnabled,
        isDarkMode = isDarkMode,
        isAdmin = isAdmin
    )
}
