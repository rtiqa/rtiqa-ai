package com.rtiqa.core.data.sync

import com.rtiqa.core.database.dao.CourseDao
import com.rtiqa.core.database.dao.SyncDao
import com.rtiqa.core.database.entity.CourseEntity
import com.rtiqa.core.database.entity.SyncQueueEntity
import com.rtiqa.core.network.api.RtiqaApiService
import com.rtiqa.core.network.session.RestSessionStore
import com.rtiqa.core.security.SecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.rtiqa.core.data.repository.FakeRtiqaApiService

class FakeIsolationSyncDao : SyncDao {
    val items = mutableListOf<SyncQueueEntity>()
    override fun getAllPendingSyncItems(userId: String, sessionId: String): Flow<List<SyncQueueEntity>> =
        flowOf(items.filter { it.ownerUserId == userId && it.ownerSessionId == sessionId })

    override suspend fun getPendingSyncItemsList(userId: String, sessionId: String): List<SyncQueueEntity> =
        items.filter { it.ownerUserId == userId && it.ownerSessionId == sessionId }

    override suspend fun insertSyncItem(item: SyncQueueEntity) {
        items.add(item)
    }

    override suspend fun deleteSyncItem(id: String) {
        items.removeAll { it.id == id }
    }

    override suspend fun clearAll() {
        items.clear()
    }
}

class OfflineSyncManagerIsolationTest {
    private lateinit var syncDao: FakeIsolationSyncDao
    private lateinit var managerA: OfflineSyncManager
    private lateinit var managerB: OfflineSyncManager

    @Before
    fun setUp() {
        syncDao = FakeIsolationSyncDao()

        val apiService = FakeRtiqaApiService()
        val courseDao = com.rtiqa.core.data.repository.FakeCourseDao()

        val sessionStoreA = object : RestSessionStore {
            override fun saveSession(token: String, organizationId: String?) {}
            override fun getSessionToken() = null
            override fun getActiveOrganizationId() = null
            override fun updateActiveOrganizationId(organizationId: String?) {}
            override fun generateAndSaveSessionId() = "session_A"
            override fun getSessionId() = "session_A"
            override fun clearSession() {}
        }
        val securityManagerA = object : SecurityManager {
            override fun putEncryptedString(key: String, value: String) {}
            override fun getEncryptedString(key: String, defaultValue: String?): String? = if (key == "user_id") "user_A" else defaultValue
            override fun removeKey(key: String) {}
            override fun clearAll() {}
        }
        
        managerA = OfflineSyncManager(
            apiService = apiService,
            courseDao = courseDao,
            syncDao = syncDao,
            syncMutex = Mutex(),
            sessionStore = sessionStoreA,
            securityManager = securityManagerA
        )

        val sessionStoreB = object : RestSessionStore {
            override fun saveSession(token: String, organizationId: String?) {}
            override fun getSessionToken() = null
            override fun getActiveOrganizationId() = null
            override fun updateActiveOrganizationId(organizationId: String?) {}
            override fun generateAndSaveSessionId() = "session_B"
            override fun getSessionId() = "session_B"
            override fun clearSession() {}
        }
        val securityManagerB = object : SecurityManager {
            override fun putEncryptedString(key: String, value: String) {}
            override fun getEncryptedString(key: String, defaultValue: String?): String? = if (key == "user_id") "user_B" else defaultValue
            override fun removeKey(key: String) {}
            override fun clearAll() {}
        }
        
        managerB = OfflineSyncManager(
            apiService = apiService,
            courseDao = courseDao,
            syncDao = syncDao,
            syncMutex = Mutex(),
            sessionStore = sessionStoreB,
            securityManager = securityManagerB
        )
    }

    @Test
    fun `user B does not see user A items`() = runTest {
        managerA.enqueueOfflineAction("TEST_A", "{}")
        
        val allItems = syncDao.items
        assertEquals(1, allItems.size)
        assertEquals("user_A", allItems[0].ownerUserId)
        assertEquals("session_A", allItems[0].ownerSessionId)

        val pendingForB = syncDao.getPendingSyncItemsList("user_B", "session_B")
        assertTrue(pendingForB.isEmpty())
        
        val pendingForA = syncDao.getPendingSyncItemsList("user_A", "session_A")
        assertEquals(1, pendingForA.size)
    }
}
