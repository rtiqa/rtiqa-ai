package com.rtiqa.core.data.repository

import android.content.Context
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.database.RtiqaDatabase
import com.rtiqa.core.database.dao.UserProfileDao
import com.rtiqa.core.database.entity.UserProfileEntity
import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.repository.AuthRemoteDataSource
import com.rtiqa.core.domain.repository.RemoteAuthUser
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.network.api.AuthResponseDto
import com.rtiqa.core.network.api.ClassGradebookDto
import com.rtiqa.core.network.api.LessonCompletionResponseDto
import com.rtiqa.core.network.api.LessonProgressRequestDto
import com.rtiqa.core.network.api.LessonProgressResponseDto
import com.rtiqa.core.network.api.LoginRequestDto
import com.rtiqa.core.network.api.NetworkCourseDto
import com.rtiqa.core.network.api.NetworkLessonDto
import com.rtiqa.core.network.api.NetworkSyncPayloadDto
import com.rtiqa.core.network.api.NetworkSyncResponseDto
import com.rtiqa.core.network.api.NetworkUserDto
import com.rtiqa.core.network.api.RegisterRequestDto
import com.rtiqa.core.network.api.RtiqaApiService
import com.rtiqa.core.network.session.RestSessionStore
import com.rtiqa.core.security.SecurityManager
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
class AuthRepositoryImplTest {

    private class FakeApiService : RtiqaApiService {
        var loginResponseCode: Int? = null
        var registerResponseCode: Int? = null
        var networkFailure = false
        var loginCalls = 0
        var registerCalls = 0

        override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> {
            loginCalls++
            if (networkFailure) throw IOException("Network down")
            loginResponseCode?.let { return Response.error(it, ResponseBody.create(null, "Error")) }
            return Response.success(
                AuthResponseDto("secondary_access_token", NetworkUserDto("u1", request.email, "Alex", 5, 200))
            )
        }

        override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> {
            registerCalls++
            if (networkFailure) throw IOException("Network down")
            registerResponseCode?.let { return Response.error(it, ResponseBody.create(null, "Error")) }
            return Response.success(
                AuthResponseDto("registration_access_token", NetworkUserDto("u2", request.email, request.name, 0, 0))
            )
        }

        override suspend fun getUserProfile() =
            Response.success(NetworkUserDto("u1", "alex@rtiqa.com", "Alex", 5, 200))
        override suspend fun getCourses(category: String?) = Response.success(emptyList<NetworkCourseDto>())
        override suspend fun getCourse(courseId: String): Response<NetworkCourseDto> = throw NotImplementedError()
        override suspend fun getCourseLessons(courseId: String) = Response.success(emptyList<NetworkLessonDto>())
        override suspend fun getLesson(courseId: String, lessonId: String): Response<NetworkLessonDto> =
            throw NotImplementedError()
        override suspend fun syncOfflineData(payload: NetworkSyncPayloadDto) =
            Response.success(NetworkSyncResponseDto(true, 1L, "Synced"))
        override suspend fun completeLesson(courseId: String, lessonId: String) = Response.success(
            LessonCompletionResponseDto(true, lessonId, courseId, true, 100f, 1, 1)
        )
        override suspend fun updateLessonProgress(
            courseId: String,
            lessonId: String,
            request: LessonProgressRequestDto
        ): Response<LessonProgressResponseDto> = completeLesson(courseId, lessonId)

        override suspend fun getClassGradebook(classId: String): Response<ClassGradebookDto> =
            Response.success(ClassGradebookDto(classId, emptyList(), emptyList(), emptyList()))
    }

    private class FakeUserProfileDao : UserProfileDao {
        val profile = MutableStateFlow<UserProfileEntity?>(null)
        var insertCount = 0
        override fun getUserProfile(): Flow<UserProfileEntity?> = profile
        override suspend fun insertOrUpdateProfile(profile: UserProfileEntity) {
            insertCount++
            this.profile.value = profile
        }
        override suspend fun clearUserProfile() {
            profile.value = null
        }
    }

    private class FakeSecurityManager : SecurityManager {
        private val values = mutableMapOf<String, String>()
        override fun putEncryptedString(key: String, value: String) { values[key] = value }
        override fun getEncryptedString(key: String, defaultValue: String?) = values[key] ?: defaultValue
        override fun removeKey(key: String) { values.remove(key) }
        override fun clearAll() = values.clear()
    }

