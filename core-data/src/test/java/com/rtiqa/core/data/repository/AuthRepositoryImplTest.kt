package com.rtiqa.core.data.repository

import androidx.test.core.app.ApplicationProvider
import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.database.dao.UserProfileDao
import com.rtiqa.core.database.entity.UserProfileEntity
import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.network.api.AuthResponseDto
import com.rtiqa.core.network.api.LessonCompletionResponseDto
import com.rtiqa.core.network.api.LoginRequestDto
import com.rtiqa.core.network.api.NetworkCourseDto
import com.rtiqa.core.network.api.NetworkLessonDto
import com.rtiqa.core.network.api.NetworkSyncPayloadDto
import com.rtiqa.core.network.api.NetworkSyncResponseDto
import com.rtiqa.core.network.api.NetworkUserDto
import com.rtiqa.core.network.api.RegisterRequestDto
import com.rtiqa.core.network.api.RtiqaApiService
import com.rtiqa.core.security.SecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
class AuthRepositoryImplTest {

    private class DynamicFakeApiService : RtiqaApiService {
        var shouldFailLogin = false
        var shouldFailRegister = false
        var isNetworkDown = false
        var errorStatusCode = 401

        override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> {
            if (isNetworkDown) throw java.io.IOException("Network down")
            if (shouldFailLogin) {
                return Response.error(errorStatusCode, ResponseBody.create(null, "Error"))
            }
            val user = NetworkUserDto("u1", request.email, "Alex", 5, 200)
            return Response.success(AuthResponseDto("fake_jwt_token", user))
        }

        override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> {
            if (isNetworkDown) throw java.io.IOException("Network down")
            if (shouldFailRegister) {
                return Response.error(409, ResponseBody.create(null, "Conflict"))
            }
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

        override suspend fun completeLesson(courseId: String, lessonId: String): Response<LessonCompletionResponseDto> {
            return Response.success(
                LessonCompletionResponseDto(
                    success = true,
                    lessonId = lessonId,
                    courseId = courseId,
                    completed = true,
                    courseProgressPercent = 100.0f,
                    completedLessons = 1,
                    totalLessons = 1
                )
            )
        }
    }

    private class DynamicFakeUserProfileDao : UserProfileDao {
        val stateFlow = MutableStateFlow<UserProfileEntity?>(null)
        override fun getUserProfile(): Flow<UserProfileEntity?> = stateFlow
        override suspend fun insertOrUpdateProfile(profile: UserProfileEntity) {
            stateFlow.value = profile
        }
        override suspend fun clearUserProfile() {
            stateFlow.value = null
        }
    }

    private class FakeSecurityManager : SecurityManager {
        private val map = mutableMapOf<String, String>()
        override fun putEncryptedString(key: String, value: String) { map[key] = value }
        override fun getEncryptedString(key: String, defaultValue: String?) = map[key] ?: defaultValue
        override fun removeKey(key: String) { map.remove(key) }
        override fun clearAll() { map.clear() }
    }

    private class FakeDataStore(context: android.content.Context) : RtiqaPreferencesDataStore(context) {
        var activeUserId: String? = null
        override suspend fun setActiveUserId(userId: String?) {
            activeUserId = userId
        }
    }

