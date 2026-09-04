import sys

file_path = "core-data/src/test/java/com/rtiqa/core/data/remote/NodeAuthDataSourceImplTest.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace(
    "MoshiConverterFactory.create()",
    "MoshiConverterFactory.create(com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build())"
)

with open(file_path, "w") as f:
    f.write(content)