    private class FakeSessionStore : RestSessionStore {
        var token: String? = null
        var organizationId: String? = null
        var storedSessionId: String? = null
        override fun saveSession(token: String, organizationId: String?) {
            this.token = token
            this.organizationId = organizationId
        }
        override fun getSessionToken() = token
        override fun getActiveOrganizationId() = organizationId
        override fun updateActiveOrganizationId(organizationId: String?) { this.organizationId = organizationId }
        override fun generateAndSaveSessionId(): String = "session-id".also { storedSessionId = it }
        override fun getSessionId() = storedSessionId
        override fun clearSession() {
            token = null
            organizationId = null
            storedSessionId = null
        }
    }

    private class FakeDataStore(context: Context) : RtiqaPreferencesDataStore(context) {
        var activeUserId: String? = null
        override suspend fun setActiveUserId(userId: String?) { activeUserId = userId }
    }

    private class FakeRemoteAuth(
        private val sessionStore: FakeSessionStore
    ) : AuthRemoteDataSource {
        var loginResult: RtiqaResult<RemoteAuthUser> = RtiqaResult.Error(RtiqaError.AuthError("Rejected"))
        var registerResult: RtiqaResult<RemoteAuthUser> = RtiqaResult.Error(RtiqaError.AuthError("Rejected"))
        var resetResult: RtiqaResult<Unit> = RtiqaResult.Success(Unit)
        var logoutFailure: Throwable? = null
        var serverToken: String? = null
        var serverOrganizationId: String? = null
        var registerCalls = 0

        override suspend fun login(email: String, pass: String): RtiqaResult<RemoteAuthUser> {
            if (loginResult is RtiqaResult.Success && serverToken != null) {
                sessionStore.saveSession(serverToken!!, serverOrganizationId)
            }
            return loginResult
        }
        override suspend fun register(name: String, email: String, pass: String): RtiqaResult<RemoteAuthUser> {
            registerCalls++
            return registerResult
        }
        override suspend fun resetPassword(email: String) = resetResult
        override suspend fun logout(): RtiqaResult<Unit> {
            logoutFailure?.let { throw it }
            sessionStore.clearSession()
            return RtiqaResult.Success(Unit)
        }
        override suspend fun getCurrentUserId(): String? = null
    }

    private class FakeDatabase : RtiqaDatabase() {
        var sensitiveDataCleared = false
        override fun createOpenHelper(config: DatabaseConfiguration): SupportSQLiteOpenHelper = throw NotImplementedError()
        override fun createInvalidationTracker(): InvalidationTracker = throw NotImplementedError()
        override fun clearAllTables() = Unit
        override fun userProfileDao() = throw NotImplementedError()
        override fun courseDao() = throw NotImplementedError()
        override fun lessonDao() = throw NotImplementedError()
        override fun aiInsightDao() = throw NotImplementedError()
        override fun syncDao() = throw NotImplementedError()
        override fun enterpriseDao() = throw NotImplementedError()
        override fun schoolClassDao() = throw NotImplementedError()
        override fun academicDao() = throw NotImplementedError()
        override fun schoolManagementCoreDao() = throw NotImplementedError()
        override fun clearSensitiveData() { sensitiveDataCleared = true }
    }

    private data class Fixture(
        val api: FakeApiService,
        val dao: FakeUserProfileDao,
        val security: FakeSecurityManager,
        val dataStore: FakeDataStore,
        val session: FakeSessionStore,
        val remote: FakeRemoteAuth,
        val database: FakeDatabase,
        val repository: AuthRepositoryImpl
    )

    private fun fixture(): Fixture {
        val api = FakeApiService()
        val dao = FakeUserProfileDao()
        val security = FakeSecurityManager()
        val dataStore = FakeDataStore(ApplicationProvider.getApplicationContext())
        val session = FakeSessionStore()
        val remote = FakeRemoteAuth(session)
        val database = FakeDatabase()
        val repository = AuthRepositoryImpl(
            database, api, dao, dataStore, security, remote, null, session, Mutex()
        )
        return Fixture(api, dao, security, dataStore, session, remote, database, repository)
    }

    private suspend fun Fixture.cacheProfile() {
        dao.insertOrUpdateProfile(UserProfileEntity("u1", "Alex", "alex@rtiqa.com", null, 100, 5))
        dao.insertCount = 0
    }

