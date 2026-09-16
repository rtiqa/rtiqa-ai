#!/bin/bash
sed -i 's/apiService = mockApiService/database = org.mockito.Mockito.mock(com.rtiqa.core.database.RtiqaDatabase::class.java),\n            apiService = mockApiService/g' /app/applet/core-data/src/test/java/com/rtiqa/core/data/repository/AuthRepositoryImplTest.kt
