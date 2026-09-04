import sys

file_path = "core-data/src/test/java/com/rtiqa/core/data/remote/NodeAuthDataSourceImplTest.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace(
    "assertTrue(result is RtiqaResult.Success)",
    "println(\"RESULT IS: $result\")\n        assertTrue(result is RtiqaResult.Success)"
)
content = content.replace(
    "assertTrue(result is RtiqaResult.Error)",
    "println(\"RESULT IS: $result\")\n        assertTrue(result is RtiqaResult.Error)"
)

with open(file_path, "w") as f:
    f.write(content)
