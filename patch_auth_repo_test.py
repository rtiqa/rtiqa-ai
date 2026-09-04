import sys

file_path = "/app/applet/core-data/src/test/java/com/rtiqa/core/data/repository/AuthRepositoryImplTest.kt"
with open(file_path, "r") as f:
    content = f.read()

replacement = """    private fun createRepository(
        apiService: DynamicFakeApiService,
        userProfileDao: DynamicFakeUserProfileDao,
        securityManager: FakeSecurityManager,
        dataStore: FakeDataStore
    ) = AuthRepositoryImpl(
        apiService = apiService,
        userProfileDao = userProfileDao,
        preferencesDataStore = dataStore,
        securityManager = securityManager,
        authRemoteDataSource = com.rtiqa.core.data.remote.FirebaseAuthDataSourceImpl()
    )"""

content = content.replace(
    """    private fun createRepository(
        apiService: DynamicFakeApiService,
        userProfileDao: DynamicFakeUserProfileDao,
        securityManager: FakeSecurityManager,
        dataStore: FakeDataStore
    ) = AuthRepositoryImpl(
        apiService = apiService,
        userProfileDao = userProfileDao,
        preferencesDataStore = dataStore,
        securityManager = securityManager
    )""",
    replacement
)

with open(file_path, "w") as f:
    f.write(content)
