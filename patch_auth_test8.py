import sys

file_path = "/app/applet/core-data/src/test/java/com/rtiqa/core/data/remote/NodeAuthDataSourceImplTest.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace(
    'mockApi.throwable = retrofit2.HttpException(retrofit2.Response.error<Any>(401, okhttp3.ResponseBody.create(null, "Unauthorized")))',
    'mockApi.throwable = retrofit2.HttpException(retrofit2.Response.error<Any>(401, okhttp3.ResponseBody.create(okhttp3.MediaType.parse("application/json"), "Unauthorized")))'
)
content = content.replace(
    'mockApi.throwable = retrofit2.HttpException(retrofit2.Response.error<Any>(500, okhttp3.ResponseBody.create(null, "Server Error")))',
    'mockApi.throwable = retrofit2.HttpException(retrofit2.Response.error<Any>(500, okhttp3.ResponseBody.create(okhttp3.MediaType.parse("application/json"), "Server Error")))'
)

with open(file_path, "w") as f:
    f.write(content)
