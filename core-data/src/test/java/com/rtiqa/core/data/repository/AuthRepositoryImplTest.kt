package com.rtiqa.core.data.repository

import androidx.test.core.app.ApplicationProvider
import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.database.dao.UserProfileDao
import com.rtiqa.core.database.entity.UserProfileEntity
import com.rtiqa.core.network.api.AuthResponseDto
import com.rtiqa.core.network.api.LoginRequestDto
import com.rtiqa.core.network.api.NetworkCourseDto
import com.rtiqa.core.network.api.NetworkLessonDto
import com.rtiqa.core.network.api.NetworkSyncPayloadDto
import com.rtiqa.core.network.api.NetworkSyncResponseDto
import com.rtiqa.core.network.api.NetworkUserDto
import com.rtiqa.core.network.api.RegisterRequestDto
import com.rtiqa.core.network.api.RtiqaApiService
import com.rtiqa.core.security.SecurityManager
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
class AuthRepositoryImplTest {

    private class FakeApiService : RtiqaApiService {
        override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> {
            val user = NetworkUserDto("u1", request.email, "Alex", 5, 200)
            return Response.success(AuthResponseDto("fake_jwt_token", user))
        }

        override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> {
            val user = NetworkUserDto("u2", request.email, request.name, 0, 0)
            return Response.success(AuthResponseDto("fake_jwt_token_2", user))
        }

        override suspend fun getUserProfile(): Response<NetworkUserDto> {
            return Response.success(NetworkUserDto("u1", "alex@rtiqa.com", "Alex", 5, 200))
        }

        override suspend fun getCourses(category: String?): Response<List<NetworkCourseDto>> {
            return Response.success(emptyList())
        }

        override suspend fun getCourseLessons(courseId: String): Response<List<NetworkLessonDto>> {
            return Response.success(emptyList())
        }

        override suspend fun syncOfflineData(payload: NetworkSyncPayloadDto): Response<NetworkSyncResponseDto> {
            return Response.success(NetworkSyncResponseDto(true, System.currentTimeMillis(), "Synced"))
        }
    }

    private class FakeUserProfileDao : UserProfileDao {
        private var entity: UserProfileEntity? = null
        override fun getUserProfile() = flowOf(entity)
        override suspend fun insertOrUpdateProfile(profile: UserProfileEntity) {
            entity = profile
        }
    }

    private class FakeSecurityManager : SecurityManager {
        private val map = mutableMapOf<String, String>()
        override fun putEncryptedString(key: String, value: String) { map[key] = value }
        override fun getEncryptedString(key: String, defaultValue: String?) = map[key] ?: defaultValue
        override fun removeKey(key: String) { map.remove(key) }
        override fun clearAll() { map.clear() }
    }

    @Test
    fun login_storesTokenAndProfile() = runTest {
        val apiService = FakeApiService()
        val userProfileDao = FakeUserProfileDao()
        val securityManager = FakeSecurityManager()

        val repo = AuthRepositoryImpl(
            apiService = apiService,
            userProfileDao = userProfileDao,
            preferencesDataStore = FakeDataStore(ApplicationProvider.getApplicationContext()),
            securityManager = securityManager
        )

        val result = repo.login("alex@rtiqa.com", "password")
        assertTrue(result is com.rtiqa.core.domain.result.RtiqaResult.Success)
        assertEquals("fake_jwt_token", securityManager.getEncryptedString("auth_token"))
    }

    private class FakeDataStore(context: android.content.Context) : RtiqaPreferencesDataStore(context) {
        private var activeUser: String? = null
        override suspend fun setActiveUserId(userId: String?) {
            activeUser = userId
        }
    }
}