    private fun assertNotAuthenticated(fixture: Fixture, result: RtiqaResult<*>) {
        assertTrue("Expected authentication error, got $result", result is RtiqaResult.Error)
        assertNull(fixture.dataStore.activeUserId)
        assertNull(fixture.security.getEncryptedString("user_id"))
        assertNull(fixture.session.getSessionToken())
        assertNull(fixture.session.getSessionId())
    }

    @Test fun login_correctCredentials_returnsSuccess() = runTest {
        val f = fixture()
        val result = f.repository.login("alex@rtiqa.com", "password123")
        assertTrue(result is RtiqaResult.Success)
        assertEquals("secondary_access_token", f.session.token)
        assertEquals("u1", f.dataStore.activeUserId)
    }

    @Test fun login_401WithCachedProfile_returnsAuthError_andDoesNotActivateProfile() = runTest {
        val f = fixture().also { it.api.loginResponseCode = 401 }
        f.cacheProfile()
        assertNotAuthenticated(f, f.repository.login("alex@rtiqa.com", "wrong"))
    }

    @Test fun login_403WithCachedProfile_returnsAuthError_andDoesNotActivateProfile() = runTest {
        val f = fixture().also { it.api.loginResponseCode = 403 }
        f.cacheProfile()
        assertNotAuthenticated(f, f.repository.login("alex@rtiqa.com", "wrong"))
    }

    @Test fun login_validationFailure_doesNotActivateCachedProfile() = runTest {
        val f = fixture().also {
            it.remote.loginResult = RtiqaResult.Error(RtiqaError.ValidationError(listOf("invalid request")))
            it.api.loginResponseCode = 422
        }
        f.cacheProfile()
        assertNotAuthenticated(f, f.repository.login("alex@rtiqa.com", "wrong"))
    }

    @Test fun login_networkFailure_withoutSecureOfflineVerifier_doesNotAuthenticateCachedProfile() = runTest {
        val f = fixture().also {
            it.remote.loginResult = RtiqaResult.Error(RtiqaError.NetworkError("offline"))
            it.api.networkFailure = true
        }
        f.cacheProfile()
        assertNotAuthenticated(f, f.repository.login("alex@rtiqa.com", "any-password"))
    }

    @Test fun login_wrongPasswordWithCachedProfile_doesNotActivateCachedIdentity() = runTest {
        val f = fixture().also { it.api.loginResponseCode = 401 }
        f.cacheProfile()
        assertNotAuthenticated(f, f.repository.login("alex@rtiqa.com", "definitely-wrong"))
    }

    @Test fun login_authError_doesNotFallThroughToAnAlternativeProviderIfThatWouldBypassAuthoritativeRejection() = runTest {
        val f = fixture().also {
            it.remote.loginResult = RtiqaResult.Error(RtiqaError.AuthError("Invalid credentials"))
        }
        val result = f.repository.login("alex@rtiqa.com", "wrong")
        assertTrue(result is RtiqaResult.Error)
        assertEquals(0, f.api.loginCalls)
    }

    @Test fun register_createNewAccount_returnsSuccess() = runTest {
        val f = fixture()
        val result = f.repository.register("Sara", "sara@rtiqa.com", "pass123")
        assertTrue(result is RtiqaResult.Success)
        assertEquals("u2", f.dataStore.activeUserId)
    }

    @Test fun register_usesAuthoritativeApiDirectly_andDoesNotConsultLegacyProvider() = runTest {
        val f = fixture().also {
            it.remote.registerResult = RtiqaResult.Error(RtiqaError.AuthError("Authoritative rejection"))
        }

        val result = f.repository.register("Sara", "sara@rtiqa.com", "pass123")

        assertTrue(result is RtiqaResult.Success)
        assertEquals(0, f.remote.registerCalls)
        assertEquals(1, f.api.registerCalls)
    }

    @Test fun register_409Conflict_returnsError_andCreatesNoLocalAuthenticatedProfile() = runTest {
        val f = fixture().also { it.api.registerResponseCode = 409 }
        val result = f.repository.register("Existing", "exist@rtiqa.com", "pass123")
        assertNotAuthenticated(f, result)
        assertEquals(0, f.dao.insertCount)
    }

    @Test fun register_422ValidationError_returnsError_andCreatesNoSession() = runTest {
        val f = fixture().also { it.api.registerResponseCode = 422 }
        assertNotAuthenticated(f, f.repository.register("Sara", "bad@rtiqa.com", "pass123"))
        assertEquals(0, f.dao.insertCount)
    }

