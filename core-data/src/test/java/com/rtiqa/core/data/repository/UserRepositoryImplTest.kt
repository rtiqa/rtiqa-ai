package com.rtiqa.core.data.repository

import com.rtiqa.core.database.dao.UserProfileDao
import com.rtiqa.core.database.entity.UserProfileEntity
import com.rtiqa.core.domain.model.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UserRepositoryImplTest {

    private class FakeUserProfileDao : UserProfileDao {
        var profileEntity: UserProfileEntity? = UserProfileEntity("u1", "Alex", "alex@rtiqa.com", null, 100, 3)

        override fun getUserProfile() = flowOf(profileEntity)
        override suspend fun insertOrUpdateProfile(profile: UserProfileEntity) {
            profileEntity = profile
        }
        override suspend fun clearUserProfile() {
            profileEntity = null
        }
    }

    @Test
    fun getUserProfile_returnsDomainModel() = runTest {
        val repository = UserRepositoryImpl(FakeUserProfileDao())
        val user = repository.getUserProfile().first()

        assertNotNull(user)
        assertEquals("Alex", user?.name)
        assertEquals(100, user?.levelXp)
    }

    @Test
    fun addXp_incrementsUserXp() = runTest {
        val dao = FakeUserProfileDao()
        val repository = UserRepositoryImpl(dao)

        repository.addXp(50)

        val updated = repository.getUserProfile().first()
        assertEquals(150, updated?.levelXp)
    }

    @Test
    fun incrementStreak_incrementsStreakDays() = runTest {
        val dao = FakeUserProfileDao()
        val repository = UserRepositoryImpl(dao)

        repository.incrementStreak()

        val updated = repository.getUserProfile().first()
        assertEquals(4, updated?.streakDays)
    }
}
