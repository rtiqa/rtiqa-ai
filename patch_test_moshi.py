import sys

file_path = "core-data/src/test/java/com/rtiqa/core/data/remote/NodeAuthDataSourceImplTest.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace(
    "retrofit2.converter.gson.GsonConverterFactory",
    "retrofit2.converter.moshi.MoshiConverterFactory"
)
content = content.replace(
    "GsonConverterFactory.create()",
    "MoshiConverterFactory.create()"
)

with open(file_path, "w") as f:
    f.write(content)
