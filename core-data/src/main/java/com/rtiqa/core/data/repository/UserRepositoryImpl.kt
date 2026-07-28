package com.rtiqa.core.data.repository

import com.rtiqa.core.data.mapper.toDomain
import com.rtiqa.core.database.dao.UserProfileDao
import com.rtiqa.core.database.entity.UserProfileEntity
import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.repository.UserRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val userProfileDao: UserProfileDao
) : UserRepositoryContract {

    override fun getUserProfile(): Flow<UserProfile?> {
        return userProfileDao.getUserProfile().map { it?.toDomain() }
    }

    override suspend fun updateUserProfile(profile: UserProfile): RtiqaResult<Unit> {
        return try {
            userProfileDao.insertOrUpdateProfile(
                UserProfileEntity(
                    id = profile.id,
                    name = profile.name,
                    email = profile.email,
                    avatarUrl = profile.avatarUrl,
                    levelXp = profile.levelXp,
                    streakDays = profile.streakDays,
                    isAdmin = profile.isAdmin,
                    isOfflineModeEnabled = profile.isOfflineModeEnabled
                )
            )
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.DatabaseError("Failed to update user profile", e))
        }
    }

    override suspend fun addXp(amount: Int): RtiqaResult<Unit> {
        val current = getUserProfile().firstOrNull() ?: UserProfile("1", "User", "user@rtiqa.com")
        val updated = current.addXp(amount)
        return updateUserProfile(updated)
    }

    override suspend fun incrementStreak(): RtiqaResult<Unit> {
        val current = getUserProfile().firstOrNull() ?: UserProfile("1", "User", "user@rtiqa.com")
        val updated = current.incrementStreak()
        return updateUserProfile(updated)
    }
}
