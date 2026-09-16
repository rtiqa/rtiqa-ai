#!/bin/bash
# Restoring the clear/delete functions in RtiqaDaos.kt that were deleted during earlier repairs
sed -i '/suspend fun deleteLessonsForCourse/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt
sed -i '/suspend fun clearUserProfile/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt
sed -i '/suspend fun deleteSyncItem/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt
sed -i '/suspend fun clearAll/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt
sed -i '/suspend fun deleteCourseById/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt

# Put them back with proper annotations
sed -i '/interface CourseDao {/a\
    @Query("DELETE FROM courses WHERE id = :id")\
    suspend fun deleteCourseById(id: String)' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt

sed -i '/interface LessonDao {/a\
    @Query("DELETE FROM lessons WHERE courseId = :courseId")\
    suspend fun deleteLessonsForCourse(courseId: String)' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt

sed -i '/interface UserProfileDao {/a\
    @Query("DELETE FROM user_profiles")\
    suspend fun clearUserProfile()' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt

sed -i '/interface SyncDao {/a\
    @Query("DELETE FROM sync_queue WHERE id = :id")\
    suspend fun deleteSyncItem(id: String)\
    @Query("DELETE FROM sync_queue")\
    suspend fun clearAll()' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt

# Same for SchoolClassDao
sed -i '/suspend fun deleteClassById/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/SchoolClassDao.kt
sed -i '/interface SchoolClassDao {/a\
    @Query("DELETE FROM school_classes WHERE id = :id")\
    suspend fun deleteClassById(id: String)' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/SchoolClassDao.kt

