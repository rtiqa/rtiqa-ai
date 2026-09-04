import sys
with open("core-data/build.gradle.kts", "r") as f:
    content = f.read()

if "converter-moshi" not in content:
    content = content.replace(
        "testImplementation(libs.junit)",
        "testImplementation(libs.junit)\n    testImplementation(libs.converter.moshi)"
    )
    with open("core-data/build.gradle.kts", "w") as f:
        f.write(content)
