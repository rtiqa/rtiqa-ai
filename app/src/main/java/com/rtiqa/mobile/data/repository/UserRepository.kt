package com.rtiqa.mobile.data.repository

import com.rtiqa.mobile.data.local.dao.UserProfileDao
import com.rtiqa.mobile.data.local.entity.UserProfileEntity
import com.rtiqa.mobile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val userProfileDao: UserProfileDao) {

    val userProfile: Flow<UserProfile> = userProfileDao.getUserProfile().map { entity ->
        entity?.toDomain() ?: UserProfile()
    }

    suspend fun addRewards(xpGained: Int, coinsGained: Int) {
        userProfileDao.addRewards("user_001", xpGained, coinsGained)
    }

    suspend fun updateLanguage(language: String) {
        userProfileDao.updateLanguage("user_001", language)
    }

    suspend fun updateTheme(isDark: Boolean) {
        userProfileDao.updateTheme("user_001", isDark)
    }

    suspend fun updateOfflineAutoSync(enabled: Boolean) {
        userProfileDao.updateOfflineAutoSync("user_001", enabled)
    }

    private fun UserProfileEntity.toDomain() = UserProfile(
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
        isDarkMode = isDarkMode
    )
}
