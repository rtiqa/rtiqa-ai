import sys

file_path = "core-data/src/test/java/com/rtiqa/core/data/remote/NodeAuthDataSourceImplTest.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace(
    "import retrofit2.Retrofit\nimport retrofit2.converter.moshi.MoshiConverterFactory",
    "import retrofit2.Retrofit\nimport retrofit2.converter.moshi.MoshiConverterFactory"
)
if "import retrofit2.converter.moshi.MoshiConverterFactory" not in content:
    content = content.replace("import retrofit2.Retrofit", "import retrofit2.Retrofit\nimport retrofit2.converter.moshi.MoshiConverterFactory")

with open(file_path, "w") as f:
    f.write(content)
