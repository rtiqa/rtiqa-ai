import sys
with open("core-data/build.gradle.kts", "r") as f:
    content = f.read()

if "moshi.kotlin" not in content:
    content = content.replace(
        "testImplementation(libs.converter.moshi)",
        "testImplementation(libs.converter.moshi)\n    testImplementation(libs.moshi.kotlin)"
    )
    with open("core-data/build.gradle.kts", "w") as f:
        f.write(content)
