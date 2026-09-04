import sys

file_path = "/app/applet/core-data/src/test/java/com/rtiqa/core/data/remote/NodeAuthDataSourceImplTest.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace(
    "import okhttp3.ResponseBody.Companion.toResponseBody",
    "import okhttp3.MediaType.Companion.toMediaTypeOrNull\nimport okhttp3.ResponseBody.Companion.toResponseBody"
)
content = content.replace(
    '"Unauthorized".toResponseBody(null)',
    '"Unauthorized".toResponseBody("application/json".toMediaTypeOrNull())'
)
content = content.replace(
    '"Server Error".toResponseBody(null)',
    '"Server Error".toResponseBody("application/json".toMediaTypeOrNull())'
)

with open(file_path, "w") as f:
    f.write(content)
