import sys
content = open('core-data/src/test/java/com/rtiqa/core/data/repository/QuizRepositoryImplTest.kt').read()

lines = content.split('\n')
# We need to remove lines 156 through 163. Wait, lines in array are 0-indexed.
# Let's just find "        )" that corresponds to line 164.
# It's better to just write the setUp method properly.
start_idx = -1
end_idx = -1
for i, line in enumerate(lines):
    if 'fun setUp() {' in line:
        start_idx = i
    if 'fun getQuizzesForCourse_returnsDefaultWhenDbEmpty()' in line:
        end_idx = i

if start_idx != -1 and end_idx != -1:
    setup_code = """    fun setUp() {
        fakeDao = FakeAcademicDao()
        fakeSyncDao = FakeSyncDao()
        offlineSyncManager = OfflineSyncManager(
            apiService = FakeRtiqaApiService(),
            courseDao = FakeCourseDao(),
            syncDao = fakeSyncDao,
            syncMutex = kotlinx.coroutines.sync.Mutex(),
            sessionStore = object : com.rtiqa.core.network.session.RestSessionStore {
                var _sessionId: String? = "test_session_id"
                override fun saveSession(t: String, o: String?) {}
                override fun getSessionToken() = null
                override fun getActiveOrganizationId() = null
                override fun updateActiveOrganizationId(o: String?) {}
                override fun generateAndSaveSessionId() = "new_id".also { _sessionId = it }
                override fun getSessionId() = _sessionId
                override fun clearSession() { _sessionId = null }
            }
        )
        repository = QuizRepositoryImpl(
            academicDao = fakeDao,
            offlineSyncManager = offlineSyncManager,
            currentUserIdProvider = { "user_123" }
        )
    }

    @Test"""
    new_content = '\n'.join(lines[:start_idx]) + '\n' + setup_code + '\n'.join(lines[end_idx:])
    open('core-data/src/test/java/com/rtiqa/core/data/repository/QuizRepositoryImplTest.kt', 'w').write(new_content)
