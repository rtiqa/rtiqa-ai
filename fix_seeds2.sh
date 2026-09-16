#!/bin/bash
sed -i 's/schoolId = "school_001"/schoolId = ""/g' /app/applet/core-data/src/main/java/com/rtiqa/core/data/repository/CourseRepositoryImpl.kt
sed -i 's/schoolId = "school_001"/schoolId = ""/g' /app/applet/core-data/src/main/java/com/rtiqa/core/data/repository/AuthRepositoryImpl.kt
