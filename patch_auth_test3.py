import sys

file_path = "/app/applet/core-data/src/test/java/com/rtiqa/core/data/remote/NodeAuthDataSourceImplTest.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace(
    'ResponseBody.create(MediaType.parse("application/json"), "Unauthorized")',
    'ResponseBody.create("application/json".toMediaTypeOrNull(), "Unauthorized")'
)
content = content.replace(
    'ResponseBody.create(MediaType.parse("application/json"), "Server Error")',
    'ResponseBody.create("application/json".toMediaTypeOrNull(), "Server Error")'
)
content = content.replace(
    'import okhttp3.MediaType\nimport okhttp3.ResponseBody',
    'import okhttp3.MediaType.Companion.toMediaTypeOrNull\nimport okhttp3.ResponseBody.Companion.toResponseBody\nimport okhttp3.ResponseBody'
)
content = content.replace(
    'ResponseBody.create',
    '"Unauthorized".toResponseBody'
)

with open(file_path, "w") as f:
    f.write(content)