    @Test fun register_403Rejection_returnsError_andCreatesNoSession() = runTest {
        val f = fixture().also { it.api.registerResponseCode = 403 }
        assertNotAuthenticated(f, f.repository.register("Sara", "sara@rtiqa.com", "pass123"))
        assertEquals(0, f.dao.insertCount)
    }

    @Test fun register_networkFailure_doesNotReturnAuthenticatedSuccess() = runTest {
        val f = fixture().also { it.api.networkFailure = true }
        assertNotAuthenticated(f, f.repository.register("Sara", "sara@rtiqa.com", "pass123"))
        assertEquals(0, f.dao.insertCount)
    }

    @Test fun repositoryLogin_success_preservesExactServerAccessToken() = runTest {
        val f = successfulPrimaryLoginFixture()
        f.repository.login("alex@rtiqa.com", "correct")
        assertEquals("real.server.jwt", f.session.token)
    }

    @Test fun repositoryLogin_success_preservesReturnedOrganizationId() = runTest {
        val f = successfulPrimaryLoginFixture()
        f.repository.login("alex@rtiqa.com", "correct")
        assertEquals("org-42", f.session.organizationId)
    }

    @Test fun repositoryLogin_success_doesNotReplaceTokenWithPlaceholder() = runTest {
        val f = successfulPrimaryLoginFixture()
        f.repository.login("alex@rtiqa.com", "correct")
        assertFalse(f.session.token.orEmpty().startsWith("remote_token_"))
        assertFalse(f.session.token.orEmpty().startsWith("offline_token_"))
    }

    @Test fun repositoryLogin_success_doesNotClearOrganizationAfterProfilePersistence() = runTest {
        val f = successfulPrimaryLoginFixture()
        f.repository.login("alex@rtiqa.com", "correct")
        assertEquals("u1", f.dao.profile.value?.id)
        assertEquals("org-42", f.session.organizationId)
    }

    private fun successfulPrimaryLoginFixture() = fixture().also {
        it.remote.serverToken = "real.server.jwt"
        it.remote.serverOrganizationId = "org-42"
        it.remote.loginResult = RtiqaResult.Success(RemoteAuthUser("u1", "alex@rtiqa.com", "Alex"))
    }

    @Test fun observeUserSession_profileWithoutValidSession_returnsNull() = runTest {
        val f = fixture()
        f.cacheProfile()
        assertNull(f.repository.observeUserSession().first())
    }

    @Test fun observeUserSession_tokenWithoutMatchingProfile_returnsNull() = runTest {
        val f = fixture()
        f.session.saveSession("orphan-token", "org-42")
        assertNull(f.repository.observeUserSession().first())
    }

    @Test fun observeUserSession_whenLoggedOut_returnsNull() = runTest {
        assertNull(fixture().repository.observeUserSession().first())
    }

    @Test fun logout_clearsDataAndSession() = runTest {
        val f = fixture()
        f.cacheProfile()
        f.session.saveSession("token", "org")
        f.security.putEncryptedString("user_id", "u1")
        f.dataStore.activeUserId = "u1"
        assertTrue(f.repository.logout() is RtiqaResult.Success)
        assertNull(f.dao.profile.value)
        assertNull(f.session.token)
        assertNull(f.session.organizationId)
        assertNull(f.dataStore.activeUserId)
    }

    @Test fun logout_remoteFailure_stillClearsLocalSessionTokenOrganizationAndUserState() = runTest {
        val f = fixture()
        f.cacheProfile()
        f.session.saveSession("token", "org")
        f.session.generateAndSaveSessionId()
        f.security.putEncryptedString("user_id", "u1")
        f.dataStore.activeUserId = "u1"
        f.remote.logoutFailure = IOException("server unavailable")

        f.repository.logout()

        assertNull(f.session.token)
        assertNull(f.session.organizationId)
        assertNull(f.session.storedSessionId)
        assertNull(f.security.getEncryptedString("user_id"))
        assertNull(f.dataStore.activeUserId)
        assertNull(f.dao.profile.value)
        assertTrue(f.database.sensitiveDataCleared)
    }

    @Test fun resetPassword_remoteFailure_doesNotReturnSuccess() = runTest {
        val f = fixture().also {
            it.remote.resetResult = RtiqaResult.Error(RtiqaError.NetworkError("server unavailable"))
        }
        assertTrue(f.repository.resetPassword("alex@rtiqa.com") is RtiqaResult.Error)
    }
}
