#!/bin/bash
# Ah, `clearSensitiveData()` is implemented in `RtiqaDatabase`. But our mock doesn't implement it properly if we just instantiated `object : RtiqaDatabase()`.
# Wait, `clearSensitiveData()` is a concrete method in `RtiqaDatabase` that calls `runInTransaction`.
# Let's see `runInTransaction`. If it's a Room method, calling it on an uninitialized RoomDatabase might throw an IllegalStateException or NullPointerException.
# We should mock it to just do nothing in the test.

sed -i 's/override fun schoolManagementCoreDao(): com.rtiqa.core.database.dao.SchoolManagementCoreDao { throw NotImplementedError() } }/override fun schoolManagementCoreDao(): com.rtiqa.core.database.dao.SchoolManagementCoreDao { throw NotImplementedError() } override fun clearSensitiveData() { } }/g' /app/applet/core-data/src/test/java/com/rtiqa/core/data/repository/AuthRepositoryImplTest.kt
