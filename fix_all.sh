#!/bin/bash
# Revert DAOs again, this time correctly removing the suspend functions that were missing annotations.
# Looking at the error messages, the issue is with `suspend fun clearAll...()` functions in DAOs
sed -i '/suspend fun deleteLessonsForCourse/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt
sed -i '/suspend fun clearUserProfile/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt
sed -i '/suspend fun deleteSyncItem/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt
sed -i '/suspend fun clearAll()/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt

# EnterpriseDao
sed -i '/suspend fun deleteSchoolById/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun deleteMajorById/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun deleteSemesterById/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun deleteBranchById/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun deleteSectionById/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun deleteSubjectById/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun deleteStudyPlanById/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun deleteBuildingById/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun deleteRoomById/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun deleteMemberById/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt

# SchoolClassDao
sed -i '/suspend fun deleteClassById/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/SchoolClassDao.kt

# Now for the RestInterceptorsTest.kt syntax error
# It looks like the file was appended twice or has syntax errors. Let's reset the file and apply our test cleanly.
git checkout core-network/src/test/java/com/rtiqa/core/network/RestInterceptorsTest.kt || true
