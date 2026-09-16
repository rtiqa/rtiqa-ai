#!/bin/bash
# Re-patching AuthRepositoryImplTest.kt to properly inject the mock database in the test setup.
# Line 120 seems to have been missed or there's another instantiation of AuthRepositoryImpl.
sed -i 's/repository = AuthRepositoryImpl(/repository = AuthRepositoryImpl(\n            database = org.mockito.Mockito.mock(com.rtiqa.core.database.RtiqaDatabase::class.java),/g' /app/applet/core-data/src/test/java/com/rtiqa/core/data/repository/AuthRepositoryImplTest.kt
