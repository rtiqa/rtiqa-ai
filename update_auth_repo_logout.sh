#!/bin/bash
sed -i 's/class AuthRepositoryImpl(/class AuthRepositoryImpl(\n    private val database: com.rtiqa.core.database.RtiqaDatabase,/' /app/applet/core-data/src/main/java/com/rtiqa/core/data/repository/AuthRepositoryImpl.kt
sed -i 's/userProfileDao.clearUserProfile()/userProfileDao.clearUserProfile()\n                database.clearSensitiveData()/' /app/applet/core-data/src/main/java/com/rtiqa/core/data/repository/AuthRepositoryImpl.kt