    private fun createRepository(
        apiService: DynamicFakeApiService,
        userProfileDao: DynamicFakeUserProfileDao,
        securityManager: FakeSecurityManager,
        dataStore: FakeDataStore
    ) = AuthRepositoryImpl(
        database = object : com.rtiqa.core.database.RtiqaDatabase() { override fun createOpenHelper(config: androidx.room.DatabaseConfiguration): androidx.sqlite.db.SupportSQLiteOpenHelper { throw NotImplementedError() } override fun createInvalidationTracker(): androidx.room.InvalidationTracker { throw NotImplementedError() } override fun clearAllTables() {} override fun userProfileDao(): com.rtiqa.core.database.dao.UserProfileDao { throw NotImplementedError() } override fun courseDao(): com.rtiqa.core.database.dao.CourseDao { throw NotImplementedError() } override fun lessonDao(): com.rtiqa.core.database.dao.LessonDao { throw NotImplementedError() } override fun aiInsightDao(): com.rtiqa.core.database.dao.AiInsightDao { throw NotImplementedError() } override fun syncDao(): com.rtiqa.core.database.dao.SyncDao { throw NotImplementedError() } override fun enterpriseDao(): com.rtiqa.core.database.dao.EnterpriseDao { throw NotImplementedError() } override fun schoolClassDao(): com.rtiqa.core.database.dao.SchoolClassDao { throw NotImplementedError() } override fun academicDao(): com.rtiqa.core.database.dao.AcademicDao { throw NotImplementedError() } override fun schoolManagementCoreDao(): com.rtiqa.core.database.dao.SchoolManagementCoreDao { throw NotImplementedError() } override fun clearSensitiveData() { } },
        apiService = apiService,
        userProfileDao = userProfileDao,
        preferencesDataStore = dataStore,
        securityManager = securityManager,
        authRemoteDataSource = object : com.rtiqa.core.domain.repository.AuthRemoteDataSource {
        override suspend fun login(email: String, pass: String) = com.rtiqa.core.domain.result.RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.AuthError("Not used in test fallback"))
        override suspend fun register(name: String, email: String, pass: String) = com.rtiqa.core.domain.result.RtiqaResult.Error(com.rtiqa.core.domain.error.RtiqaError.AuthError("Not used"))
        override suspend fun resetPassword(email: String) = com.rtiqa.core.domain.result.RtiqaResult.Success(Unit)
        override suspend fun logout() = com.rtiqa.core.domain.result.RtiqaResult.Success(Unit)
        override suspend fun getCurrentUserId(): String? = null
    },
        sessionStore = com.rtiqa.core.network.session.RestSessionStoreImpl(securityManager),
        syncMutex = kotlinx.coroutines.sync.Mutex()
    )

    // Test 1: Correct login
    @Test
    fun login_correctCredentials_returnsSuccess() = runTest {
        val apiService = DynamicFakeApiService()
        val dao = DynamicFakeUserProfileDao()
        val sec = FakeSecurityManager()
        val ds = FakeDataStore(ApplicationProvider.getApplicationContext())
        val repo = createRepository(apiService, dao, sec, ds)

        val result = repo.login("alex@rtiqa.com", "password123")
        assertTrue(result is RtiqaResult.Success)
        assertEquals("fake_jwt_token", sec.getEncryptedString("rtiqa_rest_auth_token"))
        assertEquals("u1", ds.activeUserId)
    }

    // Test 2: Wrong password
    @Test
    fun login_wrongPassword_returnsAuthError() = runTest {
        val apiService = DynamicFakeApiService().apply {
            shouldFailLogin = true
            errorStatusCode = 401
        }
        val dao = DynamicFakeUserProfileDao()
        val sec = FakeSecurityManager()
        val ds = FakeDataStore(ApplicationProvider.getApplicationContext())
        val repo = createRepository(apiService, dao, sec, ds)

        val result = repo.login("alex@rtiqa.com", "wrong_pass")
        assertTrue(result is RtiqaResult.Error)
        assertTrue((result as RtiqaResult.Error).error is RtiqaError.AuthError)
    }

    // Test 3: Non-existent user
    @Test
    fun login_nonExistentUser_returnsAuthError() = runTest {
        val apiService = DynamicFakeApiService().apply {
            shouldFailLogin = true
            errorStatusCode = 404
        }
        val dao = DynamicFakeUserProfileDao()
        val sec = FakeSecurityManager()
        val ds = FakeDataStore(ApplicationProvider.getApplicationContext())
        val repo = createRepository(apiService, dao, sec, ds)

        val result = repo.login("nobody@rtiqa.com", "pass")
        assertTrue(result is RtiqaResult.Error)
    }

    // Test 4: Create new account
    @Test
    fun register_createNewAccount_returnsSuccess() = runTest {
        val apiService = DynamicFakeApiService()
        val dao = DynamicFakeUserProfileDao()
        val sec = FakeSecurityManager()
        val ds = FakeDataStore(ApplicationProvider.getApplicationContext())
        val repo = createRepository(apiService, dao, sec, ds)

        val result = repo.register("Sara", "sara@rtiqa.com", "pass123")
        assertTrue(result is RtiqaResult.Success)
        assertEquals("Sara", (result as RtiqaResult.Success).data.name)
        assertEquals("u2", ds.activeUserId)
    }

