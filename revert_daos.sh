#!/bin/bash
sed -i '/@Query("DELETE FROM/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt
sed -i '/suspend fun clearAllLessons()/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/RtiqaDaos.kt
sed -i '/@Query("DELETE FROM/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/SchoolClassDao.kt
sed -i '/suspend fun clearAllClasses()/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/SchoolClassDao.kt
sed -i '/@Query("DELETE FROM/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
sed -i '/suspend fun clearAllSchools()/d' /app/applet/core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt
