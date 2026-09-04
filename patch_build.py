import sys
with open("core-data/build.gradle.kts", "r") as f:
    content = f.read()

if "mockwebserver" not in content:
    content = content.replace(
        "testImplementation(libs.junit)",
        "testImplementation(libs.junit)\n    testImplementation(\"com.squareup.okhttp3:mockwebserver:4.10.0\")"
    )
    with open("core-data/build.gradle.kts", "w") as f:
        f.write(content)