    // Test 5: Account already exists offline fallback
    @Test
    fun register_apiFailure_fallbackCreatesLocalProfile() = runTest {
        val apiService = DynamicFakeApiService().apply { shouldFailRegister = true }
        val dao = DynamicFakeUserProfileDao()
        val sec = FakeSecurityManager()
        val ds = FakeDataStore(ApplicationProvider.getApplicationContext())
        val repo = createRepository(apiService, dao, sec, ds)

        val result = repo.register("Existing User", "exist@rtiqa.com", "pass")
        assertTrue(result is RtiqaResult.Success)
        assertEquals("Existing User", (result as RtiqaResult.Success).data.name)
    }

    // Test 6: Forgot password
    @Test
    fun resetPassword_alwaysReturnsSuccess() = runTest {
        val repo = createRepository(
            DynamicFakeApiService(),
            DynamicFakeUserProfileDao(),
            FakeSecurityManager(),
            FakeDataStore(ApplicationProvider.getApplicationContext())
        )

        val result = repo.resetPassword("alex@rtiqa.com")
        assertTrue(result is RtiqaResult.Success)
    }

    // Test 7: Logout
    @Test
    fun logout_clearsDataAndSession() = runTest {
        val dao = DynamicFakeUserProfileDao()
        val sec = FakeSecurityManager().apply { putEncryptedString("rtiqa_rest_auth_token", "token") }
        val ds = FakeDataStore(ApplicationProvider.getApplicationContext()).apply { activeUserId = "u1" }
        val repo = createRepository(DynamicFakeApiService(), dao, sec, ds)

        val result = repo.logout()
        assertTrue(result is RtiqaResult.Success)
        assertNull(ds.activeUserId)
        assertNull(sec.getEncryptedString("rtiqa_rest_auth_token"))
        assertNull(dao.getUserProfile().first())
    }

    // Test 8: Re-open app & session persistence
    @Test
    fun observeUserSession_returnsPersistedUser() = runTest {
        val dao = DynamicFakeUserProfileDao().apply {
            insertOrUpdateProfile(UserProfileEntity("u1", "Alex", "alex@rtiqa.com", null, 100, 5))
        }
        val repo = createRepository(
            DynamicFakeApiService(),
            dao,
            FakeSecurityManager(),
            FakeDataStore(ApplicationProvider.getApplicationContext())
        )

        val session = repo.observeUserSession().first()
        assertEquals("u1", session?.id)
        assertEquals("Alex", session?.name)
    }

    // Test 9: Protected screen attempt without login (no session)
    @Test
    fun observeUserSession_whenLoggedOut_returnsNull() = runTest {
        val dao = DynamicFakeUserProfileDao()
        val repo = createRepository(
            DynamicFakeApiService(),
            dao,
            FakeSecurityManager(),
            FakeDataStore(ApplicationProvider.getApplicationContext())
        )

        val session = repo.observeUserSession().first()
        assertNull(session)
    }

    // Test 10: Offline mode login with cached profile
    @Test
    fun login_offlineWithCachedProfile_succeeds() = runTest {
        val apiService = DynamicFakeApiService().apply { isNetworkDown = true }
        val dao = DynamicFakeUserProfileDao().apply {
            insertOrUpdateProfile(UserProfileEntity("u1", "Alex", "alex@rtiqa.com", null, 100, 5))
        }
        val sec = FakeSecurityManager()
        val ds = FakeDataStore(ApplicationProvider.getApplicationContext())
        val repo = createRepository(apiService, dao, sec, ds)

        val result = repo.login("alex@rtiqa.com", "anypass")
        assertTrue(result is RtiqaResult.Success)
        assertEquals("Alex", (result as RtiqaResult.Success).data.name)
    }
}


// Dummy implementation of RtiqaDatabase for testing
abstract class FakeRtiqaDatabase : com.rtiqa.core.database.RtiqaDatabase() {
    // We don't need to implement anything since it's just passed along
}
// End of file
