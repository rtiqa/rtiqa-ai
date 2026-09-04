import sys

file_path = "/app/applet/core-data/src/test/java/com/rtiqa/core/data/remote/NodeAuthDataSourceImplTest.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace(
    '"Unauthorized".toResponseBody("application/json".toMediaTypeOrNull(), "Unauthorized")',
    '"Unauthorized".toResponseBody("application/json".toMediaTypeOrNull())'
)
content = content.replace(
    '"Unauthorized".toResponseBody("application/json".toMediaTypeOrNull(), "Server Error")',
    '"Server Error".toResponseBody("application/json".toMediaTypeOrNull())'
)
with open(file_path, "w") as f:
    f.write(content)
