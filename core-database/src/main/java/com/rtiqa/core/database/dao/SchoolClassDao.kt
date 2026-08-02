package com.rtiqa.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rtiqa.core.database.entity.SchoolClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolClassDao {
    @Query("SELECT * FROM school_classes WHERE schoolId = :schoolId ORDER BY displayOrder ASC, name ASC")
    fun getClassesForSchool(schoolId: String): Flow<List<SchoolClassEntity>>

    @Query("SELECT * FROM school_classes WHERE id = :id")
    suspend fun getClassById(id: String): SchoolClassEntity?

    @Query("SELECT * FROM school_classes WHERE schoolId = :schoolId AND LOWER(TRIM(name)) = LOWER(TRIM(:name)) AND id != :excludeId LIMIT 1")
    suspend fun findClassByNameInSchool(schoolId: String, name: String, excludeId: String = ""): SchoolClassEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schoolClass: SchoolClassEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<SchoolClassEntity>)

    @Query("DELETE FROM school_classes WHERE id = :id")
    suspend fun deleteClassById(id: String)
}
