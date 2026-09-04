import sys

file_path = "/app/applet/core-data/src/test/java/com/rtiqa/core/data/remote/NodeAuthDataSourceImplTest.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace(
    '"Unauthorized".toResponseBody("application/json".toMediaTypeOrNull())',
    'ResponseBody.create(okhttp3.MediaType.parse("application/json"), "Unauthorized")'
)
content = content.replace(
    '"Server Error".toResponseBody("application/json".toMediaTypeOrNull())',
    'ResponseBody.create(okhttp3.MediaType.parse("application/json"), "Server Error")'
)
content = content.replace(
    'import okhttp3.MediaType.Companion.toMediaTypeOrNull\nimport okhttp3.ResponseBody.Companion.toResponseBody\nimport okhttp3.ResponseBody',
    'import okhttp3.ResponseBody'
)

with open(file_path, "w") as f:
    f.write(content)
