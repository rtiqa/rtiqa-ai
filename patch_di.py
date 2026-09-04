import sys

file_path = "/app/applet/core-data/src/main/java/com/rtiqa/core/data/di/AppDiContainer.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace(
    "import com.rtiqa.core.data.remote.FirebaseAuthDataSourceImpl",
    "import com.rtiqa.core.data.remote.FirebaseAuthDataSourceImpl\nimport com.rtiqa.core.data.remote.NodeAuthDataSourceImpl\nimport com.rtiqa.core.network.RestNetworkClient\nimport com.rtiqa.core.network.session.RestSessionStoreImpl\nimport com.rtiqa.core.network.session.RestSessionStore"
)

content = content.replace(
    "class AppDiContainer(val context: Context) {",
    "class AppDiContainer(val context: Context) {\n\n    companion object {\n        const val REST_AUTH_ENABLED = true\n    }"
)

auth_ds_replacement = """    val restSessionStore: RestSessionStore by lazy {
        RestSessionStoreImpl(coreDiContainer.securityManager)
    }

    val restNetworkClient: RestNetworkClient by lazy {
        RestNetworkClient(restSessionStore, isDebug = true)
    }

    val authRemoteDataSource: AuthRemoteDataSource by lazy {
        if (REST_AUTH_ENABLED) {
            NodeAuthDataSourceImpl(
                restApiContract = restNetworkClient.api,
                sessionStore = restSessionStore
            )
        } else {
            FirebaseAuthDataSourceImpl()
        }
    }"""

content = content.replace(
    """    val authRemoteDataSource: AuthRemoteDataSource by lazy {
        FirebaseAuthDataSourceImpl()
    }""",
    auth_ds_replacement
)

with open(file_path, "w") as f:
    f.write(content)

print("Patched AppDiContainer.kt")
