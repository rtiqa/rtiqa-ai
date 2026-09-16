#!/bin/bash
sed -i 's/suspend fun deleteCourseById(id: String)/@Query("DELETE FROM courses WHERE id = :id")\n    suspend fun deleteCourseById(id: String)/g' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt
