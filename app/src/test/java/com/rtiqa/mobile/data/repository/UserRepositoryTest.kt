package com.rtiqa.mobile.data.repository

import com.rtiqa.core.domain.model.UserProfile as CoreUserProfile
import com.rtiqa.core.domain.repository.AuthRemoteDataSource
import com.rtiqa.core.domain.repository.RemoteAuthUser
import com.rtiqa.core.domain.repository.RemoteSyncDataSource
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.mobile.data.local.dao.UserProfileDao
import com.rtiqa.mobile.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserRepositoryTest {

    private lateinit var fakeDao: FakeUserProfileDao
    private lateinit var fakeAuthDataSource: FakeAuthRemoteDataSource
    private lateinit var fakeSyncDataSource: FakeRemoteSyncDataSource
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        fakeDao = FakeUserProfileDao()
        fakeAuthDataSource = FakeAuthRemoteDataSource()
        fakeSyncDataSource = FakeRemoteSyncDataSource()
        userRepository = UserRepository(
            userProfileDao = fakeDao,
            authDataSource = fakeAuthDataSource,
            syncDataSource = fakeSyncDataSource
        )
    }

    @Test
    fun rtiqaEduEmail_alone_doesNotMakeUserAdmin() = runTest {
        val profile = userRepository.createUser(
            id = "user_1",
            name = "Test User",
            email = "teacher@rtiqa.edu"
        )
        assertFalse("An @rtiqa.edu email alone must NOT grant admin privilege", profile.isAdmin)
    }

    @Test
    fun irtiqahqEmail_alone_doesNotMakeUserAdmin() = runTest {
        val profile = userRepository.createUser(
            id = "user_2",
            name = "Test User",
            email = "irtiqahq@gmail.com"
        )
        assertFalse("irtiqahq@gmail.com alone must NOT grant admin privilege", profile.isAdmin)
    }

    @Test
    fun normalEmail_isNonAdmin_whenNoTrustedAdminRoleExists() = runTest {
        val entity = UserProfileEntity(
            id = "user_3",
            name = "Standard Student",
            email = "student@gmail.com",
            avatarResName = "avatar",
            xp = 50,
            coins = 10,
            level = 1,
            streakDays = 2,
            currentGoal = "Learn",
            language = "ar",
            isOfflineAutoSyncEnabled = true,
            isDarkMode = true
        )
        fakeDao.saveUserProfile(entity)

        val profile = userRepository.userProfile.first()
        assertFalse("Normal user must be non-admin when no trusted role exists", profile.isAdmin)
    }

    @Test
    fun fetchUserProfileFromFirestore_preservesTrustedAdminStatus_whenTrue() = runTest {
        val cloudProfile = CoreUserProfile(
            id = "admin_user",
            name = "Real Admin",
            email = "custom_admin@domain.com",
            isAdmin = true
        )
        fakeSyncDataSource.cloudProfileToReturn = cloudProfile

        val result = userRepository.fetchUserProfileFromFirestore("admin_user")
        assertTrue("Trusted admin status from cloud source must be preserved", result?.isAdmin == true)
    }

    @Test
    fun fetchUserProfileFromFirestore_preservesTrustedAdminStatus_whenFalse() = runTest {
        val cloudProfile = CoreUserProfile(
            id = "regular_user",
            name = "Regular Staff",
            email = "staff@rtiqa.edu",
            isAdmin = false
        )
        fakeSyncDataSource.cloudProfileToReturn = cloudProfile

        val result = userRepository.fetchUserProfileFromFirestore("regular_user")
        assertFalse("Non-admin status from cloud must be preserved even if email ends with @rtiqa.edu", result?.isAdmin == true)
    }

    @Test
    fun userProfile_localPath_emitsLocalProfile() = runTest {
        val entity = UserProfileEntity(
            id = "local_123",
            name = "Local User",
            email = "local@example.com",
            avatarResName = "avatar",
            xp = 120,
            coins = 20,
            level = 2,
            streakDays = 5,
            currentGoal = "Daily",
            language = "ar",
            isOfflineAutoSyncEnabled = true,
            isDarkMode = false
        )
        fakeDao.saveUserProfile(entity)

        val profile = userRepository.userProfile.first()
        assertEquals("local_123", profile.id)
        assertEquals("Local User", profile.name)
        assertFalse("Local user must default to non-admin", profile.isAdmin)
    }

    @Test
    fun userProfile_remoteFallbackPath_emitsRemoteUidWithoutBlocking() = runTest {
        fakeDao.clear()
        fakeAuthDataSource.currentUserId = "remote_uid_999"

        val profile = userRepository.userProfile.first()
        assertEquals("remote_uid_999", profile.id)
        assertFalse("Fallback remote user must default to non-admin", profile.isAdmin)
    }

    @Test
    fun userProfile_whenNoLocalOrRemoteUser_emitsDefaultProfile() = runTest {
        fakeDao.clear()
        fakeAuthDataSource.currentUserId = null

        val profile = userRepository.userProfile.first()
        assertEquals("", profile.id)
        assertFalse("Default profile must default to non-admin", profile.isAdmin)
    }

    private class FakeUserProfileDao : UserProfileDao {
        private val state = MutableStateFlow<UserProfileEntity?>(null)

        fun clear() {
            state.value = null
        }

        override fun getUserProfile(): Flow<UserProfileEntity?> = state

        override fun getUserProfileById(id: String): Flow<UserProfileEntity?> = state

        override suspend fun saveUserProfile(profile: UserProfileEntity) {
            state.value = profile
        }

        override suspend fun addRewards(id: String, xpGained: Int, coinsGained: Int) {}
        override suspend fun updateLanguage(id: String, lang: String) {}
        override suspend fun updateTheme(id: String, isDark: Boolean) {}
        override suspend fun updateOfflineAutoSync(id: String, enabled: Boolean) {}
    }

    private class FakeAuthRemoteDataSource : AuthRemoteDataSource {
        var currentUserId: String? = "test_user_id"
        override suspend fun login(email: String, pass: String): RtiqaResult<RemoteAuthUser> =
            RtiqaResult.Success(RemoteAuthUser("test_user_id", email, "Test User"))
        override suspend fun register(name: String, email: String, pass: String): RtiqaResult<RemoteAuthUser> =
            RtiqaResult.Success(RemoteAuthUser("test_user_id", email, name))
        override suspend fun resetPassword(email: String): RtiqaResult<Unit> = RtiqaResult.Success(Unit)
        override suspend fun logout(): RtiqaResult<Unit> = RtiqaResult.Success(Unit)
        override suspend fun getCurrentUserId(): String? = currentUserId
    }

    private class FakeRemoteSyncDataSource : RemoteSyncDataSource {
        var cloudProfileToReturn: CoreUserProfile? = null
        var syncedProfile: CoreUserProfile? = null

        override fun isAvailable(): Boolean = true

        override suspend fun syncUserProfileToCloud(profile: CoreUserProfile): RtiqaResult<Unit> {
            syncedProfile = profile
            return RtiqaResult.Success(Unit)
        }

        override suspend fun fetchUserProfileFromCloud(userId: String): RtiqaResult<CoreUserProfile?> {
            return RtiqaResult.Success(cloudProfileToReturn)
        }

        override suspend fun syncCourseProgressToCloud(
            userId: String,
            courseId: String,
            progressPercent: Float,
            completedLessonsCount: Int
        ): RtiqaResult<Unit> = RtiqaResult.Success(Unit)

        override suspend fun syncQuizResultToCloud(
            userId: String,
            quizId: String,
            score: Int,
            totalQuestions: Int
        ): RtiqaResult<Unit> = RtiqaResult.Success(Unit)

        override suspend fun syncUserSettingsToCloud(
            userId: String,
            darkTheme: Boolean,
            notificationsEnabled: Boolean,
            offlineMode: Boolean
        ): RtiqaResult<Unit> = RtiqaResult.Success(Unit)

        override suspend fun pushSyncPayload(
            collection: String,
            documentId: String,
            payload: Map<String, Any>
        ): RtiqaResult<Unit> = RtiqaResult.Success(Unit)
    }
}
