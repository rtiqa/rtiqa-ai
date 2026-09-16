#!/bin/bash
sed -i 's/schoolId = schoolId/schoolId = schoolId ?: ""/g' /app/applet/core-data/src/main/java/com/rtiqa/core/data/mapper/DataMappers.kt
sed -i 's/schoolId = schoolId ?: "" ?: ""/schoolId = schoolId ?: ""/g' /app/applet/core-data/src/main/java/com/rtiqa/core/data/mapper/DataMappers.kt
