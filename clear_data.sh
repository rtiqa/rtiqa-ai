#!/bin/bash
# Adding clear methods to Daos
sed -i '/@Dao/!b;n;a\    @Query("DELETE FROM courses")\n    suspend fun clearAllCourses()' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/CourseDao.kt
sed -i '/@Dao/!b;n;a\    @Query("DELETE FROM lessons")\n    suspend fun clearAllLessons()' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt
sed -i '/@Dao/!b;n;a\    @Query("DELETE FROM sync_queue")\n    suspend fun clearSyncQueue()' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/SyncDao.kt
sed -i '/@Dao/!b;n;a\    @Query("DELETE FROM schools")\n    suspend fun clearAllSchools()' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/@Dao/!b;n;a\    @Query("DELETE FROM school_classes")\n    suspend fun clearAllClasses()' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/SchoolClassDao.kt
